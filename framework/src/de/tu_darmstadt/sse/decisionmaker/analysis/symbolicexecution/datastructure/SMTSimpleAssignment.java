package de.tu_darmstadt.sse.decisionmaker.analysis.symbolicexecution.datastructure;


public class SMTSimpleAssignment extends SMTAssignment{
	private final SMTValue rhs;
	
	public SMTSimpleAssignment(SMTBinding lhs, SMTValue rhs) {
		super(lhs);
		this.rhs = rhs;
	}
	
	@Override
	public SMTStatement getStatement() {
		return this;
	}
	
	public String toString() {
		return String.format("(= %s %s)", getLhs().getBinding(), rhs);
	}

}
