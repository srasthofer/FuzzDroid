package de.tu_darmstadt.sse.decisionmaker.analysis.symbolicexecution.datastructure;

public class SMTUnaryOperationAssignment extends SMTAssignment{
	private final SMTUnaryOperation rhs;
	
	public SMTUnaryOperationAssignment(SMTBinding lhs, SMTUnaryOperation rhs) {
		super(lhs);
		this.rhs = rhs;
	}
	
	public String toString() {
		return String.format("(= %s %s )", getLhs().getBinding(), rhs);
	}
	
	
	@Override
	public SMTStatement getStatement() {
		return this;
	}
}
