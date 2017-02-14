package de.tu_darmstadt.sse.decisionmaker.analysis.symbolicexecution.datastructure;

public class SMTSubstringMethodCall extends StringMethodCall{
	private final SMTValue stringValue;
	private final SMTValue from;
	private final SMTValue to;
	
	public SMTSubstringMethodCall(SMTValue stringVlaue, SMTValue from, SMTValue to) {
		this.stringValue = stringVlaue;
		this.from = from;
		this.to = to;
	}
	
	public String toString() {
		return String.format("( Substring %s %s %s )", stringValue.toString(), from.toString(), to.toString());
	}
}
