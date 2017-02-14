package de.tu_darmstadt.sse.frameworkevents;

import java.util.concurrent.TimeUnit;

import com.android.ddmlib.IDevice;

import de.tu_darmstadt.sse.commandlinelogger.LoggerHelper;
import de.tu_darmstadt.sse.commandlinelogger.MyLevel;


public class StartActivityEvent extends FrameworkEvent{
	private final String packageName;
	private final String activityName;
	
	public StartActivityEvent(String packageName, String activityName) {
		this.packageName = packageName;
		this.activityName = activityName;
	}
	
	@Override
	public Object onEventReceived(IDevice device) {
		String shellCmd = String.format("am start -n %s/.%s", packageName, activityName);
		try {
			device.executeShellCommand(shellCmd, new GenericReceiver(), 10000, TimeUnit.MILLISECONDS);
			LoggerHelper.logEvent(MyLevel.START_ACTIVITY, String.format("started activity %s/%s", packageName, activityName));
		} catch (Exception e) {
			LoggerHelper.logEvent(MyLevel.EXCEPTION_ANALYSIS, String.format("not able to start activity %s/%s: ", packageName, activityName, e.getMessage()));
			e.printStackTrace();
		}		
		return null;
	}
	
	@Override
	public String toString() {
		return "StartActivityEvent";
	}

}
