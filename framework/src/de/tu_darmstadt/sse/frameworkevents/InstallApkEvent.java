package de.tu_darmstadt.sse.frameworkevents;

import com.android.ddmlib.IDevice;

import de.tu_darmstadt.sse.appinstrumentation.UtilInstrumenter;
import de.tu_darmstadt.sse.commandlinelogger.LoggerHelper;
import de.tu_darmstadt.sse.commandlinelogger.MyLevel;

public class InstallApkEvent extends FrameworkEvent{
	private final String packageName;
	
	public InstallApkEvent(String packageName) {
		this.packageName = packageName;
	}
	
	@Override
	public Object onEventReceived(IDevice device) {
		String deployedApkPath = UtilInstrumenter.SOOT_OUTPUT_DEPLOYED_APK;
		try {
			String res = device.installPackage(deployedApkPath, true);
			if (res != null && !res.isEmpty())
				LoggerHelper.logWarning("Not able to install apk " + packageName + ". Error message: " + res);
		} catch (Exception e) {
			LoggerHelper.logEvent(MyLevel.EXCEPTION_ANALYSIS, "Not able to install apk " + packageName);
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public String toString() {
		return "InstallAEvent";
	}

}
