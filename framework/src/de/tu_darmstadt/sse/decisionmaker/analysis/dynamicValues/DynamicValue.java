package de.tu_darmstadt.sse.decisionmaker.analysis.dynamicValues;


public class DynamicValue {
	
	private final int codePosition;
	private final int paramIdx;
	
	
	public DynamicValue(int codePosition, int paramIdx) {
		this.codePosition = codePosition;
		this.paramIdx = paramIdx;
	}

	
	public int getCodePosition() {
		return this.codePosition;
	}
	
	
	public int getParamIdx() {
		return this.paramIdx;
	}
}
