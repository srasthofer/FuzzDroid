package de.tu_darmstadt.sse.decisionmaker;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import soot.jimple.infoflow.android.axml.AXmlNode;
import soot.jimple.infoflow.android.manifest.ProcessManifest;
import de.tu_darmstadt.sse.EnvironmentResult;
import de.tu_darmstadt.sse.FrameworkOptions;
import de.tu_darmstadt.sse.FrameworkOptions.TraceConstructionMode;
import de.tu_darmstadt.sse.apkspecific.UtilApk;
import de.tu_darmstadt.sse.apkspecific.CodeModel.CodePositionManager;
import de.tu_darmstadt.sse.apkspecific.CodeModel.StaticCodeIndexer;
import de.tu_darmstadt.sse.bootstrap.AnalysisTaskManager;
import de.tu_darmstadt.sse.bootstrap.DexFileManager;
import de.tu_darmstadt.sse.commandlinelogger.LoggerHelper;
import de.tu_darmstadt.sse.commandlinelogger.MyLevel;
import de.tu_darmstadt.sse.decisionmaker.analysis.AnalysisDecision;
import de.tu_darmstadt.sse.decisionmaker.analysis.FuzzyAnalysis;
import de.tu_darmstadt.sse.decisionmaker.analysis.filefuzzer.FileFuzzer;
import de.tu_darmstadt.sse.decisionmaker.server.SocketServer;
import de.tu_darmstadt.sse.decisionmaker.server.ThreadTraceManager;
import de.tu_darmstadt.sse.decisionmaker.server.TraceManager;
import de.tu_darmstadt.sse.decisionmaker.server.history.ClientHistory;
import de.tu_darmstadt.sse.decisionmaker.server.history.GeneticCombination;
import de.tu_darmstadt.sse.dynamiccfg.DynamicCallgraphBuilder;
import de.tu_darmstadt.sse.dynamiccfg.utils.MapUtils;
import de.tu_darmstadt.sse.frameworkevents.FrameworkEvent;
import de.tu_darmstadt.sse.frameworkevents.manager.FrameworkEventManager;
import de.tu_darmstadt.sse.progressmetric.IProgressMetric;
import de.tu_darmstadt.sse.sharedclasses.networkconnection.DecisionRequest;
import de.tu_darmstadt.sse.sharedclasses.networkconnection.ServerResponse;
import de.tu_darmstadt.sse.sharedclasses.util.Pair;


public class DecisionMaker {
	
	private final DecisionMakerConfig config;
	private final DexFileManager dexFileManager;
	private final AnalysisTaskManager analysisTaskManager;
	
	private EnvironmentResult result = null;

	
	private static final int GENETIC_MIN_GENE_POOL_SIZE = 5;
	
	private static final int GENETIC_RANDOM_OFFSET = 10000;
	
	private static final float GENETIC_GENE_POOL_EXTENSION_PROBABILITY = 0.25f;
	
	private static final float GENETIC_PICK_BAD_DECISION_PROBABILITY = 0.10f;
	
	private static final int PENALYZE_ANALYSES_MIN_HISTORY_COUNT = 5;
	
	private static final float PENALYZE_ANALYSES_FACTOR = 2.0f;
	
	private ProcessManifest manifest;
	
	private SocketServer socketServer;
	private FrameworkEventManager eventManager;
	private DynamicCallgraphBuilder callgraphBuilder;
	private CodePositionManager codePositionManager;
	private StaticCodeIndexer codeIndexer;
	
	private final TraceManager traceManager = new TraceManager();
	
	private String logFileProgressName;
			
	private boolean geneticOnlyMode = false;
	
	public DecisionMaker(DecisionMakerConfig config, DexFileManager dexFileManager,
			AnalysisTaskManager analysisTaskManager) {
		this.config = config;
		this.dexFileManager = dexFileManager;
		this.analysisTaskManager = analysisTaskManager;
	}
	
	
	public void runPreAnalysisPhase() {
		logProgressMetricsInit();
		startAllPreAnalysis();
	}
		
	
	private void logProgressMetricsInit() {
		Date date = new Date() ;
	    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss") ;
	    FileWriter logFileProgress;
		try {
			logFileProgressName = "plot"+ File.separator +"logProgress-" + dateFormat.format(date) + ".data";
			logFileProgress = new FileWriter(logFileProgressName, true);
			for(IProgressMetric metric : config.getMetrics()) {
				String className = metric.getClass().getName();
				logFileProgress.write(className.substring(
						className.lastIndexOf('.')+1)+'\t');
			}
			logFileProgress.write(System.getProperty("line.separator"));
			logFileProgress.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	private void startAllPreAnalysis() {
		if(config.getAnalyses().size() == 0)
			throw new RuntimeException("There should be at least one analysis registered!");
		for(FuzzyAnalysis analysis : config.getAnalyses()) {
			LoggerHelper.logEvent(MyLevel.PRE_ANALYSIS_START, analysis.getAnalysisName());
			analysis.doPreAnalysis(config.getAllTargetLocations(), traceManager);
			LoggerHelper.logEvent(MyLevel.PRE_ANALYSIS_STOP, analysis.getAnalysisName());
		}
	}
	
	
	private ServerResponse computeResponse(DecisionRequest request,
			ThreadTraceManager currentManager) {
		// If we already have a decision for that request in the current
		// history, we take it
		{
			AnalysisDecision response = currentManager.getNewestClientHistory()
					.getResponseForRequest(request);
			if (response != null && response.getServerResponse().doesResponseExist()) {
				response.setDecisionUsed();
				LoggerHelper.logEvent(MyLevel.ANALYSIS_NAME, response.getAnalysisName());
				return response.getServerResponse();
			}
		}
		
		// Compute the analyses for the current request
		List<AnalysisDecision> allDecisions = new ArrayList<AnalysisDecision>();
		for(FuzzyAnalysis singleAnalysis : config.getAnalyses()) {
			Collection<AnalysisDecision> decisions = singleAnalysis.resolveRequest(
					request, currentManager);
			if (decisions != null) {
				// We only add decisions that actually value values
				for (AnalysisDecision decision : decisions)
					if (decision.getServerResponse().doesResponseExist())
						allDecisions.add(decision);
			}
		}
		
		// If we are in genetic-only mode and don't have a response in the
		// current trace, we try to get something from an older trace
		if (geneticOnlyMode && allDecisions.isEmpty()) {
			AnalysisDecision decision = currentManager.getBestResponse(request);
			if (decision != null && decision.getServerResponse().doesResponseExist())
				allDecisions.add(decision);
		}
		
		// If no analysis returned anything, but we asked, we create an empty response
		// so that we can at least keep track of the hook
		if (allDecisions.isEmpty()) {
			ServerResponse resp = new ServerResponse();
			resp.setResponseExist(false);
			resp.setAnalysisName("EMPTY_ANALYSIS");
			
			AnalysisDecision decision = new AnalysisDecision();
			decision.setServerResponse(resp);
			decision.setAnalysisName("EMPTY_ANALYSIS");
			allDecisions.add(decision);
			
			if (geneticOnlyMode)
				System.err.println("We're in genetic-only mode, but don't have a value for the "
						+ "request. **playing sad music**");
		}
		
		// Apply penalties (if any) to the decisions
		for (AnalysisDecision decision : allDecisions) {
			FuzzyAnalysis analysis = config.getAnalysisByName(decision.getAnalysisName());
			if (analysis != null) {
				int penalty = analysis.getPenaltyRank();
				if (penalty > 0) {
					float newWeight = (float) ((float) decision.getDecisionWeight() / (0.1 * (float) penalty + 1.0f));
					decision.setDecisionWeight(Math.round(newWeight));
				}
			}
		}
		
		// Get one of the decisions with the highest confidence
		AnalysisDecision finalDecision = getFinalDecision(allDecisions);
		
		// If the analysis gave us lots of choices, we need to feed them into
		// the trace set to make them available to the genetic algorithm in
		// future runs
		ClientHistory currentHistory = currentManager.getNewestClientHistory();
		if (allDecisions.size() > 1) {
			for (AnalysisDecision nonPickedDecision : allDecisions)
				if (nonPickedDecision != finalDecision
						&& nonPickedDecision.getServerResponse().doesResponseExist()) {
					ClientHistory shadow = currentHistory.clone();
					shadow.addDecisionRequestAndResponse(request, nonPickedDecision);
					shadow.setShadowTrace(true);
					currentManager.addShadowHistory(shadow);
				}
		}
		
		// Check that we have a decision
		if (finalDecision == null)
			return ServerResponse.getEmptyResponse();
		else
			finalDecision.setDecisionUsed();
		
		// Extract the server response to send back to the app and add it to the
		// current trace
		currentHistory.addDecisionRequestAndResponse(request, finalDecision);
		
		// If we have a shadow that is a prefix of the decision we have taken anyway,
		// there is no need to keep the shadow around for further testing.
		int removedCount = 0;
		for (Iterator<ClientHistory> shadowIt = currentManager.getShadowHistories().iterator();
				shadowIt.hasNext(); ) {
			ClientHistory shadow = shadowIt.next();
			if (shadow.isPrefixOf(currentHistory)) {
				shadowIt.remove();
				removedCount++;
			}
		}
		if (removedCount > 0)
			LoggerHelper.logInfo("Removed " + removedCount + " shadow histories, because they "
					+ "were prefixes of the decision we are trying now.");
		
		ServerResponse serverResponse = finalDecision.getServerResponse();
		serverResponse.setAnalysisName(finalDecision.getAnalysisName());
		return serverResponse;
	}
	
	
	public ServerResponse resolveRequest(DecisionRequest request) {
		System.out.println("Incoming decision request: "+request);
		
		// Get the current trace we're working on
		ThreadTraceManager currentManager = initializeHistory();
		if (currentManager == null)
			return ServerResponse.getEmptyResponse();
		
		// If we need a decision at a certain statement, we have reached that statement
		currentManager.getNewestClientHistory().addCodePosition(request.getCodePosition(),
				codePositionManager);
		
		// Make sure that we have updated the dynamic callgraph
		if (callgraphBuilder != null)
			callgraphBuilder.updateCFG();
		
		// Make sure that our metrics are up to date
		for (IProgressMetric metric : config.getMetrics()) {
			metric.update(currentManager.getNewestClientHistory());
		}
		
		// Compute the decision
		ServerResponse response = computeResponse(request, currentManager);
		if (response == null)
			response = ServerResponse.getEmptyResponse();
		
		//updating the Analysis Progress Metric
		//logging the new data to file
	    FileWriter logFileProgress;
		try{
			logFileProgress = new FileWriter(logFileProgressName, true);
			for (IProgressMetric metric : config.getMetrics()) {
				int newlyCovered = metric.update(currentManager.getNewestClientHistory());
				System.out.println("Metric for " + metric.getMetricName() + ":" + newlyCovered);
				logFileProgress.write(Integer.toString(newlyCovered)+'\t');			
			}
			logFileProgress.write(System.getProperty("line.separator"));
			logFileProgress.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
				
		return response;
	}
	

	
	private AnalysisDecision getFinalDecision(List<AnalysisDecision> decisions) {
		List<AnalysisDecision> finalDecisions = new ArrayList<AnalysisDecision>();
		if (decisions.isEmpty())
			return null;
		
		// Pick among those decisions with the highest confidence
		Collections.sort(decisions);
		int highestWeight = decisions.get(0).getDecisionWeight();
		for(AnalysisDecision decision : decisions) {
			if(decision.getDecisionWeight() == highestWeight)
				finalDecisions.add(decision);
			// with a certain (low) probability, we also pick a decision with lower
			// confidence
			else if (DeterministicRandom.theRandom.nextInt(GENETIC_RANDOM_OFFSET)
					< GENETIC_RANDOM_OFFSET * GENETIC_PICK_BAD_DECISION_PROBABILITY)
				finalDecisions.add(decision);
		}
		
		//random pick
		int amountOfDecisons = finalDecisions.size();
		if(amountOfDecisons > 1) {
			int randomPick = DeterministicRandom.theRandom.nextInt(amountOfDecisons);
			return finalDecisions.get(randomPick);
		}
		else
			return finalDecisions.get(0);
	}
	
	public void initialize() {		
		this.manifest = UtilApk.getManifest();
				
		// set up event manager
		eventManager =  FrameworkEventManager.getEventManager();		
		
		// Get a code model
		codePositionManager = CodePositionManager.getCodePositionManagerInstance();
		codeIndexer = new StaticCodeIndexer();
		
		//start server...
		socketServer = SocketServer.getInstance(this);
		Runnable r1 = new Runnable() {
			@Override
			public void run() {
				socketServer.startSocketServerObjectTransfer();				
			}
		};		
		Thread backgroundThreadForObjectTransfer = new Thread(r1);
		backgroundThreadForObjectTransfer.start();				
		
		// set up event manager
		eventManager =  FrameworkEventManager.getEventManager();
		eventManager.connectToAndroidDevice();	
		
		//monitor the logcat for VM crashes
		if(FrameworkOptions.enableLogcatViewer)
			eventManager.startLogcatCrashViewer();
	}
	
	
	private void reset() {
		// Create a new result object
		result = new EnvironmentResult();
		
		// Reset all analyses
		for (FuzzyAnalysis analysis : config.getAnalyses())
			analysis.reset();
	}
	
	public EnvironmentResult executeDecisionMaker(FrameworkEvent event) {
		reset();
		long startingTime = System.currentTimeMillis();
		
		//client handling...
		if(!FrameworkOptions.testServer){	
			//pull files onto device
			eventManager.pushFiles(FileFuzzer.FUZZY_FILES_DIR);
			eventManager.installApp(manifest.getPackageName());
			
			//add contacts onto device
			eventManager.addContacts(manifest.getPackageName());
			
			tryStartingApp();
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			};												
			
			if(event != null)
				eventManager.sendEvent(event);
			
			// Make sure that we don't have any old state lying around
			socketServer.resetForNewRun();

			// We only reset the genetic-only mode per app installation
			geneticOnlyMode = false;
			
			boolean trying = true;
			while(trying && !result.isTargetReached()) {
				// Compute the time since the last client request
				long currentTime = System.currentTimeMillis();
				long timeDiff = currentTime - socketServer.getLastRequestProcessed();
				
				// we do a complete (clean) re-install of the app
				if(timeDiff > FrameworkOptions.inactivityTimeout * 1000 ||
						(currentTime - startingTime) > (FrameworkOptions.forceTimeout * 1000)) {

					if (result.getRestartCount() < FrameworkOptions.maxRestarts || FrameworkOptions.maxRestarts == -1) {						
						LoggerHelper.logEvent(MyLevel.RESTART, String.format("Restarted app due to timeout: %d", result.getRestartCount()+1));
						LoggerHelper.logEvent(MyLevel.RESTART, String.format("timeDiff: %d\ncurr - starting: %d", timeDiff, (currentTime - startingTime)));

						eventManager.killAppProcess(manifest.getPackageName());
						eventManager.uninstallAppProcess(manifest.getPackageName());
						
						//wait a couple of seconds...
						try {
							Thread.sleep(5000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						};
						
						// Reset our internal state
						callgraphBuilder = null;
						result.setRestartCount(result.getRestartCount() + 1);
						socketServer.notifyAppRunDone();
						
						// We need to clean up the trace and remove all decision we haven't used
						cleanUpUnusedDecisions();

						// Check if one analysis performed poorly
						penalizePoorAnalyses();
						
						eventManager.installApp(manifest.getPackageName());
						startingTime = System.currentTimeMillis();
						tryStartingApp();
						
						try {
							Thread.sleep(3000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						};
						
						//send events
						if(event != null)
							eventManager.sendEvent(event);
					} else {
						LoggerHelper.logEvent(MyLevel.RUNTIME, "Maximum number of restarts reached -- giving up.");
						trying = false;
					}
				}
			}
			
			// Wait for the next task to arrive. We only need this is the app
			// sends really large dex files
//			try {
//				Thread.sleep(60000);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			
			// Make sure to clean up after ourselves
			eventManager.killAppProcess(manifest.getPackageName());
			eventManager.uninstallAppProcess(manifest.getPackageName());
		}
		//test server
		else {
			System.err.println("TESTING SERVER ONLY");
			while(true) {}
		}
		return result;
	}
	
	
	private void cleanUpUnusedDecisions() {
		for (ThreadTraceManager ttm : traceManager.getAllThreadTraceManagers()) {
			ttm.getNewestClientHistory().removeUnusedDecisions();
		}
	}

	
	private void penalizePoorAnalyses() {
		int historyCount = 0;
		Map<String, Integer> analysisToBestScore = new HashMap<>();
		for (ThreadTraceManager tm : traceManager.getAllThreadTraceManagers()) {
			for (ClientHistory hist : tm.getHistories()) {
				historyCount++;
				int progressVal = hist.getProgressValue("ApproachLevel");
				for (Pair<DecisionRequest, AnalysisDecision> pair : hist.getAllDecisionRequestsAndResponses()) {
					String name = pair.getSecond().getAnalysisName();
					Integer oldVal = analysisToBestScore.get(name);
					if (oldVal == null || oldVal < progressVal)
						analysisToBestScore.put(name, progressVal);
				}
			}
		}
		
		// We only judge analyses if have some data
		if (historyCount < PENALYZE_ANALYSES_MIN_HISTORY_COUNT || analysisToBestScore.size() < 2)
			return;
		
		// Check if we have an analysis that is  10 times worse than the next
		// better one
		Map<String, Integer> sortedMap = MapUtils.sortByValue(analysisToBestScore);
		Entry<String, Integer> lastEntry = null;
		int penaltyRank = 1;
		for (Entry<String, Integer> entry : sortedMap.entrySet()) {
			// Skip the first entry
			if (lastEntry == null) {
				lastEntry = entry;
				continue;
			}
			
			// Is this entry 10 times worse than the previous one?
			if (entry.getValue() * PENALYZE_ANALYSES_FACTOR < lastEntry.getValue()) {
				FuzzyAnalysis analysis = config.getAnalysisByName(entry.getKey());
				analysis.setPenaltyRank(penaltyRank++);
			}
			
			lastEntry = entry;
		}
	}

	public void tearDown() {
		socketServer.stop();
	}
	
//	
//	private String getLaunchableActivity(ProcessManifest manifest) {
//		Set<AXmlNode> allLaunchableActivities = manifest.getLaunchableActivities();
//		if(allLaunchableActivities.size() == 0) {
//			throw new RuntimeException("we do not support apps yet that do not have a launchable activitiy (e.g., just services)");
//		}			
//		else if(allLaunchableActivities.size() > 1)
//			LoggerHelper.logWarning("This app contains more than one activity that is launchable! Taking the first one which is defined in the manifest...");
//		else {
//			AXmlNode node = allLaunchableActivities.iterator().next();
//			return (String)node.getAttribute("name").getValue();
//		}
//		return null;
//	}
	
	
	public ThreadTraceManager getManagerForThreadId(long threadId) {
		ThreadTraceManager manager = traceManager.getThreadTraceManager(threadId);
		if (manager == null)
			throw new RuntimeException(
					"There has to be a client history for threadID: "
							+ threadId);

		return manager;
	}
	
	
	public synchronized ThreadTraceManager initializeHistory() {
		ThreadTraceManager manager = traceManager.getOrCreateThreadTraceManager(-1);
		
		// Only perform genetic recombination when actually generating new traces
		boolean forceGenetic = false;
		if (manager.getHistories().size() < result.getRestartCount() + 1) {
			// Are we in genetic-only mode?
			forceGenetic = geneticOnlyMode;
			
			// If we did not get any new values in the last run, the analyses have
			// run out of values. In that case, we can only rely on genetic
			// recombination.
			if (!forceGenetic) {
				// We can only make this decision if we have already had one complete run
				if (manager.getHistories().size() > 1 && manager.getLastClientHistory().hasOnlyEmptyDecisions()) {
					if (manager.getHistoryAndShadowCount() >= 2) {
						forceGenetic = true;
						geneticOnlyMode = true;
						LoggerHelper.logEvent(MyLevel.GENTETIC_ONLY_MODE, "genetic only mode on");
					}
					else {
						System.err.println("It's all empty now, but we don't have enough histories "
								+ "to combine. Looks like we're seriously out of luck.");
						return null;
					}
				}
			}
			
			// If we have a couple of histories, we do genetic recombination
			if (!forceGenetic) {
				if (manager.getHistories().size() > GENETIC_MIN_GENE_POOL_SIZE) {
					if (DeterministicRandom.theRandom.nextInt(GENETIC_RANDOM_OFFSET)
							< GENETIC_GENE_POOL_EXTENSION_PROBABILITY * GENETIC_RANDOM_OFFSET) {
						forceGenetic = true;
						LoggerHelper.logEvent(MyLevel.GENTETIC_ONLY_MODE, "genetic only mode on");
					}
				}
			}
			
			// When we do genetic recombination, we pre-create a history object
			if (forceGenetic && FrameworkOptions.traceConstructionMode
					!= TraceConstructionMode.AnalysesOnly) {
				LoggerHelper.logInfo("Using genetic recombination for generating a trace...");
				
				// We also need to take the shadow histories into account. We take histories
				// from all threads in case we are not on the main thread
				Set<ClientHistory> histories = new HashSet<>();
				for (ThreadTraceManager tmanager : traceManager.getAllThreadTraceManagers()) {
					histories.addAll(tmanager.getHistories());
					histories.addAll(tmanager.getShadowHistories());
				}
				
				// Do the genetic combination
				GeneticCombination combination = new GeneticCombination();
				ClientHistory combinedHistory = combination.combineGenetically(histories);
				if (combinedHistory == null) {
					LoggerHelper.logWarning("Genetic recombination failed.");
					return null;
				}
				combinedHistory.setShadowTrace(false);
				manager.ensureHistorySize(result.getRestartCount() + 1, combinedHistory);
				
				// Create the dynamic callgraph
				this.callgraphBuilder = new DynamicCallgraphBuilder(
						manager.getNewestClientHistory().getCallgraph(),
						codePositionManager,
						codeIndexer);
				return manager;
			}			
			// If we actually created a new trace, we must re-initialize the
			// factories
			else if (manager.ensureHistorySize(result.getRestartCount() + 1)) {
				// Check it
				if (geneticOnlyMode)
					System.err.println("In genetic only mode, but didn't recombine anything. Life ain't good, man :(");
				
				// Create the new trace
				LoggerHelper.logInfo("Creating a new empty trace...");
				this.callgraphBuilder = new DynamicCallgraphBuilder(
						manager.getNewestClientHistory().getCallgraph(),
						codePositionManager,
						codeIndexer);
			}
			
			// We need a dynamic callgraph
			if (this.callgraphBuilder == null)
				throw new RuntimeException("This should never happen. There is no such exception. "
						+ "It's all just an illusion. Move along.");
		}
		
		return manager;
	}		
	
	public DynamicCallgraphBuilder getDynamicCallgraph() {
		return this.callgraphBuilder;
	}
	
	public CodePositionManager getCodePositionManager() {
		return this.codePositionManager;
	}
	
	
	public void setTargetReached(boolean targetReached) {
		result.setTargetReached(targetReached);
	}
	
	
	public DexFileManager getDexFileManager() {
		return this.dexFileManager;
	}
	
	
	public AnalysisTaskManager getAnalysisTaskManager() {
		return this.analysisTaskManager;
	}
	
	
	private void tryStartingApp() {
		boolean hasLaunchableActivity = manifest.getLaunchableActivities().size() > 0;
		String packageName = manifest.getPackageName();
		if(hasLaunchableActivity) {
			eventManager.startApp(packageName);
		}
		//if there is no launchable activity, we try calling the first activity in manifest
		else if(manifest.getActivities().size() > 0){
			AXmlNode node = manifest.getActivities().iterator().next();
			String activityName = (String)node.getAttribute("name").getValue();
			
			eventManager.startActivity(packageName, activityName);
		}
		//if there is no launchable activity and no activity at all, we try calling the first service in manifest
		else if(manifest.getServices().size() > 0) {			
			AXmlNode node = manifest.getServices().iterator().next();
			String serviceName = (String)node.getAttribute("name").getValue();
			
			eventManager.startService(packageName, serviceName);
		}
		else
			throw new RuntimeException("we are not able to start the application");
	}
	
	
	public DecisionMakerConfig getConfig() {
		return this.config;
	}
	
	
	public void SIMPLE_START_APP_OR_START_APP_AND_INIT_EVENT_EVALUATION_CASE(FrameworkEvent event) {
		long startingTime = System.currentTimeMillis();
		// Create a new result object
		result = new EnvironmentResult();		
		//pull files onto device
		eventManager.installApp(manifest.getPackageName());	
		tryStartingApp();
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		};	
		
		if(event != null)
			eventManager.sendEvent(event);
		boolean trying = true;
		while(trying && !result.isTargetReached()) {								
			long currentTime = System.currentTimeMillis();
			long timeDiff = currentTime - socketServer.getLastRequestProcessed();
			
			// we do a complete (clean) re-install of the app
			if(timeDiff > FrameworkOptions.inactivityTimeout * 1000 ||
					(currentTime - startingTime) > (FrameworkOptions.forceTimeout * 1000)) {

				if (result.getRestartCount() < FrameworkOptions.maxRestarts || FrameworkOptions.maxRestarts == -1) {						
					LoggerHelper.logEvent(MyLevel.RESTART, String.format("Restarted app due to timeout: %d", result.getRestartCount()+1));
					LoggerHelper.logEvent(MyLevel.RESTART, String.format("timeDiff: %d\ncurr - starting: %d", timeDiff, (currentTime - startingTime)));

					eventManager.killAppProcess(manifest.getPackageName());
					eventManager.uninstallAppProcess(manifest.getPackageName());
					
					//wait a couple of seconds...
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					};
					
					
					// Reset our internal state
					callgraphBuilder = null;
					geneticOnlyMode = false;
					result.setRestartCount(result.getRestartCount() + 1);
					socketServer.notifyAppRunDone();
					
					eventManager.installApp(manifest.getPackageName());
					startingTime = System.currentTimeMillis();
					tryStartingApp();
					
					try {
						Thread.sleep(3000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					};
					
					//send events
					if(event != null)
						eventManager.sendEvent(event);
				} else {
					LoggerHelper.logEvent(MyLevel.RUNTIME, "Maximum number of restarts reached -- giving up.");
					trying = false;
				}
			}
		}
		
		// Make sure to clean up after ourselves
		eventManager.killAppProcess(manifest.getPackageName());
		eventManager.uninstallAppProcess(manifest.getPackageName());
	}
	
}
