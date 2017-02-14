package de.tu_darmstadt.sse.frameworkevents.broadcastevents;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.android.ddmlib.IDevice;

import de.tu_darmstadt.sse.commandlinelogger.LoggerHelper;
import de.tu_darmstadt.sse.commandlinelogger.MyLevel;
import de.tu_darmstadt.sse.frameworkevents.FrameworkEvent;
import de.tu_darmstadt.sse.frameworkevents.GenericReceiver;


public class RealBroadcastEvent extends FrameworkEvent{
	private final Map<String, Object> extras;
	private final String mimeType;
	private final String action;
	
	public RealBroadcastEvent(Map<String, Object> extras, String action, String mimeType) {
		this.extras = extras;
		this.action = action;
		this.mimeType = mimeType;
	}	
	
	@Override
	public Object onEventReceived(IDevice device) {
		String shellCmd = String.format("am broadcast %s -a %s %s", prepareExtras(extras), action, prepareMimeType(mimeType));		
		try {
			device.executeShellCommand(shellCmd, new GenericReceiver(), 10000, TimeUnit.MILLISECONDS);
			LoggerHelper.logEvent(MyLevel.ADB_EVENT, adbEventFormat(toString(), shellCmd));
		} catch (Exception e) {
			e.printStackTrace();
		}		
		return null;
	}
	
	private String prepareExtras(Map<String, Object> extras) {
		StringBuilder sb = new StringBuilder();
		if(extras == null || extras.keySet().isEmpty())
			return "";
		else {
			for(Map.Entry<String, Object> entry : extras.entrySet()) {
				if(entry.getValue() instanceof Boolean)
					sb.append(String.format(" --ez %s %s ", entry.getKey(), entry.getValue()));
				else if(entry.getValue() instanceof String)
					sb.append(String.format(" --es %s %s ", entry.getKey(), entry.getValue()));
				else if(entry.getValue() instanceof Integer)
					sb.append(String.format(" --ei %s %s ", entry.getKey(), entry.getValue()));
				else if(entry.getValue() instanceof Long)
					sb.append(String.format(" --el %s %s ", entry.getKey(), entry.getValue()));
				else if(entry.getValue() instanceof Float)
					sb.append(String.format(" --ef %s %s ", entry.getKey(), entry.getValue()));
			}
		}
		return sb.toString();
	}

	private String prepareMimeType(String mimeType) {
		if(mimeType == null)
			return "";
		else
			return String.format(" -t %s ", mimeType);
	}
	
	@Override
	public boolean equals(Object obj) {           
		if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RealBroadcastEvent other = (RealBroadcastEvent) obj;
        
        String otherMimeType = (other.mimeType != null)? other.mimeType : "10";
        String thisMimeType = (this.mimeType != null)? this.mimeType : "10";
        String otherAction = (other.action != null)? other.action : "20";
        String thisAction = (this.action != null)? this.action : "20";
        
        if (!otherMimeType.equals(thisMimeType)) 	            
            return false;
        if(!otherAction.equals(thisAction))
        	return false;
        if(other.extras != this.extras)
        	return false;
        return true;
	}
	
	@Override
    public int hashCode() {
		int hashCode = 42;
		hashCode += (mimeType != null)? mimeType.hashCode() : 10;
		hashCode += (action != null)? action.hashCode() : 20;
		hashCode += (extras != null)? extras.hashCode() : 30;
		return hashCode;
	}
	
	@Override
	public String toString() {
		return "RealBroadcastEvent";
	}
}
