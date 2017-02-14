package de.tu_darmstadt.sse.decisionmaker.analysis.smartconstantdataextractor;

import soot.jimple.Stmt;


public class DataFlowObject {
	private final Stmt[] dataflow;
	
	public DataFlowObject(Stmt[] dataflow) {
		this.dataflow = dataflow;
	}

	public Stmt[] getDataflow() {
		return dataflow;
	}
}
