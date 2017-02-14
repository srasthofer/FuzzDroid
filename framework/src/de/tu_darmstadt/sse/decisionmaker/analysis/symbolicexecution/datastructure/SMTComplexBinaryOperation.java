package de.tu_darmstadt.sse.decisionmaker.analysis.symbolicexecution.datastructure;

import java.util.Set;


public class SMTComplexBinaryOperation implements SMTStatement{
	public enum SMTComplexBinaryOperator {OR};
	
	private final SMTComplexBinaryOperator operator;
	private final Set<SMTStatement> statements;
	
	public SMTComplexBinaryOperation(SMTComplexBinaryOperator operator, Set<SMTStatement> statements) {
		this.operator = operator;
		this.statements = statements;
	}
	
	public String toString() {
		String stringOp = null;
		switch(operator) {
		case OR: stringOp = "or"; break;
		}
		
		StringBuilder statementsAsString = new StringBuilder();
		for(SMTStatement statement : statements) 
			statementsAsString.append(statement.toString() + " ");
		
		return String.format("(%s %s)", stringOp, statementsAsString.toString());
	}
	
	@Override
	public SMTStatement getStatement() {
		return this;
	}

}
