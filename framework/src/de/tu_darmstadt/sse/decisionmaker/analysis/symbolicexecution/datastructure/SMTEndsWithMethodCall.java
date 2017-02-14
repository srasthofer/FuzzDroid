package de.tu_darmstadt.sse.decisionmaker.analysis.symbolicexecution.datastructure;

public class SMTEndsWithMethodCall extends StringMethodCall{
	private final SMTValue stringValue;
	private final SMTValue stringEndsWith;
	
	public SMTEndsWithMethodCall(SMTValue stringValue, SMTValue stringEndsWith) {
		this.stringValue = stringValue;
		this.stringEndsWith = stringEndsWith;
	}
	
	public String toString() {
		return String.format("( EndsWith %s %s )", stringValue, stringEndsWith);
	}
}
