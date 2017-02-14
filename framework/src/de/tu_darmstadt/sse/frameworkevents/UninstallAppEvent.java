package de.tu_darmstadt.sse.frameworkevents;

import com.android.ddmlib.IDevice;
import com.android.ddmlib.InstallException;

import de.tu_darmstadt.sse.commandlinelogger.LoggerHelper;
import de.tu_darmstadt.sse.commandlinelogger.MyLevel;


public class UninstallAppEvent extends FrameworkEvent{
	private final String packageName;
	
	public UninstallAppEvent(String packageName) {
		this.packageName = packageName;
	}
	
	@Override
	public Object onEventReceived(IDevice device) {
		try {
			device.uninstallPackage(packageName);
		} catch (InstallException e) {
			LoggerHelper.logEvent(MyLevel.EXCEPTION_ANALYSIS, e.getMessage());
			e.printStackTrace();
			System.exit(-1);
		}
		return null;
	}
	
	@Override
	public String toString() {
		return "UninstallApkEvent";
	}

}
