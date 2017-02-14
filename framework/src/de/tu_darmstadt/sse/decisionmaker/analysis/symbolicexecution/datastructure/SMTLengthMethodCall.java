package de.tu_darmstadt.sse.decisionmaker.analysis.symbolicexecution.datastructure;

public class SMTLengthMethodCall extends StringMethodCall{
	private final SMTValue stringValue;
	
	public SMTLengthMethodCall(SMTValue stringValue) {
		this.stringValue = stringValue;
	}
	
	public String toString() {
		return String.format("( Length %s )", stringValue.toString());
	}
}
