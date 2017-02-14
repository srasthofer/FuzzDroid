package de.tu_darmstadt.sse.decisionmaker.analysis.symbolicexecution.datastructure;


public class SMTAssertStatement {
	private SMTStatement statement;
	
	public SMTAssertStatement(SMTStatement statement) {
		this.statement = statement;
	}		
	
	public SMTStatement getStatement() {
		return statement;
	}

	public String toString() {
		return String.format("( assert %s )", statement.toString());
	}
}
