package de.tu_darmstadt.sse.decisionmaker.analysis.symbolicexecution.datastructure;

public class SMTCharAtMethodCall extends StringMethodCall{
	private final SMTValue stringValue;
	private final SMTValue indexOfChar;
	
	public SMTCharAtMethodCall(SMTValue stringValue, SMTValue indexOfChar) {
		this.stringValue = stringValue;
		this.indexOfChar = indexOfChar;
	}
	
	public String toString() {
		return String.format("( CharAt %s %s )", stringValue, indexOfChar);
	}
}
