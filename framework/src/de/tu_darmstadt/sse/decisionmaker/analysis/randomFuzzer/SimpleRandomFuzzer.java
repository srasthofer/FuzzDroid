package de.tu_darmstadt.sse.decisionmaker.analysis.randomFuzzer;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import soot.Unit;
import de.tu_darmstadt.sse.decisionmaker.DeterministicRandom;
import de.tu_darmstadt.sse.decisionmaker.analysis.AnalysisDecision;
import de.tu_darmstadt.sse.decisionmaker.analysis.FuzzyAnalysis;
import de.tu_darmstadt.sse.decisionmaker.server.ThreadTraceManager;
import de.tu_darmstadt.sse.decisionmaker.server.TraceManager;
import de.tu_darmstadt.sse.sharedclasses.networkconnection.DecisionRequest;
import de.tu_darmstadt.sse.sharedclasses.networkconnection.ServerResponse;


public class SimpleRandomFuzzer extends FuzzyAnalysis {

	private RandomPrimitives primitives = new RandomPrimitives();

	@Override
	public void doPreAnalysis(Set<Unit> targetUnits, TraceManager traceManager){
	}

	@Override
	public List<AnalysisDecision> resolveRequest(DecisionRequest clientRequest,
			ThreadTraceManager completeHistory) {
		AnalysisDecision finalDecistion = new AnalysisDecision();
		finalDecistion.setDecisionWeight(3);
		finalDecistion.setAnalysisName(getAnalysisName());
		ServerResponse response = new ServerResponse();
		response.setAnalysisName(getAnalysisName());

		if (clientRequest.isHookAfter()) {
			String hookSignature = clientRequest.getLoggingPointSignature();
			String returnType = extractReturnType(hookSignature);
			if (!returnType.equals("void")) {
				if (primitives.isSupportedType(returnType)) {
					if (DeterministicRandom.theRandom.nextBoolean()) {
						Object value = primitives.next(returnType);
						response.setReturnValue(value);
						response.setResponseExist(true);
					}
				} else {
					if (DeterministicRandom.theRandom.nextBoolean()) {
						response.setReturnValue("null");
						response.setResponseExist(true);
					}
				}
			}
		}

		finalDecistion.setServerResponse(response);
		return Collections.singletonList(finalDecistion);
	}

	private String extractReturnType(String methodSignature) {
		return methodSignature.split(": ")[1].split(" ")[0];
	}

	@Override
	public void reset() {
		// nothing to do here
	}
	
	@Override
	public String getAnalysisName() {
		return "SimpleRandomFuzzer";
	}

}
