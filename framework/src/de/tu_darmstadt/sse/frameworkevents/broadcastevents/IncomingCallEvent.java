package de.tu_darmstadt.sse.frameworkevents.broadcastevents;

import java.util.concurrent.ThreadLocalRandom;

import com.android.ddmlib.EmulatorConsole;
import com.android.ddmlib.IDevice;

import de.tu_darmstadt.sse.commandlinelogger.LoggerHelper;
import de.tu_darmstadt.sse.commandlinelogger.MyLevel;
import de.tu_darmstadt.sse.frameworkevents.FrameworkEvent;


public class IncomingCallEvent extends FrameworkEvent{
	@Override
	public Object onEventReceived(IDevice device) {
		String senderNumber = "";
		for(int i = 0; i < 8; i++)
			senderNumber += ThreadLocalRandom.current().nextInt(0, 9 + 1);
		
		EmulatorConsole emulatorConsole = EmulatorConsole.getConsole(device);
		LoggerHelper.logEvent(MyLevel.ADB_EVENT, adbEventFormat(toString(), String.format("incomingCall(%s)", senderNumber)));
		//call a random number
		emulatorConsole.call(senderNumber);
		//let it ring for 3 seconds
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		//cancel call
		emulatorConsole.cancelCall(senderNumber);
		return null;
	}
	
	@Override
	public String toString() {
		return "IncomingCallEvent";
	}
}
