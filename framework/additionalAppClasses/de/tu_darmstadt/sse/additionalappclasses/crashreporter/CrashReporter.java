package de.tu_darmstadt.sse.additionalappclasses.crashreporter;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Collections;

import android.util.Log;
import de.tu_darmstadt.sse.additionalappclasses.tracing.BytecodeLogger;
import de.tu_darmstadt.sse.sharedclasses.SharedClassesSettings;
import de.tu_darmstadt.sse.sharedclasses.crashreporter.CrashReportItem;
import de.tu_darmstadt.sse.sharedclasses.networkconnection.IClientRequest;
import de.tu_darmstadt.sse.sharedclasses.networkconnection.ServerCommunicator;


public class CrashReporter {

	private static final UncaughtExceptionHandler uch = new UncaughtExceptionHandler() {
		
		private ServerCommunicator communicator = new ServerCommunicator(this);
		
		@Override
		public void uncaughtException(Thread arg0, Throwable arg1) {
			Log.i(SharedClassesSettings.TAG, "Crash reporter started: " + arg1.toString()
					+ " at " + arg1.getStackTrace());
			if (arg1.getCause() != null)
				Log.i(SharedClassesSettings.TAG, "Cause: " + arg1.getCause().toString());
			if (arg1.getCause().getCause() != null)
				Log.i(SharedClassesSettings.TAG, "Cause 2: " + arg1.getCause().getCause().toString());
			if (arg1.getCause().getCause().getCause() != null)
				Log.i(SharedClassesSettings.TAG, "Cause 3: " + arg1.getCause().getCause().getCause().toString());
			
			// Make sure that we flush the trace items before we die
			BytecodeLogger.dumpTracingDataSynchronous();
			
			// Send the crash report
			CrashReportItem ci = new CrashReportItem(arg1.getMessage(),
					BytecodeLogger.getLastExecutedStatement());
			communicator.send(Collections.<IClientRequest>singleton(ci), true);
		}
		
	};
	
	
	public static void registerExceptionHandler() {
		Thread.setDefaultUncaughtExceptionHandler(uch);
	}
	
}
