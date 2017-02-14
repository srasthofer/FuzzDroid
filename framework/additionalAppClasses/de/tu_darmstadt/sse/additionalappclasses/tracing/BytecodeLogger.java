package de.tu_darmstadt.sse.additionalappclasses.tracing;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Parcelable;
import android.util.Log;
import de.tu_darmstadt.sse.additionalappclasses.hooking.Hooker;
import de.tu_darmstadt.sse.additionalappclasses.tracing.TracingService.TracingServiceBinder;
import de.tu_darmstadt.sse.sharedclasses.SharedClassesSettings;
import de.tu_darmstadt.sse.sharedclasses.dynamiccfg.MethodCallItem;
import de.tu_darmstadt.sse.sharedclasses.dynamiccfg.MethodEnterItem;
import de.tu_darmstadt.sse.sharedclasses.dynamiccfg.MethodLeaveItem;
import de.tu_darmstadt.sse.sharedclasses.dynamiccfg.MethodReturnItem;
import de.tu_darmstadt.sse.sharedclasses.networkconnection.IClientRequest;
import de.tu_darmstadt.sse.sharedclasses.networkconnection.ServerCommunicator;
import de.tu_darmstadt.sse.sharedclasses.tracing.DexFileTransferTraceItem;
import de.tu_darmstadt.sse.sharedclasses.tracing.DynamicIntValueTraceItem;
import de.tu_darmstadt.sse.sharedclasses.tracing.DynamicStringValueTraceItem;
import de.tu_darmstadt.sse.sharedclasses.tracing.FileBasedTracingUtils;
import de.tu_darmstadt.sse.sharedclasses.tracing.PathTrackingTraceItem;
import de.tu_darmstadt.sse.sharedclasses.tracing.TargetReachedTraceItem;
import de.tu_darmstadt.sse.sharedclasses.tracing.TimingBombTraceItem;
import de.tu_darmstadt.sse.sharedclasses.tracing.TraceItem;


public class BytecodeLogger {
	
	private static final Queue<TraceItem> bootupQueue = new LinkedBlockingQueue<>();
	private static ITracingServiceInterface tracingService = null;
	private static ServiceConnection tracingConnection = new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			tracingService = null;
			Log.i(SharedClassesSettings.TAG, "Tracing service disconnected");
		}
		
		@Override
		public void onServiceConnected(ComponentName arg0, IBinder serviceBinder) {
			Log.i(SharedClassesSettings.TAG, "Tracing service connected");
			if (serviceBinder == null) {
				Log.e(SharedClassesSettings.TAG, "Got a null binder. Shitaki.");
				return;				
			}
			
			try {
				tracingService = ((TracingServiceBinder) serviceBinder).getService();
			}
			catch (RuntimeException ex) {
				Log.e(SharedClassesSettings.TAG, "Could not get tracing service: "
						+ ex.getMessage());	
			}
		}
		
	};
	
	BytecodeLogger() {
		// this constructor shall just prevent external code from instantiating
		// this class
	}
	
	
	public static void initialize(final Context context) {
		// Start the service in its own thread to avoid an ANR
		if (tracingService == null) {
			Thread initThread = new Thread() {
				
				@Override
				public void run() {
					if (tracingService == null) {
						Log.i(SharedClassesSettings.TAG, "Binding to tracing service...");
						Intent serviceIntent = new Intent(context, TracingService.class);
						serviceIntent.setAction(TracingService.ACTION_NULL);
						context.startService(serviceIntent);
						if (context.bindService(serviceIntent, tracingConnection, Context.BIND_AUTO_CREATE))
							Log.i(SharedClassesSettings.TAG, "Tracing service bound.");
						else
							Log.i(SharedClassesSettings.TAG, "bindService() returned false.");
					}
				}
				
			};
			initThread.start();
		}
	}
	
	private static ThreadLocal<Integer> lastExecutedStatement = new ThreadLocal<Integer>() {
		
		@Override
		protected Integer initialValue() {
			return -1;
		}
		
	};
	
	
	private static int globalLastExecutedStatement;
		
	
	public static void setLastExecutedStatement(int statementID) {
		lastExecutedStatement.set(statementID);
		globalLastExecutedStatement = statementID;
	}
	
	
	public static int getLastExecutedStatement() {
		return lastExecutedStatement.get();
	}
	
	
	private static Context getAppContext() {
		if(Hooker.applicationContext != null)
			return Hooker.applicationContext;
		try {
		    Class<?> activityThreadClass =
		            Class.forName("android.app.ActivityThread");
		    Method method = activityThreadClass.getMethod("currentApplication");
		    Context app = (Context) method.invoke(null, (Object[]) null);
			
		    if (app == null) {
		    	Class<?> appGlobalsClass = Class.forName("android.app.AppGlobals");
		    	method = appGlobalsClass.getMethod("getInitialApplication");
		    	app = (Context) method.invoke(null, (Object[]) null);
		    }
		    
		    return app;
		}
		catch (Exception ex) {
			throw new RuntimeException("Could not get context");
		}
	}
	
	
	public static void reportConditionOutcome(boolean decision) {
		reportConditionOutcome(decision, getAppContext());
	}
	
	
	public static void reportConditionOutcome(boolean decision, Context context) {			
		Intent serviceIntent = new Intent(context, TracingService.class);
		serviceIntent.setAction(TracingService.ACTION_ENQUEUE_TRACE_ITEM);
		serviceIntent.putExtra(TracingService.EXTRA_ITEM_TYPE,
				TracingService.ITEM_TYPE_PATH_TRACKING);
		serviceIntent.putExtra(TracingService.EXTRA_TRACE_ITEM, (Parcelable)
				new PathTrackingTraceItem(getLastExecutedStatement(), decision));
		context.startService(serviceIntent);
	}
	
	
	public static void reportConditionOutcomeSynchronous(boolean decision) {
		reportConditionOutcomeSynchronous(decision, getAppContext());
	}
	
	
	public static void reportConditionOutcomeSynchronous(boolean decision,
			Context context) {
		// Create the trace item to be enqueued
		TraceItem traceItem = new PathTrackingTraceItem(
				getLastExecutedStatement(), decision);
		sendTraceItemSynchronous(context, traceItem);
	}
	
	
	private static void flushBootupQueue() {
		if (tracingService == null || bootupQueue.isEmpty())
			return;
		
		synchronized (bootupQueue) {
			if (bootupQueue.isEmpty())
				return;
			
			// Flush it
			while (!bootupQueue.isEmpty()) {
				TraceItem ti = bootupQueue.poll();
				if (ti != null)
					tracingService.enqueueTraceItem(ti);
			}
		}
	}

	
	public static void dumpTracingData() {
		dumpTracingData(getAppContext());
	}
	
	
	public static void dumpTracingData(Context context) {
		Log.i(SharedClassesSettings.TAG, "Sending an intent to dump tracing data...");
		Intent serviceIntent = new Intent(context, TracingService.class);
		serviceIntent.setAction(TracingService.ACTION_DUMP_QUEUE);
		context.startService(serviceIntent);
		Log.i(SharedClassesSettings.TAG, "Tracing data dumped via intent");
	}

	
	public static void dumpTracingDataSynchronous() {
		dumpTracingDataSynchronous(getAppContext());
	}
	
	
	public static void dumpTracingDataSynchronous(Context context) {
		// If we don't have a service connection yet, we must directly send the
		// contents of our boot-up queue
		if (tracingService == null && !bootupQueue.isEmpty()) {
			Log.i(SharedClassesSettings.TAG, String.format("Flushing "
					+ "boot-up queue of %d elements...", bootupQueue.size()));
			ServerCommunicator communicator = new ServerCommunicator(bootupQueue);
			List<IClientRequest> items = new ArrayList<>(bootupQueue.size());
			while (!bootupQueue.isEmpty()) {
				TraceItem ti = bootupQueue.poll();
				if (ti == null)
					break;
				items.add(ti);
			}
			communicator.send(items, true);
			Log.i(SharedClassesSettings.TAG, "All elements in queue sent.");
			return;
		}
		else {
			// If we have a service connection, we must make sure to flush the
			// trace items we accumulated during boot-up
			flushBootupQueue();
		}
		
		try {
			Log.i(SharedClassesSettings.TAG, "Dumping trace queue on binder...");
			tracingService.dumpQueue();
			Log.i(SharedClassesSettings.TAG, "Done.");
		}
		catch (RuntimeException ex) {
			Log.e(SharedClassesSettings.TAG, "Binder communication failed: "
					+ ex.getMessage());
		}
	}

	
	public static void reportMethodCallSynchronous(int codePosition) {
		reportMethodCallSynchronous(codePosition, getAppContext());
	}
	
	
	public static void reportMethodCallSynchronous(int codePosition, Context context) {
		sendTraceItemSynchronous(context, new MethodCallItem(codePosition));
	}
	
	
	public static void reportMethodReturnSynchronous(int codePosition) {
		reportMethodReturnSynchronous(codePosition, getAppContext());
	}
	
	
	public static void reportMethodReturnSynchronous(int codePosition, Context context) {			
		sendTraceItemSynchronous(context, new MethodReturnItem(codePosition));
	}
	
	
	public static void reportMethodEnterSynchronous(int codePosition) {
		reportMethodEnterSynchronous(codePosition, getAppContext());
	}
	
	
	public static void reportMethodEnterSynchronous(int codePosition, Context context) {			
		sendTraceItemSynchronous(context, new MethodEnterItem(codePosition));
	}
	
	
	public static void reportMethodLeaveSynchronous(int codePosition) {
		reportMethodLeaveSynchronous(codePosition, getAppContext());
	}
	
	
	public static void reportMethodLeaveSynchronous(int codePosition, Context context) {
		sendTraceItemSynchronous(context, new MethodLeaveItem(codePosition));
	}
	
	
	public static void reportTargetReachedSynchronous() {
		reportTargetReachedSynchronous(getAppContext());
	}
	
	
	public static void reportTargetReachedSynchronous(Context context) {			
		Log.i(SharedClassesSettings.TAG, "Target location has been reached.");
		
		sendTraceItemSynchronous(context, new TargetReachedTraceItem(
				getLastExecutedStatement()));
		
		// This is usually the end of the analysis, so make sure to get our
		// data out
		dumpTracingDataSynchronous(context);
	}
	

	public static void sendDexFileToServer(String dexFileName, byte[] dexFile) {
		sendDexFileToServer(dexFileName, dexFile, getAppContext());
	}
	
	
	public static void sendDexFileToServer(String dexFileName, byte[] dexFile, Context context) {
		// Since dex files can be large and we need to make sure that they are
		// sent even if the app crashes afterwards, we write them to disk. The
		// separate watchdog app will pick them up there.
		TraceItem ti = new DexFileTransferTraceItem(dexFileName, dexFile,
				getLastExecutedStatement(), globalLastExecutedStatement);
		Log.i(SharedClassesSettings.TAG, "Writing dex file of " + dexFile.length
				+ " bytes at location " + getLastExecutedStatement()
				+ " (" + ti.getLastExecutedStatement() + " in object)");
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;
		try {
			// Create the target directory
			File storageDir = FileBasedTracingUtils.getFuzzerDirectory();
			
			// Serialize the object
			File targetFile;
			try {
				targetFile = File.createTempFile("evofuzz", ".dat", storageDir);
				fos = new FileOutputStream(targetFile);
				oos = new ObjectOutputStream(fos);
				oos.writeObject(ti);
				Log.i(SharedClassesSettings.TAG, "Dex file written to disk for watchdog");
			} catch (IOException e) {
				// ignore it, we can't really do much about it
				Log.e(SharedClassesSettings.TAG, "Could not write serialized trace item to disk: "
						+ e.getMessage());
			}
		}
		finally {
			if (oos != null)
				try {
					oos.close();
				} catch (IOException e1) {
					// ignore it, there's little we can do
					Log.e(SharedClassesSettings.TAG, "Could not close object stream");
				}
			if (fos != null)
				try {
					fos.close();
				} catch (IOException e) {
					// ignore it, there's little we can do
					Log.e(SharedClassesSettings.TAG, "Could not close file stream");
				}
		}
	}
	
	
	public static void reportDynamicValue(String dynamicValue, int paramIdx) {
		reportDynamicValue(getAppContext(), dynamicValue, paramIdx);
	}
	
	
	public static void reportDynamicValue(Context context, String dynamicValue,
			int paramIdx) {
		if (dynamicValue != null && dynamicValue.length() > 0) {
			sendTraceItemSynchronous(context, new DynamicStringValueTraceItem(
					dynamicValue, paramIdx, getLastExecutedStatement()));
		}
	}
	
	
	public static void reportDynamicValue(int dynamicValue, int paramIdx) {
		reportDynamicValue(getAppContext(), dynamicValue, paramIdx);
	}
	
	
	public static void reportDynamicValue(Context context, int dynamicValue,
			int paramIdx) {
		sendTraceItemSynchronous(context, new DynamicIntValueTraceItem(
				dynamicValue, paramIdx, getLastExecutedStatement()));
	}

	
	public static void reportTimingBomb(long originalValue, long newValue) {
		reportTimingBomb(getAppContext(), originalValue, newValue);
	}
	
	
	public static void reportTimingBomb(Context context, long originalValue, long newValue) {
		sendTraceItemSynchronous(context, new TimingBombTraceItem(originalValue, newValue));		
	}
	
	
	public static void sendTraceItemSynchronous(Context context,
			TraceItem traceItem) {
		// If we don't have a service connection yet, we use our own boot-up
		// queue
		if (tracingService == null) {
			bootupQueue.add(traceItem);
			return;
		}
		else {
			// If we have a service connection, we must make sure to flush the
			// trace items we accumulated during boot-up
			flushBootupQueue();
		}
		
		try {
			tracingService.enqueueTraceItem(traceItem);
		}
		catch (RuntimeException ex) {
			Log.e(SharedClassesSettings.TAG, "Binder communication failed: "
					+ ex.getMessage());
		}
	}
	
}
