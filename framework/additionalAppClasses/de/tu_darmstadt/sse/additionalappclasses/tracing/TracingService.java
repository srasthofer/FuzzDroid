package de.tu_darmstadt.sse.additionalappclasses.tracing;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import de.tu_darmstadt.sse.sharedclasses.SharedClassesSettings;
import de.tu_darmstadt.sse.sharedclasses.networkconnection.IClientRequest;
import de.tu_darmstadt.sse.sharedclasses.networkconnection.ServerCommunicator;
import de.tu_darmstadt.sse.sharedclasses.tracing.TraceItem;


public class TracingService extends Service {
	
	private final static int THREAD_PRIORITY_BACKGROUND = 10;
	private final static int QUEUE_DUMP_LIMIT = 25;
	private final static int DUMP_TIMEOUT = 1000;
	
	private boolean DUMPER_STARTED = false;
	
	public final static String ACTION_ENQUEUE_TRACE_ITEM = "enqueueTraceItem";
	public final static String ACTION_DUMP_QUEUE = "dumpQueue";
	public final static String ACTION_DUMP_QUEUE_SYNCHRONOUS = "dumpQueueSynchronous";
	public final static String ACTION_NULL = "null";
	
	public final static String EXTRA_ITEM_TYPE = "itemType";
	public final static String EXTRA_TRACE_ITEM = "traceItem";
	
	public final static String ITEM_TYPE_PATH_TRACKING = "pathTrackingItem";
	
	private final Queue<TraceItem> traceQueue = new LinkedBlockingQueue<>();
	
	protected HandlerDumpQueue handlerDumpQueue;
	
	private ServerCommunicator communicator = new ServerCommunicator(this);
	
	public TracingService() {
		super();

		Log.i(SharedClassesSettings.TAG_TS, "TracingService created.");			
	}
	
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(SharedClassesSettings.TAG_TS, "TracingService action started.");			
		
		// If we have not started our communication with the remote server yet,
		// we do this now
		if (!DUMPER_STARTED) {
			DUMPER_STARTED = true;
			startDumper();
		}
		
		// Process the current command
		if (intent.getAction().equals(ACTION_ENQUEUE_TRACE_ITEM)
				&& intent.hasExtra(EXTRA_ITEM_TYPE)
				&& intent.hasExtra(EXTRA_TRACE_ITEM)) {
			String itemType = intent.getStringExtra(EXTRA_ITEM_TYPE);
			switch (itemType) {
				case ITEM_TYPE_PATH_TRACKING:
					TraceItem ti = intent.getParcelableExtra(EXTRA_TRACE_ITEM);
					enqueueTraceItem(ti);
					break;
			}
		}
		else if (intent.getAction().equals(ACTION_DUMP_QUEUE))
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					Log.i(SharedClassesSettings.TAG, "Flushing queue, explicit request...");
					dumpQueue();
				}
				
			}).start();
		else if (intent.getAction().equals(ACTION_DUMP_QUEUE_SYNCHRONOUS)) {
			Log.i(SharedClassesSettings.TAG_TS, "Flushing queue (blocking mode), "
					+ "explicit request...");
			dumpQueue(true);
		}
		else if (intent.getAction().equals(ACTION_NULL)) {
			// Do nothing. This action is mainly used for later binding to the
			// service.
			Log.i(SharedClassesSettings.TAG_TS, "Starting TracingService...");
		}
		else
			Log.e(SharedClassesSettings.TAG_TS, String.format("Invalid action: %s",
					intent.getAction()));
		
		Log.i(SharedClassesSettings.TAG_TS, "TracingService action done.");
		return START_STICKY;
	}
	
	
	private class HandlerDumpQueue extends Handler {
		
		private final Runnable dumpRunnable = new Runnable() {

			@Override
			public void run() {
				// If the trace queue is longer than the limit, we dump it
				if (traceQueue.size() > QUEUE_DUMP_LIMIT) {
					Log.i(SharedClassesSettings.TAG_TS, "Flushing queue, size limit exceeded...");
					dumpQueue();
				}
				else
					Log.i(SharedClassesSettings.TAG_TS, "Tracing queue is at " + traceQueue.size() + " elements");
				postDelayed(dumpRunnable, DUMP_TIMEOUT);
			}
			
		};
		
		public HandlerDumpQueue(Looper looper) {
			super(looper);
			postDelayed(dumpRunnable, DUMP_TIMEOUT);
		}
		
	}
	
	
	private void startDumper() {
		HandlerThread thread = new HandlerThread("ServiceDumpTraceQueue",
				THREAD_PRIORITY_BACKGROUND);
		thread.start();
		handlerDumpQueue = new HandlerDumpQueue(thread.getLooper());
	}
	
	
	private void dumpQueue() {
		dumpQueue(false);
	}
	
	
	private void dumpQueue(boolean waitForReturn) {
		Log.i(SharedClassesSettings.TAG_TS, "Flushing the queue of " + traceQueue.size()
				+ " items in thread " + Thread.currentThread().getId());
		
		// Dump the current contents of the queue
		List<IClientRequest> items = new ArrayList<>(traceQueue.size());
		while (!traceQueue.isEmpty()) {
			TraceItem ti = traceQueue.poll();
			if (ti == null)
				break;
			items.add(ti);
		}
		communicator.send(items, waitForReturn);
	}
	
	
	class TracingServiceBinder extends Binder {
		
		
		public ITracingServiceInterface getService() {
			return new ITracingServiceInterface() {
				
				@Override
				public void dumpQueue() {
					Log.i(SharedClassesSettings.TAG_TS, "Flushing queue, explicit request "
							+ "through binder...");
					TracingService.this.dumpQueue(true);
				}
				
				@Override
				public void enqueueTraceItem(TraceItem ti) {
					TracingService.this.enqueueTraceItem(ti);
				}
				
			};
		}
		
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return new TracingServiceBinder();
	}
	
	@SuppressLint("NewApi")
	@Override
	public void onDestroy() {
		Log.i(SharedClassesSettings.TAG_TS, "Destroying tracing service...");
		handlerDumpQueue.getLooper().quitSafely();
	}
	
	
	public void enqueueTraceItem(TraceItem item) {
		traceQueue.add(item);
	}

}
