package de.tu_darmstadt.sse.frameworkevents;

import java.util.concurrent.TimeUnit;

import com.android.ddmlib.IDevice;

import de.tu_darmstadt.sse.commandlinelogger.LoggerHelper;
import de.tu_darmstadt.sse.commandlinelogger.MyLevel;

public class StartApkEvent extends FrameworkEvent{
	private final String packageName;
	
	public StartApkEvent(String packageName) {
		this.packageName = packageName;
	}
	
	@Override
	public Object onEventReceived(IDevice device) {
//		String shellCmd = String.format("am start -n %s/.%s", packageName, launchableActivity);
		String shellCmd = String.format("monkey -p %s -c android.intent.category.LAUNCHER 1", packageName);
		try {
			device.executeShellCommand(shellCmd, new GenericReceiver(), 10000, TimeUnit.MILLISECONDS);
			LoggerHelper.logEvent(MyLevel.OPEN_APK, "APK opened");
		} catch (Exception e) {
			LoggerHelper.logEvent(MyLevel.EXCEPTION_ANALYSIS, "not able to start apk: " + e.getMessage());
			e.printStackTrace();
		}		
		return null;
	}
	
	@Override
	public String toString() {
		return "StartApkEvent";
	}
}
