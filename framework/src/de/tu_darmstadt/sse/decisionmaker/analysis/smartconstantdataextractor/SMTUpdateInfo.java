package de.tu_darmstadt.sse.decisionmaker.analysis.smartconstantdataextractor;

import soot.jimple.Stmt;
import de.tu_darmstadt.sse.decisionmaker.analysis.symbolicexecution.datastructure.SMTAssertStatement;


public class SMTUpdateInfo {
	private final SMTAssertStatement assertionStmt;
	private final Stmt stmt;
	private final Stmt sourceOfDataflow;
	
	public SMTUpdateInfo(SMTAssertStatement assertionStmt, Stmt stmt, Stmt sourceOfDataflow) {
		this.assertionStmt = assertionStmt;
		this.stmt = stmt;
		this.sourceOfDataflow = sourceOfDataflow;
	}

	public SMTAssertStatement getAssertionStmt() {
		return assertionStmt;
	}

	public Stmt getStmt() {
		return stmt;
	}

	public Stmt getSourceOfDataflow() {
		return sourceOfDataflow;
	}		
}
