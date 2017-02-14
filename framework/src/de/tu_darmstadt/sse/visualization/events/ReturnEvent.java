package de.tu_darmstadt.sse.visualization.events;

import de.tu_darmstadt.sse.sharedclasses.SharedClassesSettings;


public class ReturnEvent extends AbstractPathExecutionEvent{
	
	private final String returnValue;
	
	public ReturnEvent(int processId, long lastCodePosition, String methodSignature, String returnValue)
	{
		super(processId, lastCodePosition, methodSignature);
		this.returnValue = returnValue;
	}
	
	
	@Override
	public String toString()
	{
		return String.format("%s %s return-value: %s", SharedClassesSettings.RETURN_LABEL, getMethodSignature(), returnValue);
	}
	
	public String getReturnValue() {
		return returnValue;
	}

	public int hashCode()
	{
		int hashCode = 42;
		hashCode += getProcessId();
		hashCode += getLastCodePosition();
		hashCode += getMethodSignature().hashCode();
		hashCode += returnValue.hashCode();
		return hashCode;
	}
	
	public boolean equals(Object obj)
	{
		if (!(obj instanceof ReturnEvent))
            return false;
        if (obj == this)
            return true;

        ReturnEvent rhs = (ReturnEvent) obj;
        return this.getProcessId() == rhs.getProcessId() &&
        		this.getMethodSignature().equals(rhs.getMethodSignature()) &&
        		this.getLastCodePosition() == rhs.getLastCodePosition() &&
        		this.returnValue.equals(rhs.returnValue);
	}		
}
