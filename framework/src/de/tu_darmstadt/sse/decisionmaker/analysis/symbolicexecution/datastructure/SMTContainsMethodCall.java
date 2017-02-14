package de.tu_darmstadt.sse.decisionmaker.analysis.symbolicexecution.datastructure;

public class SMTContainsMethodCall extends StringMethodCall{
	private final SMTValue stringValue;
	private final SMTValue containsValue;
	
	public SMTContainsMethodCall(SMTValue stringValue, SMTValue containsValue) {
		this.stringValue = stringValue;
		this.containsValue = containsValue;
	}
	
	public String toString() {
		return String.format("( Contains %s %s )", stringValue, containsValue);
	}
}
