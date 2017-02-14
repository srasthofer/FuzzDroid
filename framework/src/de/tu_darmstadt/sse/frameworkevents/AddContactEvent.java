package de.tu_darmstadt.sse.frameworkevents;

import java.util.concurrent.TimeUnit;

import com.android.ddmlib.IDevice;

import de.tu_darmstadt.sse.appinstrumentation.UtilInstrumenter;
import de.tu_darmstadt.sse.commandlinelogger.LoggerHelper;
import de.tu_darmstadt.sse.commandlinelogger.MyLevel;


public class AddContactEvent extends FrameworkEvent{

	private final String packageName;
	
	public AddContactEvent(String packageName) {
		this.packageName = packageName;
	}
	
	@Override
	public Object onEventReceived(IDevice device) {
		String shellCmd = String.format("am startservice --es \"task\" \"addContact\" -n %s/%s", 
				packageName, UtilInstrumenter.COMPONENT_CALLER_SERVICE_HELPER);		
		try {
			device.executeShellCommand(shellCmd, new GenericReceiver(), 10000, TimeUnit.MILLISECONDS);
			LoggerHelper.logEvent(MyLevel.ADB_EVENT, adbEventFormat(toString(), "contact added..."));
		} catch (Exception e) {
			e.printStackTrace();
		}		
		return null;
	}

	public String toString() {
		return "AddContactEvent";
	}
}
