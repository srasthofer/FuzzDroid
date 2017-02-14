package de.tu_darmstadt.sse.bootstrap;


public class InstanceIndependentCodePosition {
	
	private String methodSignature;
	private int lineNumber;
	private String statement;
	
	public InstanceIndependentCodePosition(String methodSignature,
			int lineNumber, String statement) {
		this.methodSignature = methodSignature;
		this.lineNumber = lineNumber;
		this.statement = statement;
	}
	
	public String getMethodSignature() {
		return this.methodSignature;
	}
	
	public int getLineNumber() {
		return this.lineNumber;
	}
	
	public String getStatement() {
		return this.statement;
	}

}
