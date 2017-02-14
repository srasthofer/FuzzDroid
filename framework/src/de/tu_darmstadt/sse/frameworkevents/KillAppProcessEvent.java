package de.tu_darmstadt.sse.frameworkevents;

import java.util.concurrent.TimeUnit;

import com.android.ddmlib.IDevice;

import de.tu_darmstadt.sse.commandlinelogger.LoggerHelper;
import de.tu_darmstadt.sse.commandlinelogger.MyLevel;


public class KillAppProcessEvent extends FrameworkEvent{
	private final String packageName;
	
	public KillAppProcessEvent(String packageName) {
		this.packageName = packageName;
	}
	
	@Override
	public Object onEventReceived(IDevice device) {
		String shellCmd = String.format("am force-stop %s", packageName);
		try {
			device.executeShellCommand(shellCmd, new GenericReceiver(), 10000, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			LoggerHelper.logEvent(MyLevel.EXCEPTION_ANALYSIS, "not able to kill app: " + e.getMessage());
			e.printStackTrace();
		}		
		return null;
	}
	
	@Override
	public String toString() {
		return "KillApkEvent";
	}

}
