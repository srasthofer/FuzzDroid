package de.tu_darmstadt.sse.frameworkevents;

import java.util.concurrent.TimeUnit;

import com.android.ddmlib.IDevice;

import de.tu_darmstadt.sse.commandlinelogger.LoggerHelper;
import de.tu_darmstadt.sse.commandlinelogger.MyLevel;


public class ServiceEvent extends FrameworkEvent{
	private final String packageName;
	private final String servicePath;
	
	public ServiceEvent(String packageName, String servicePath) {
		this.packageName = packageName;
		this.servicePath = servicePath;
	}
	
	@Override
	public Object onEventReceived(IDevice device) {
		//just start the onCreate for now; we do not care whether the bundle is null
		String shellCmd = String.format("am startservice %s/%s", packageName, servicePath);		
		try {			
			device.executeShellCommand(shellCmd, new GenericReceiver(), 10000, TimeUnit.MILLISECONDS);
			LoggerHelper.logEvent(MyLevel.ADB_EVENT, adbEventFormat(toString(), shellCmd));
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
        ServiceEvent other = (ServiceEvent) obj;
        
        String otherServicePath = (other.servicePath != null)? other.servicePath : "10";
        String thisServicePath = (this.servicePath != null)? this.servicePath : "10";
        String otherPackageName = (other.packageName != null)? other.packageName : "20";
        String thisPackgeName = (this.packageName != null)? this.packageName : "20";
        
        if (!otherServicePath.equals(thisServicePath)) 	            
            return false;
        if(!otherPackageName.equals(thisPackgeName))
        	return false;
        return true;
	}
	
	@Override
    public int hashCode() {
		int hashCode = 42;
		hashCode += (packageName != null)? packageName.hashCode() : 10;
		hashCode += (servicePath != null)? servicePath.hashCode() : 20;
		return hashCode;
	}
	
	@Override
	public String toString() {
		return "ServiceStartEvent";
	}

}
