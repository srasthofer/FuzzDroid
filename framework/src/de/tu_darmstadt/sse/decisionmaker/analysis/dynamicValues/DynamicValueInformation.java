package de.tu_darmstadt.sse.decisionmaker.analysis.dynamicValues;

import soot.jimple.Stmt;
import de.tu_darmstadt.sse.decisionmaker.analysis.symbolicexecution.datastructure.SMTBinding;


public class DynamicValueInformation {
	private final Stmt statement;
	private final SMTBinding binding;
	private boolean baseObject = false;
	private int argPos = -1;
	//this information is required for the SMT-solver problem with digits as Strings
	private Stmt sourceOfDataflow;
	
	public DynamicValueInformation(Stmt stmt, SMTBinding binding) {
		this.statement = stmt;
		this.binding = binding;
	}
	
	public void setBaseObject(boolean bool) {
		baseObject = bool;
	}

	public int getArgPos() {
		return argPos;
	}

	public void setArgPos(int argPos) {
		this.argPos = argPos;
	}

	public Stmt getStatement() {
		return statement;
	}

	public SMTBinding getBinding() {
		return binding;
	}

	public boolean isBaseObject() {
		return baseObject;
	}

	public Stmt getSourceOfDataflow() {
		return sourceOfDataflow;
	}

	public void setSourceOfDataflow(Stmt sourceOfDataflow) {
		this.sourceOfDataflow = sourceOfDataflow;
	}		
}
