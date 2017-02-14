package de.tu_darmstadt.sse.frameworkevents;

import com.android.ddmlib.IDevice;

import de.tu_darmstadt.sse.commandlinelogger.LoggerHelper;
import de.tu_darmstadt.sse.commandlinelogger.MyLevel;

public class PhoneRestartEvent extends FrameworkEvent{

	@Override
	public Object onEventReceived(IDevice device) {
		try {
			device.reboot(null);
			LoggerHelper.logEvent(MyLevel.RESTART, "App restarted event sent");
		} catch (Exception e) {
			LoggerHelper.logEvent(MyLevel.EXCEPTION_ANALYSIS, "Not able to reboot device...: " + e.getMessage());
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public String toString() {
		return "PhoneRestartEvent";
	}
}
