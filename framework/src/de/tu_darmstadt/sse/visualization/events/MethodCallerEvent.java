package de.tu_darmstadt.sse.visualization.events;

import de.tu_darmstadt.sse.sharedclasses.SharedClassesSettings;


public class MethodCallerEvent extends AbstractPathExecutionEvent{
	
	private final String methodCalleeWithRuntimeValues;
	
	public MethodCallerEvent(int processId, long lastCodePosition, String methodSignature, String methodCalleeWithRuntimeValues)
	{
		super(processId, lastCodePosition, methodSignature);
		this.methodCalleeWithRuntimeValues = methodCalleeWithRuntimeValues;
	}
	
	@Override
	public String toString(){
		return String.format("%s %s -> %s", SharedClassesSettings.METHOD_CALLER_LABEL, 
				getMethodSignature(), methodCalleeWithRuntimeValues);
	}


	public int hashCode()
	{
		int hashCode = 42;
		hashCode += getProcessId();
		hashCode += getLastCodePosition();
		hashCode += getMethodSignature().hashCode();
		hashCode += methodCalleeWithRuntimeValues.hashCode();
		return hashCode;
	}
	
	public boolean equals(Object obj)
	{
		if (!(obj instanceof MethodCallerEvent))
            return false;
        if (obj == this)
            return true;

        MethodCallerEvent rhs = (MethodCallerEvent) obj;
        return this.getProcessId() == rhs.getProcessId() &&
        		this.getMethodSignature().equals(rhs.getMethodSignature()) &&
				this.methodCalleeWithRuntimeValues.equals(rhs.methodCalleeWithRuntimeValues) &&
        		this.getLastCodePosition() == rhs.getLastCodePosition();
	}
}
