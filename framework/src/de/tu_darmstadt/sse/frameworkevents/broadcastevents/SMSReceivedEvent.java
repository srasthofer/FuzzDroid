package de.tu_darmstadt.sse.frameworkevents.broadcastevents;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import com.android.ddmlib.EmulatorConsole;
import com.android.ddmlib.IDevice;

import de.tu_darmstadt.sse.commandlinelogger.LoggerHelper;
import de.tu_darmstadt.sse.commandlinelogger.MyLevel;
import de.tu_darmstadt.sse.frameworkevents.FrameworkEvent;


public class SMSReceivedEvent extends FrameworkEvent{
	
	@Override
	public Object onEventReceived(IDevice device) {
		
		String senderNumber = "";
		for(int i = 0; i < 8; i++)
			senderNumber += ThreadLocalRandom.current().nextInt(0, 9 + 1);
		String message = UUID.randomUUID().toString().replace("-", "");
		
			
		EmulatorConsole emulatorConsole = EmulatorConsole.getConsole(device);
		if (emulatorConsole == null) {
			LoggerHelper.logEvent(MyLevel.ADB_EVENT, adbEventFormat(toString(),"ERROR: Could not send SMS"));
			return null;
		}

		emulatorConsole.sendSms(senderNumber, message);
		LoggerHelper.logEvent(MyLevel.ADB_EVENT, adbEventFormat(toString(), String.format("SMS received: Nr: %s | Msg: %s", senderNumber, message)));
		return null;
	}

	@Override
	public String toString() {
		return "SMSReceivedEvent";
	}
}
