package de.tu_darmstadt.sse.decisionmaker.server;

import heros.solver.CountingThreadPoolExecutor;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import soot.Unit;
import de.tu_darmstadt.sse.apkspecific.CodeModel.CodePosition;
import de.tu_darmstadt.sse.appinstrumentation.UtilInstrumenter;
import de.tu_darmstadt.sse.bootstrap.AnalysisTaskManager;
import de.tu_darmstadt.sse.bootstrap.DexFile;
import de.tu_darmstadt.sse.bootstrap.InstanceIndependentCodePosition;
import de.tu_darmstadt.sse.commandlinelogger.LoggerHelper;
import de.tu_darmstadt.sse.commandlinelogger.MyLevel;
import de.tu_darmstadt.sse.decisionmaker.DecisionMaker;
import de.tu_darmstadt.sse.decisionmaker.analysis.dynamicValues.DynamicIntValue;
import de.tu_darmstadt.sse.decisionmaker.analysis.dynamicValues.DynamicStringValue;
import de.tu_darmstadt.sse.decisionmaker.analysis.dynamicValues.DynamicValue;
import de.tu_darmstadt.sse.decisionmaker.server.history.ClientHistory;
import de.tu_darmstadt.sse.progressmetric.IProgressMetric;
import de.tu_darmstadt.sse.sharedclasses.crashreporter.CrashReportItem;
import de.tu_darmstadt.sse.sharedclasses.dynamiccfg.AbstractDynamicCFGItem;
import de.tu_darmstadt.sse.sharedclasses.networkconnection.CloseConnectionRequest;
import de.tu_darmstadt.sse.sharedclasses.networkconnection.DecisionRequest;
import de.tu_darmstadt.sse.sharedclasses.networkconnection.ServerResponse;
import de.tu_darmstadt.sse.sharedclasses.networkconnection.serializables.BinarySerializableObject;
import de.tu_darmstadt.sse.sharedclasses.tracing.DexFileTransferTraceItem;
import de.tu_darmstadt.sse.sharedclasses.tracing.DynamicIntValueTraceItem;
import de.tu_darmstadt.sse.sharedclasses.tracing.DynamicStringValueTraceItem;
import de.tu_darmstadt.sse.sharedclasses.tracing.DynamicValueTraceItem;
import de.tu_darmstadt.sse.sharedclasses.tracing.PathTrackingTraceItem;
import de.tu_darmstadt.sse.sharedclasses.tracing.TargetReachedTraceItem;
import de.tu_darmstadt.sse.sharedclasses.tracing.TimingBombTraceItem;
import de.tu_darmstadt.sse.sharedclasses.tracing.TraceItem;
import de.tu_darmstadt.sse.sharedclasses.util.NetworkSettings;


public class SocketServer {
	
	private static SocketServer socketServerInstance;
	
	private final DecisionMaker decisionMaker;
	private CountingThreadPoolExecutor executor = null;
	
	
	private long lastRequestProcessed = System.currentTimeMillis();

	private volatile boolean stopped = false;
	private volatile ServerSocket objectListener;
		
	private SocketServer(DecisionMaker decisionMaker) {
		this.decisionMaker = decisionMaker;
	}

	public static SocketServer getInstance(DecisionMaker decisionMaker) {
		if (socketServerInstance == null)
			return new SocketServer(decisionMaker);
		else
			return socketServerInstance;
	}		

	
	private class ClientHandlerObjectThread implements Runnable {

		private final Socket socket;

		public ClientHandlerObjectThread(Socket socket) {
			this.socket = socket;
		}

		@Override
		public void run() {			
			ObjectInputStream ois = null;
			ObjectOutputStream oos = null;

			try {
				int numAcks = 0;
				
				// Only create the streams once for the full lifetime of the socket
				ois = new ObjectInputStream(this.socket.getInputStream());
				oos = new ObjectOutputStream(this.socket.getOutputStream());
				
				
				
				while (ois != null && !socket.isClosed()) {					
					Object clientRequest = ois.readObject();
										
					// For every trace item, register the last position
					if (clientRequest instanceof TraceItem) {
						TraceItem ti = (TraceItem) clientRequest;
						ThreadTraceManager manager = decisionMaker.initializeHistory();
						if (manager != null) {
							ClientHistory currentClientHistory = manager.getNewestClientHistory();
							if (currentClientHistory != null) {
								currentClientHistory.addCodePosition(ti.getLastExecutedStatement(),
										decisionMaker.getCodePositionManager());
								
								// Make sure that our metrics are up to date
								for (IProgressMetric metric : decisionMaker.getConfig().getMetrics()) {
									metric.update(currentClientHistory);
								}
							}
						}
					}
					
					numAcks += handleClientRequests(oos, clientRequest);
					
					// Acknowledge that we have processed the items
					if (numAcks > 0) {
						while (numAcks > 0) {
							numAcks--;
							oos.writeObject("ACK");
						}
						oos.flush();
					}
					
					// Make sure we send out all our data
					if (oos != null)
						oos.flush();
					
					// Terminate the connection if requested
					if (clientRequest instanceof CloseConnectionRequest)
						break;
				}
			} catch (Exception ex) {
				LoggerHelper.logEvent(MyLevel.EXCEPTION_ANALYSIS, "There is a problem in the client-server communication "
								+ ex.getMessage());
				
				ex.printStackTrace();
			} finally {
				try {
					if (!socket.isOutputShutdown())
						socket.shutdownOutput();
					if (!socket.isInputShutdown())
						socket.shutdownInput();
				} catch (IOException ex) {
					ex.printStackTrace();
					System.err.println("Network communication died: "
							+ ex.getMessage());
				}
			}
		}

		
		private int handleClientRequests(ObjectOutputStream oos,
				Object clientRequest) throws IOException {
			int numAcks = 0;
			if (clientRequest instanceof PathTrackingTraceItem) {
//						System.out.println("Received a PathTrackingTraceItem");
				handlePathTracking((PathTrackingTraceItem) clientRequest);
				numAcks++;
			}
			else if (clientRequest instanceof DecisionRequest) {
				DecisionRequest request = (DecisionRequest)clientRequest;
				//there will be a hook in the dalvik part
				if(request.getCodePosition() != -1) {
					System.out.println("Received a DecisionRequest");
					handleDecisionRequest(
							(DecisionRequest) clientRequest, oos);
				}
			}
			else if (clientRequest instanceof CloseConnectionRequest) {
				System.out.println("Received a CloseConnectionRequest");
			}
			else if (clientRequest instanceof AbstractDynamicCFGItem) {
				handleDynamicCallgraph((AbstractDynamicCFGItem) clientRequest);
				numAcks++;
			}
			else if (clientRequest instanceof TargetReachedTraceItem) {
				handleGoalReached((TargetReachedTraceItem) clientRequest);
				numAcks++;
			}
			else if(clientRequest instanceof CrashReportItem) {
				CrashReportItem crash = (CrashReportItem)clientRequest;
				handleCrash(crash);
				LoggerHelper.logEvent(MyLevel.EXCEPTION_RUNTIME, String.format("%s | %s", crash.getLastExecutedStatement(), crash.getExceptionMessage()));
				numAcks++;
			}
			else if(clientRequest instanceof DexFileTransferTraceItem) {
				LoggerHelper.logInfo("received DexFileTransferTraceItem");
				handleDexFileReceived((DexFileTransferTraceItem)clientRequest);	
				numAcks++;
			}
			else if(clientRequest instanceof DynamicValueTraceItem) {
				handleDynamicValueReceived((DynamicValueTraceItem)clientRequest);	
				numAcks++;
			}
			else if(clientRequest instanceof TimingBombTraceItem) {
				handleTimingBombReceived((TimingBombTraceItem)clientRequest);	
				numAcks++;
			}
			else if (clientRequest instanceof BinarySerializableObject) {
				// Deserialize the contents of the binary object and recursively
				// process the request within.
				BinarySerializableObject bos = (BinarySerializableObject) clientRequest;
				ByteArrayInputStream bais = null;
				ObjectInputStream ois = null;
				Object innerRequest = null;
				try {
					bais = new ByteArrayInputStream(bos.getBinaryData());
					ois = new ObjectInputStream(bais);
					innerRequest = ois.readObject();
				} catch (ClassNotFoundException e) {
					System.err.println("Could not de-serialize inner request object");
					e.printStackTrace();
				}
				finally {
					if (bais != null)
						bais.close();
					if (ois != null)
						ois.close();
				}
				if (innerRequest != null)
					handleClientRequests(oos, innerRequest);
				numAcks++;
			}
			else
				throw new RuntimeException("Received an unknown data item from the app");
			return numAcks;
		}
	};


	
	public void startSocketServerObjectTransfer() {
		objectListener = null;
		try {								
			// Create the server socket
			objectListener = new ServerSocket(NetworkSettings.SERVERPORT_OBJECT_TRANSFER);
			executor = new CountingThreadPoolExecutor(1,
					Runtime.getRuntime().availableProcessors(),
					30, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
			
			// Wait for new incoming client connections
			while (!stopped) {
				System.out.println("waiting for client-app request...");
				try {
					Socket socket = objectListener.accept();
					lastRequestProcessed = System.currentTimeMillis();
					System.out.println("got client-app request...");
					executor.execute(new ClientHandlerObjectThread(socket));
				} catch (SocketException e) {
					// expected: another thread has called listener.close() to stop the server
					System.out.println();
				}
			}
		} catch (Exception e) {
			LoggerHelper.logEvent(MyLevel.EXCEPTION_ANALYSIS, "There is a problem in startSocketServerObjectTransfer: " + e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				if (objectListener != null && !objectListener.isClosed())
					objectListener.close();
				if (executor != null)
					executor.shutdown();
			} catch (IOException ex) {
				ex.printStackTrace();
				System.err.println("Server socket died: " + ex.getMessage());
			}
		}
	}

	
	private void sendResponse(ObjectOutputStream out, Object response)
			throws IOException {
		out.writeObject(response);
		out.flush();
		System.out.println("sending response to client-app...");
		System.out.println(response);
	}

	
	private void handleDecisionRequest(DecisionRequest decisionRequest,
			ObjectOutputStream oos) throws IOException {				
		// Run the analyses
		ServerResponse response = decisionMaker.resolveRequest(decisionRequest);
		String logMessage = String.format("[DECISION_REQUEST] %s \n [DECISION_RESPONSE] %s", decisionRequest, response);
		LoggerHelper.logEvent(MyLevel.DECISION_REQUEST_AND_RESPONSE, logMessage);
		
		// send the response to the client
		sendResponse(oos, response);
	}
	
	
	private void handlePathTracking(PathTrackingTraceItem pathTrackigTrace) {
		Unit codePostionUnit = decisionMaker.getCodePositionManager()
				.getUnitForCodePosition(pathTrackigTrace
						.getLastExecutedStatement());
		boolean decision = pathTrackigTrace.getLastConditionalResult();
		ThreadTraceManager mgr = decisionMaker.initializeHistory();
		if (mgr != null) {
			ClientHistory currentClientHistory = mgr.getNewestClientHistory();
			if (currentClientHistory != null) {
				currentClientHistory.addPathTrace(codePostionUnit, decision);
			}
		}
	}
	
	
	private void handleCrash(CrashReportItem crash) {
		ThreadTraceManager mgr = decisionMaker.initializeHistory();
		if (mgr != null) {
			ClientHistory currentClientHistory = mgr.getNewestClientHistory();
			currentClientHistory.setCrashException(crash.getExceptionMessage());
			System.err.println("Application crashed after " + crash.getLastExecutedStatement());
		}
	}
	
	
	private void handleDynamicCallgraph(AbstractDynamicCFGItem cgItem) {
		ThreadTraceManager currentManager = decisionMaker.initializeHistory();
		if (currentManager != null && decisionMaker.getDynamicCallgraph() != null) {
			decisionMaker.getDynamicCallgraph().enqueueItem(cgItem);
		}
	}
	
	
	private void handleDexFileReceived(DexFileTransferTraceItem dexFileRequest) {
		byte[] dexFile = dexFileRequest.getDexFile();
		try{
			// Write the received dex file to disk for debugging
			long timestamp = System.currentTimeMillis();
			String dirPath = String.format("%s/dexFiles/", UtilInstrumenter.SOOT_OUTPUT);
			File dir = new File(dirPath);
			if(!dir.exists())
				dir.mkdir();
			String filePath = String.format("%s/dexFiles/%d_dexfile.dex", UtilInstrumenter.SOOT_OUTPUT, timestamp);
			System.out.println(String.format("Dex-File: %s (code position: %d)", filePath,
					dexFileRequest.getLastExecutedStatement()));
			LoggerHelper.logEvent(MyLevel.DEXFILE, String.format("Received dex-file %s/dexFiles/%d_dexfile.dex", UtilInstrumenter.SOOT_OUTPUT, timestamp));
			Files.write(Paths.get(filePath), dexFile);
			
			// We need to remove the statements that load the external code,
			// because we merge it into a single app. We must not take the
			// last executed statement, but the current one -> +1.
			CodePosition codePos = decisionMaker.getCodePositionManager()
					.getCodePositionByID(dexFileRequest
							.getLastExecutedStatement());
			Unit codePosUnit = decisionMaker.getCodePositionManager().getUnitForCodePosition(codePos);
			
			Set<InstanceIndependentCodePosition> statementsToRemove = new HashSet<>();
			statementsToRemove.add(new InstanceIndependentCodePosition(codePos.getEnclosingMethod(),
					codePos.getLineNumber(), codePosUnit.toString()));
			
			// Register the new dex file and spawn an analysis task for it
			DexFile dexFileObj = decisionMaker.getDexFileManager().add(new DexFile(
					dexFileRequest.getFileName(), filePath, dexFile));
			AnalysisTaskManager taskManager = decisionMaker.getAnalysisTaskManager();
			taskManager.enqueueAnalysisTask(taskManager.getCurrentTask().deriveNewTask(dexFileObj,
					statementsToRemove));
		}catch(Exception ex) {
			ex.printStackTrace();
		}
		System.out.println("received dex file");
	}
	
	
	private void handleDynamicValueReceived(DynamicValueTraceItem dynamicValue) {
		// Get the current trace
		ThreadTraceManager mgr = decisionMaker.initializeHistory();
		if (mgr != null) {
			ClientHistory currentClientHistory = mgr.getNewestClientHistory();
			
			// Depending on the type of object received, we need to create a
			// different data object
			if (dynamicValue instanceof DynamicStringValueTraceItem) {
				DynamicStringValueTraceItem sti = (DynamicStringValueTraceItem) dynamicValue;
				String stringValue = sti.getStringValue();
				if (stringValue != null && stringValue.length() > 0) {
					DynamicValue val = new DynamicStringValue(dynamicValue.getLastExecutedStatement(),
							dynamicValue.getParamIdx(), stringValue);
					currentClientHistory.getDynamicValues().add(dynamicValue.getLastExecutedStatement(), val);
				}
			}
			else if (dynamicValue instanceof DynamicIntValueTraceItem) {
				DynamicIntValueTraceItem iti = (DynamicIntValueTraceItem) dynamicValue;
				DynamicValue val = new DynamicIntValue(dynamicValue.getLastExecutedStatement(),
						dynamicValue.getParamIdx(), iti.getIntValue());
				currentClientHistory.getDynamicValues().add(dynamicValue.getLastExecutedStatement(), val);
			}
			else
				throw new RuntimeException("Unknown trace item received from app");
		}
	}
	
	
	private void handleTimingBombReceived(TimingBombTraceItem timingBomb) {
		LoggerHelper.logEvent(MyLevel.TIMING_BOMB, "Timing bomb, originally " + timingBomb.getOriginalValue());
	}
	
	
	private void handleGoalReached(TargetReachedTraceItem grItem) {
		decisionMaker.setTargetReached(true);
		LoggerHelper.logEvent(MyLevel.LOGGING_POINT_REACHED, "REACHED: " + grItem.getLastExecutedStatement());
	}
	
	public long getLastRequestProcessed() {
		return lastRequestProcessed;
	}	
	
	public void notifyAppRunDone() {
		lastRequestProcessed = System.currentTimeMillis();
	}
	
	public void stop() {
		LoggerHelper.logInfo("Stopping socket server");
		stopped = true;
		try {
			if (objectListener != null) objectListener.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	public void resetForNewRun() {
		lastRequestProcessed = System.currentTimeMillis();
	}
}
