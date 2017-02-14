package de.tu_darmstadt.sse.decisionmaker.analysis.symbolicexecution.datastructure;

public class SMTStartsWithMethodCall extends StringMethodCall{
	private final SMTValue stringValue;
	private final SMTValue startsWithValue;
	
	public SMTStartsWithMethodCall(SMTValue stringValue, SMTValue startsWithValue) {
		this.stringValue = stringValue;
		this.startsWithValue = startsWithValue;
	}
	
	public String toString() {
		return String.format("( StartsWith %s %s )", stringValue.toString(), startsWithValue.toString());
	}
}
