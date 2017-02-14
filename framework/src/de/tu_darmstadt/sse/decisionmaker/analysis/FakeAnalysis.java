package de.tu_darmstadt.sse.decisionmaker.analysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import soot.Unit;
import de.tu_darmstadt.sse.decisionmaker.server.ThreadTraceManager;
import de.tu_darmstadt.sse.decisionmaker.server.TraceManager;
import de.tu_darmstadt.sse.sharedclasses.networkconnection.DecisionRequest;
import de.tu_darmstadt.sse.sharedclasses.networkconnection.ServerResponse;

public class FakeAnalysis extends FuzzyAnalysis {

	@Override
	public void doPreAnalysis(Set<Unit> targetUnits, TraceManager traceManager) {
		//
	}

	private Set<DecisionRequest> requests = new HashSet<>();
	
	@Override
	public List<AnalysisDecision> resolveRequest(DecisionRequest clientRequest,
			ThreadTraceManager completeHistory) {
		if (!requests.add(clientRequest))
			return Collections.emptyList();
		
		List<AnalysisDecision> decisions = new ArrayList<>();
		
		// Fake 3 values
		decisions.add(fakeDecision("aaaag"));
		decisions.add(fakeDecision("bmtsg"));
		decisions.add(fakeDecision("bmegafong"));
 		
		return decisions;
	}

	private AnalysisDecision fakeDecision(String string) {
		ServerResponse resp = new ServerResponse();
		resp.setResponseExist(true);
		resp.setReturnValue(string);
		
		AnalysisDecision decision = new AnalysisDecision();
		decision.setAnalysisName("FAKE");
		decision.setDecisionWeight(100);
		decision.setServerResponse(resp);
		
		return decision;
	}

	@Override
	public void reset() {
		requests.clear();
	}

	@Override
	public String getAnalysisName() {
		return "FAKE";
	}

}
