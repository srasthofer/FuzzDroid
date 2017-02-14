package de.tu_darmstadt.sse.decisionmaker.analysis.symbolicexecution.datastructure;

import java.util.ArrayList;
import java.util.List;


public class SMTProgram implements Cloneable{
	//SSA-based variable declaration
	List<SMTBinding> variableDeclarations;
	//all assert statements
	List<SMTAssertStatement> assertStatements;
	
	public SMTProgram() {
		variableDeclarations = new ArrayList<SMTBinding>();	
		assertStatements = new ArrayList<SMTAssertStatement>();
	}
	
	public SMTProgram(List<SMTBinding> variableDeclarations, List<SMTAssertStatement> assertStatements) {
		this.variableDeclarations = variableDeclarations;	
		this.assertStatements = assertStatements;
	}
	
	public void addVariableDeclaration(SMTBinding variableDecl) {
		if(variableDecl == null)
			throw new RuntimeException("binding should not be null");
		this.variableDeclarations.add(variableDecl);
	}
	
	public void addAssertStatement(SMTAssertStatement assertStmt) {
		if(assertStmt == null)
			throw new RuntimeException("assertion should not be null");
		this.assertStatements.add(assertStmt);
	}
	
	public void removeAssertStatement(SMTAssertStatement assertStmt) {
		if(assertStatements.contains(assertStmt))
			assertStatements.remove(assertStmt);
	}
	
	
	public List<SMTBinding> getVariableDeclarations() {
		return variableDeclarations;
	}

	public List<SMTAssertStatement> getAssertStatements() {
		return assertStatements;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(SMTBinding variable : variableDeclarations) {
			String stringFormat = String.format("(declare-variable %s %s)\n", variable.getBinding(), variable.getType().toString());
			sb.append(stringFormat);
		}
		sb.append("\n");
		for(SMTAssertStatement assertStmt : assertStatements) {
			sb.append(assertStmt.toString() + "\n");
		}
		sb.append("\n");
		sb.append("(check-sat)\n");
		sb.append("(get-model)");
		return sb.toString();
	}
	
	public SMTProgram clone() {
		//Deep copy
		//note that it is not necessary to make deep copy of the SMTBinding elements
		List<SMTBinding> newVariableDeclarations = new ArrayList<SMTBinding>(variableDeclarations);
		//note that it is not necessary to make deep copy of the SMTAssertStatement elements
		List<SMTAssertStatement> newAssertStmts = new ArrayList<SMTAssertStatement>(assertStatements);
			
		SMTProgram newSMTProgram = new SMTProgram(newVariableDeclarations, newAssertStmts);
		return newSMTProgram;
	  }
}
