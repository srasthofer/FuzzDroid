package de.tu_darmstadt.sse.frameworkevents;

import java.util.concurrent.TimeUnit;

import com.android.ddmlib.IDevice;

import de.tu_darmstadt.sse.appinstrumentation.UtilInstrumenter;
import de.tu_darmstadt.sse.commandlinelogger.LoggerHelper;
import de.tu_darmstadt.sse.commandlinelogger.MyLevel;


public class OnClickEvent extends FrameworkEvent{
	private final String onClickListenerClass;
	private final String packageName;
	
	public OnClickEvent(String onClickListenerClass, String packageName) {
		//in case of an inner class, we have to escape the $ sign
		this.onClickListenerClass = onClickListenerClass.replace("$", "\\$");
		this.packageName = packageName;
	}
	
	@Override
	public Object onEventReceived(IDevice device) {		
		String shellCmd = String.format("am startservice --es \"className\" \"%s\""
				+ " --es \"task\" \"onClick\" -n %s/%s", 
				onClickListenerClass, packageName, UtilInstrumenter.COMPONENT_CALLER_SERVICE_HELPER);		
		try {
			device.executeShellCommand(shellCmd, new GenericReceiver(), 10000, TimeUnit.MILLISECONDS);
			LoggerHelper.logEvent(MyLevel.ADB_EVENT, adbEventFormat(toString(), onClickListenerClass));
		} catch (Exception e) {
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
        OnClickEvent other = (OnClickEvent) obj;
        
        String otherOnClickListenerClass = (other.onClickListenerClass != null)? other.onClickListenerClass : "10";
        String thisOnClickListenerClass = (this.onClickListenerClass != null)? this.onClickListenerClass : "10";
        String otherPackageName = (other.packageName != null)? other.packageName : "20";
        String thisPackageName = (this.packageName != null)? this.packageName : "20";
        
        if (!otherOnClickListenerClass.equals(thisOnClickListenerClass)) 	            
            return false;
        if(!thisPackageName.equals(otherPackageName))
        	return false;
        return true;
	}
	
	@Override
    public int hashCode() {
		int hashCode = 42;
		hashCode += (packageName != null)? packageName.hashCode() : 10;
		hashCode += (onClickListenerClass != null)? onClickListenerClass.hashCode() : 20;
		return hashCode;
	}
	
	@Override
	public String toString() {
		return "(Fake) OnClickEvent";
	}

}
