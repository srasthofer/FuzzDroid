package de.tu_darmstadt.sse.decisionmaker.analysis.symbolicexecution.datastructure;

public class SMTBooleanEqualsAssignment extends SMTAssignment{
	private final SMTValue innerLHS;
	private final SMTValue innerRHS;
	
	public SMTBooleanEqualsAssignment(SMTBinding outerLHS, SMTValue innerLHS, SMTValue innerRHS) {
		super(outerLHS);
		this.innerLHS = innerLHS;
		this.innerRHS = innerRHS;
	}
	
	public String toString() {
		return String.format("(= %s ( = %s %s ) )", getLhs().getBinding(), innerLHS, innerRHS);
	}

	@Override
	public SMTStatement getStatement() {
		return this;
	}
}
