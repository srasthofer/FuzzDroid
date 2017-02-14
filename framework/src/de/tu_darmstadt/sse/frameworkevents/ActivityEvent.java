package de.tu_darmstadt.sse.frameworkevents;

import java.util.concurrent.TimeUnit;

import com.android.ddmlib.IDevice;

import de.tu_darmstadt.sse.commandlinelogger.LoggerHelper;
import de.tu_darmstadt.sse.commandlinelogger.MyLevel;


public class ActivityEvent extends FrameworkEvent{
	private final String packageName;
	private final String activityPath;
	
	public ActivityEvent(String packageName, String activityPath) {
		this.packageName = packageName;
		this.activityPath = activityPath;
	}
	
	@Override
	public Object onEventReceived(IDevice device) {
		//just start the onCreate for now; we do not care whether the bundle is null
		String shellCmd = String.format("am start -W -n %s/%s", packageName, activityPath);		
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
        ActivityEvent other = (ActivityEvent) obj;
        
        String otherPackageName = (other.packageName != null)? other.packageName : "10";
        String thisPackageName = (this.packageName != null)? this.packageName : "10";
        String otherActivityPath = (other.activityPath != null)? other.activityPath : "20";
        String thisActivityPath = (this.activityPath != null)? this.activityPath : "20";
        
        if (!otherActivityPath.equals(thisActivityPath)) 	            
            return false;
        if(!otherPackageName.equals(thisPackageName))
        	return false;
        return true;
	}
	
	@Override
    public int hashCode() {
		int hashCode = 42;
		hashCode += packageName.hashCode();
		hashCode += activityPath.hashCode();
		return hashCode;
	}

	@Override
	public String toString() {
		return "ActivityEvent";
	}
}
