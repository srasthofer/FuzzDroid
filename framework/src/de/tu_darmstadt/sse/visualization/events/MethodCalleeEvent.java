package de.tu_darmstadt.sse.visualization.events;

import de.tu_darmstadt.sse.sharedclasses.SharedClassesSettings;


public class MethodCalleeEvent extends AbstractPathExecutionEvent {
	
	public MethodCalleeEvent(int processId, long lastCodePosition, String methodSignature)
	{
		super(processId, lastCodePosition, methodSignature);
	}
		
	public String toString(){
		return String.format("%s %s", SharedClassesSettings.METHOD_CALLEE_LABEL, getMethodSignature());
	}	

	public int hashCode()
	{
		int hashCode = 42;
		hashCode += getProcessId();
		hashCode += getMethodSignature().hashCode();
		hashCode += getLastCodePosition();
		return hashCode;
	}
	
	public boolean equals(Object obj)
	{
		if (!(obj instanceof MethodCalleeEvent))
            return false;
        if (obj == this)
            return true;

        MethodCalleeEvent rhs = (MethodCalleeEvent) obj;
        return this.getProcessId() == rhs.getProcessId() &&
        		this.getMethodSignature().equals(rhs.getMethodSignature()) &&
        		this.getLastCodePosition() == rhs.getLastCodePosition();
	}
}
