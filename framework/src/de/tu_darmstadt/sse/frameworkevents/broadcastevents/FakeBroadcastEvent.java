package de.tu_darmstadt.sse.frameworkevents.broadcastevents;

import java.util.concurrent.TimeUnit;

import com.android.ddmlib.IDevice;

import de.tu_darmstadt.sse.appinstrumentation.UtilInstrumenter;
import de.tu_darmstadt.sse.commandlinelogger.LoggerHelper;
import de.tu_darmstadt.sse.commandlinelogger.MyLevel;
import de.tu_darmstadt.sse.frameworkevents.FrameworkEvent;
import de.tu_darmstadt.sse.frameworkevents.GenericReceiver;


public class FakeBroadcastEvent extends FrameworkEvent{
	private final String receiverClassName;
	private final String actionName;
	private final String packageName;
	private final String mimeType;
	
	public FakeBroadcastEvent(String receiverClassName, String actionName, String mimeType, String packageName) {
		this.receiverClassName = receiverClassName;
		this.actionName = actionName;
		this.packageName = packageName;
		this.mimeType = mimeType;
	}
	
	
	@Override
	public Object onEventReceived(IDevice device) {		
		String shellCmd = prepareStartService();
		try {
			device.executeShellCommand(shellCmd, new GenericReceiver(), 10000, TimeUnit.MILLISECONDS);
			LoggerHelper.logEvent(MyLevel.ADB_EVENT, adbEventFormat(toString(), shellCmd));
		} catch (Exception e) {
//			LoggerHelper.logEvent(MyLevel.EXCEPTION_ANALYSIS, "not able to send a broadcast: " + e.getMessage());
			e.printStackTrace();
		}		
		return null;
	}
	
	@Override
	public boolean equals(Object obj) {           
		if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FakeBroadcastEvent other = (FakeBroadcastEvent) obj;
        
        String otherActionName= (other.actionName != null)? other.actionName : "10";
        String thisActionname = (this.actionName != null)? this.actionName : "10";
        String otherReceiverClassName = (other.receiverClassName != null)? other.receiverClassName : "20";
        String thisReceiverClassName = (this.receiverClassName != null)? this.receiverClassName : "20";
        String otherPackageName = (other.packageName != null)? other.packageName : "30";
        String thisPackageName = (this.packageName != null)? this.packageName : "30";
        String otherMimeType = (other.mimeType != null)? other.mimeType : "40";
        String thisMimeType = (this.mimeType != null)? this.mimeType : "40";
        
        if (!otherActionName.equals(thisActionname)) 	            
            return false;
        if(!otherReceiverClassName.equals(thisReceiverClassName))
        	return false;
        if(!otherPackageName.equals(thisPackageName))
        	return false;
        if(!otherMimeType.equals(thisMimeType))
        	return false;
        return true;
	}
	
	@Override
    public int hashCode() {
		int hashCode = 42;
		hashCode += (receiverClassName != null)? receiverClassName.hashCode() : 10;
		hashCode += (actionName != null)? actionName.hashCode() : 20;
		hashCode += (packageName != null)? packageName.hashCode() : 30; 
		hashCode += (mimeType != null)? mimeType.hashCode() : 40;
		return hashCode;
	}

	public String getActionName() {
		return actionName;
	}
	
	private String prepareStartService() {
		if(mimeType != null) {
			return String.format("am startservice --es \"className\" %s"
					+ " --es \"action\" %s --es \"task\" \"broadcast\" --es \"mimeType\" %s -n %s/%s", 
					receiverClassName, actionName, mimeType, packageName, UtilInstrumenter.COMPONENT_CALLER_SERVICE_HELPER);
		}
		else {
			return String.format("am startservice --es \"className\" %s"
				+ " --es \"action\" %s --es \"task\" \"broadcast\"  -n %s/%s", 
				receiverClassName, actionName, packageName, UtilInstrumenter.COMPONENT_CALLER_SERVICE_HELPER);
		}
	}

	@Override
	public String toString() {
		return "FakeBroadcastEvent";
	}


}
