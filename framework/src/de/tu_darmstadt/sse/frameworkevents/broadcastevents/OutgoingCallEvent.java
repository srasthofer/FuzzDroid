package de.tu_darmstadt.sse.frameworkevents.broadcastevents;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import com.android.ddmlib.IDevice;

import de.tu_darmstadt.sse.commandlinelogger.LoggerHelper;
import de.tu_darmstadt.sse.commandlinelogger.MyLevel;
import de.tu_darmstadt.sse.frameworkevents.FrameworkEvent;
import de.tu_darmstadt.sse.frameworkevents.GenericReceiver;


public class OutgoingCallEvent extends FrameworkEvent{

	@Override
	public Object onEventReceived(IDevice device) {
		String numberToCall = "";
		for(int i = 0; i < 8; i++)
			numberToCall += ThreadLocalRandom.current().nextInt(0, 9 + 1);
		String shellCmd = String.format("am broadcast -a android.intent.action.NEW_OUTGOING_CALL --es PHONE_NUMBER %s", numberToCall);		
		try {
			device.executeShellCommand(shellCmd, new GenericReceiver(), 10000, TimeUnit.MILLISECONDS);
			LoggerHelper.logEvent(MyLevel.ADB_EVENT, adbEventFormat(toString(), shellCmd));
		} catch (Exception e) {
			e.printStackTrace();
		}		
		return null;

	}

	@Override
	public String toString() {
		return "OutgoingCallEvent";
	}
}
