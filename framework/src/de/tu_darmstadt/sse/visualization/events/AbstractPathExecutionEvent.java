package de.tu_darmstadt.sse.visualization.events;


public abstract class AbstractPathExecutionEvent {
	
	private final int processId;
	
	private final long lastCodePosition;
	
	private final String methodSignature;
	
	public AbstractPathExecutionEvent(int processId, long lastCodePosition, String methodSignature) {
		this.processId = processId;
		this.lastCodePosition = lastCodePosition;
		this.methodSignature = methodSignature;
	}
			
	public abstract String toString();
	public abstract int hashCode();
	public abstract boolean equals(Object o);

	public int getProcessId() {
		return processId;
	}

	public long getLastCodePosition() {
		return lastCodePosition;
	}

	public String getMethodSignature() {
		return methodSignature;
	}		
}
