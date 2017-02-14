package de.tu_darmstadt.sse.decisionmaker.analysis;

import java.util.List;
import java.util.Set;

import soot.Unit;
import de.tu_darmstadt.sse.decisionmaker.server.ThreadTraceManager;
import de.tu_darmstadt.sse.decisionmaker.server.TraceManager;
import de.tu_darmstadt.sse.sharedclasses.networkconnection.DecisionRequest;


public abstract class FuzzyAnalysis {
	private int penaltyRank = 0;
	
	
	public abstract void doPreAnalysis(Set<Unit> targetUnits, TraceManager traceManager);
	
	
	public abstract List<AnalysisDecision> resolveRequest(DecisionRequest clientRequest,
			ThreadTraceManager completeHistory);
	
	
	public abstract void reset();
	
	public abstract String getAnalysisName();

	
	public void setPenaltyRank(int rank) {
		this.penaltyRank = rank;
	}
	
	
	public int getPenaltyRank() {
		return this.penaltyRank;
	}
	
}
