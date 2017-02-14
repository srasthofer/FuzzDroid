package de.tu_darmstadt.sse.decisionmaker.analysis.symbolicexecution.datastructure;

public class SMTConcatMethodCall extends StringMethodCall{
	private final SMTValue concatLhs;
	private final SMTValue concatRhs;
	
	public SMTConcatMethodCall(SMTValue concatLhs, SMTValue concatRhs) {
		this.concatLhs = concatLhs;
		this.concatRhs = concatRhs;
	}
	
	public String toString() {
		return String.format("( Concat %s %s )", concatLhs, concatRhs);
	}
}
