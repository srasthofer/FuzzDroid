package de.tu_darmstadt.sse.frameworkevents;

import com.android.ddmlib.IDevice;


public abstract class FrameworkEvent {
	public abstract Object onEventReceived(IDevice device);
	
	public String adbEventFormat(String eventType, String eventCmd) {
		return String.format("%s || %s", eventType, eventCmd);
	}
}
