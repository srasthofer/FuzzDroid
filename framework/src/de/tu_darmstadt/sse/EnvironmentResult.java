package de.tu_darmstadt.sse;


public class EnvironmentResult {
	
	private int restartCount = 0;
	private boolean targetReached = false;
	
	public EnvironmentResult() {
		
	}
	
	
	public int getRestartCount() {
		return restartCount;
	}
	
	public void setRestartCount(int count) {
		this.restartCount = count;
	}
	
	
	public boolean isTargetReached() {
		return this.targetReached;
	}
	
	public void setTargetReached(boolean targetReached) {
		this.targetReached = targetReached;
	}
	
}
