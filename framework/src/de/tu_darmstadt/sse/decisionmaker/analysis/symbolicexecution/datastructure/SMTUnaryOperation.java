package de.tu_darmstadt.sse.decisionmaker.analysis.symbolicexecution.datastructure;


public class SMTUnaryOperation{
	public enum SMTUnaryOperator {Plus, Minus, Times, Divided, Modulo};
	
	private final SMTValue lhs;
	private final SMTValue rhs;
	private final SMTUnaryOperator operator;
	
	public SMTUnaryOperation(SMTUnaryOperator operator, SMTValue lhs, SMTValue rhs) {
		this.operator = operator;
		this.lhs = lhs;
		this.rhs = rhs;
	}
	
	public String toString() {
		String stringOperator = null;
		switch(operator) {
		case Plus : stringOperator = "+"; break;
		case Minus : stringOperator = "-"; break;
		case Times : stringOperator = "*"; break;
		case Divided : stringOperator = "/"; break;
		case Modulo : stringOperator = "%"; break;
		}
		return String.format("(%s %s %s)", stringOperator, lhs, rhs);
	}
}
