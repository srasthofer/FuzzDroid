package de.tu_darmstadt.sse.decisionmaker.analysis.symbolicexecution.datastructure;

public class SMTIndexOfMethodCall extends StringMethodCall{
	private final SMTValue stringValue;
	private final SMTValue indexOf;
	
	public SMTIndexOfMethodCall(SMTValue stringValue, SMTValue indexOf) {
		this.stringValue = stringValue;
		this.indexOf = indexOf;
	}
	
	public String toString() {
		return String.format("( Indexof %s %s )", stringValue.toString(), indexOf.toString());
	}
}
