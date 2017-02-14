package de.tu_darmstadt.sse.decisionmaker.analysis.simpleBooleanAnalysis;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import soot.Unit;
import de.tu_darmstadt.sse.decisionmaker.analysis.AnalysisDecision;
import de.tu_darmstadt.sse.decisionmaker.analysis.FuzzyAnalysis;
import de.tu_darmstadt.sse.decisionmaker.server.ThreadTraceManager;
import de.tu_darmstadt.sse.decisionmaker.server.TraceManager;
import de.tu_darmstadt.sse.sharedclasses.networkconnection.DecisionRequest;
import de.tu_darmstadt.sse.sharedclasses.networkconnection.ServerResponse;


public class SimpleBooleanAnalysis extends FuzzyAnalysis{
	
	private final Set<Integer> seenCodePositions = new HashSet<Integer>();
	
	@Override
	public void doPreAnalysis(Set<Unit> targetUnits, TraceManager traceManager) {
		//No pre-analysis required		
	}

	@Override
	public List<AnalysisDecision> resolveRequest(DecisionRequest clientRequest,
			ThreadTraceManager completeHistory) {
		List<AnalysisDecision> decisions = new ArrayList<AnalysisDecision>();
		
		if(seenCodePositions.contains(clientRequest.getCodePosition()))
			return null;
		
		if(clientRequest.getLoggingPointSignature().equals("<android.app.SharedPreferencesImpl: boolean getBoolean(java.lang.String, boolean)>") ||
				clientRequest.getLoggingPointSignature().equals("<android.app.admin.DevicePolicyManager: boolean isAdminActive(android.content.ComponentName)>")	
				) {	
			seenCodePositions.add(clientRequest.getCodePosition());
			ServerResponse responseTrue = new ServerResponse();
			responseTrue.setAnalysisName(getAnalysisName());
			responseTrue.setResponseExist(true);
			responseTrue.setReturnValue(true);
	        AnalysisDecision finalDecisionTrue = new AnalysisDecision();
	        finalDecisionTrue.setAnalysisName(getAnalysisName());
	        finalDecisionTrue.setDecisionWeight(5);
	        finalDecisionTrue.setServerResponse(responseTrue);
	        ServerResponse responseFalse = new ServerResponse();
	        responseFalse.setAnalysisName(getAnalysisName());
	        responseFalse.setResponseExist(true);
	        responseFalse.setReturnValue(false);
	        AnalysisDecision finalDecisionFalse = new AnalysisDecision();
	        finalDecisionFalse.setAnalysisName(getAnalysisName());
	        finalDecisionFalse.setDecisionWeight(5);
	        finalDecisionFalse.setServerResponse(responseFalse);
				        
	        decisions.add(finalDecisionTrue);      
	        decisions.add(finalDecisionFalse);
	        return decisions;
		}		

		return null;
	}

	@Override
	public void reset() {
		// no reset requried		
	}
	
	@Override
	public String getAnalysisName() {
		return "SimpleBooleanAnalysis";
	}

}
