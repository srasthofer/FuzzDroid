package de.tu_darmstadt.sse.decisionmaker.analysis.symbolicexecution.datastructure;


public class SMTSimpleBinaryOperation implements SMTStatement{
	public enum SMTSimpleBinaryOperator {GT, LT};
	
	private final SMTValue lhs;
	private final SMTValue rhs;
	private final SMTSimpleBinaryOperator operator;
	
	public SMTSimpleBinaryOperation(SMTSimpleBinaryOperator operator, SMTValue lhs, SMTValue rhs) {
		this.operator = operator;
		this.lhs = lhs;
		this.rhs = rhs;
	}	
	
	public String toString() {
		String operatorString = null;
		switch(operator) {
		case GT : operatorString = ">"; break;
		case LT : operatorString = "<"; break;
		}
		
		return String.format("(%s %s %s)", operatorString, lhs, rhs);
	}
	
	@Override
	public SMTStatement getStatement() {
		return this;
	}
}
