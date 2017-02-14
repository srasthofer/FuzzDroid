package de.tu_darmstadt.sse.decisionmaker.server.history;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import soot.Unit;
import de.tu_darmstadt.sse.apkspecific.CodeModel.CodePositionManager;
import de.tu_darmstadt.sse.decisionmaker.analysis.AnalysisDecision;
import de.tu_darmstadt.sse.decisionmaker.analysis.dynamicValues.DynamicValueContainer;
import de.tu_darmstadt.sse.dynamiccfg.Callgraph;
import de.tu_darmstadt.sse.sharedclasses.networkconnection.DecisionRequest;
import de.tu_darmstadt.sse.sharedclasses.util.Pair;


public class ClientHistory implements Cloneable {
	
	private List<Unit> codePositions = new ArrayList<Unit>();
	
	private List<Pair<Unit, Boolean>> pathTrace = new ArrayList<Pair<Unit, Boolean>>();
	
	private List<Pair<DecisionRequest, AnalysisDecision>> decisionAndResponse = new ArrayList<Pair<DecisionRequest, AnalysisDecision>>();
	
	private Callgraph dynamicCallgraph = new Callgraph();
	
	private Map<String, Integer> progressMetrics = new HashMap<>();
	
	private boolean isShadowTrace = false;
	
	private String crashExceptionMessage = null;
	
	private DynamicValueContainer dynamicValues = new DynamicValueContainer();
	
	
	public ClientHistory() {
		
	}
	
	
	private ClientHistory(ClientHistory original) {
		this.codePositions.addAll(original.codePositions);
		this.pathTrace.addAll(original.pathTrace);
		for (Pair<DecisionRequest, AnalysisDecision> orgDecResp : original.decisionAndResponse)
			this.decisionAndResponse.add(new Pair<>(orgDecResp.getFirst().clone(),
					orgDecResp.getSecond().clone()));
		this.dynamicCallgraph = original.dynamicCallgraph.clone();
		this.progressMetrics.putAll(original.progressMetrics);
		this.crashExceptionMessage = original.crashExceptionMessage;
		this.dynamicValues = dynamicValues.clone();
	}
	
	
	public void addCodePosition(Unit codePostion) {
		// The client might notify us of the same code position several times
		if (codePositions.isEmpty()
				|| codePositions.get(codePositions.size() - 1) != codePostion)
			codePositions.add(codePostion);
	}
	
	
	public void addCodePosition(int codePostion, CodePositionManager manager) {
		Unit unit = manager.getUnitForCodePosition(codePostion);
		addCodePosition(unit);
	}
	
	
	public void addPathTrace(Unit ifStmt, boolean decision) {
		pathTrace.add(new Pair<Unit, Boolean>(ifStmt, decision));
	}	
	
	
	public void addDecisionRequestAndResponse(DecisionRequest request, AnalysisDecision response) {
		decisionAndResponse.add(new Pair<>(request, response));
	}

	public List<Unit> getCodePostions() {
		return codePositions;
	}
	public List<Pair<Unit, Boolean>> getPathTrace() {
		return pathTrace;
	}
	
	public List<Pair<DecisionRequest, AnalysisDecision>> getDecisionAndResponse() {
		return decisionAndResponse;
	}
	
	public Callgraph getCallgraph() {
		return this.dynamicCallgraph;
	}
	
	public void setProgressValue(String metric, int value) {
		// We always take the best value we have seen so far
		Integer oldValue = this.progressMetrics.get(metric);
		int newValue = value;
		if (oldValue != null)
			newValue = Math.min(value, oldValue);
		this.progressMetrics.put(metric, newValue);
	}
	
	
	public int getProgressValue(String metric) {
		Integer val = progressMetrics.get(metric);
		return val == null ? Integer.MAX_VALUE : val;
	}
	
	
	public AnalysisDecision getResponseForRequest(DecisionRequest request) {
		for (Pair<DecisionRequest, AnalysisDecision> pair : getDecisionAndResponse())
			if (pair.getFirst().equals(request) && pair.getSecond().getServerResponse().doesResponseExist())
				return pair.getSecond();
		return null;
	}
	
	
	public boolean hasOnlyEmptyDecisions() {
		if (decisionAndResponse.isEmpty())
			return false;
		for (Pair<DecisionRequest, AnalysisDecision> pair : decisionAndResponse)
			if (pair.getSecond().getServerResponse().doesResponseExist())
				return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((codePositions == null) ? 0 : codePositions.hashCode());
		result = prime
				* result
				+ ((decisionAndResponse == null) ? 0 : decisionAndResponse
						.hashCode());
		result = prime
				* result
				+ ((dynamicCallgraph == null) ? 0 : dynamicCallgraph.hashCode());
		result = prime * result
				+ ((pathTrace == null) ? 0 : pathTrace.hashCode());
		result = prime * result
				+ ((progressMetrics == null) ? 0 : progressMetrics.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ClientHistory other = (ClientHistory) obj;
		if (codePositions == null) {
			if (other.codePositions != null)
				return false;
		} else if (!codePositions.equals(other.codePositions))
			return false;
		if (decisionAndResponse == null) {
			if (other.decisionAndResponse != null)
				return false;
		} else if (!decisionAndResponse.equals(other.decisionAndResponse))
			return false;
		if (dynamicCallgraph == null) {
			if (other.dynamicCallgraph != null)
				return false;
		} else if (!dynamicCallgraph.equals(other.dynamicCallgraph))
			return false;
		if (pathTrace == null) {
			if (other.pathTrace != null)
				return false;
		} else if (!pathTrace.equals(other.pathTrace))
			return false;
		if (progressMetrics == null) {
			if (other.progressMetrics != null)
				return false;
		} else if (!progressMetrics.equals(other.progressMetrics))
			return false;
		return true;
	}
	
	
	public int length() {
		return decisionAndResponse.size();
	}
	
	
	public boolean isPrefixOf(ClientHistory existingHistory) {
		// The given history must be at least as long as the current one
		if (existingHistory.length() < this.length())
			return false;
		
		// Check for incompatibilities
		for (int i = 0; i < decisionAndResponse.size(); i++)  {
			Pair<DecisionRequest, AnalysisDecision> pairThis = decisionAndResponse.get(i);
			Pair<DecisionRequest, AnalysisDecision> pairEx = existingHistory.decisionAndResponse.get(i);
			if (!pairThis.getFirst().equals(pairEx.getFirst())
					|| !pairThis.getSecond().equals(pairEx.getSecond()))
				return false;
		}
		return true;
	}
	
	
	@Override
	public ClientHistory clone() {
		return new ClientHistory(this);
	}

	
	public boolean isShadowTrace() {
		return isShadowTrace;
	}

	
	public void setShadowTrace(boolean isShadowTrace) {
		this.isShadowTrace = isShadowTrace;
	}
	
	
	public void setCrashException(String exceptionMessage) {
		this.crashExceptionMessage = exceptionMessage;
	}
	
	
	public String getCrashException() {
		return this.crashExceptionMessage;
	}
	
	
	public List<Pair<DecisionRequest, AnalysisDecision>> getAllDecisionRequestsAndResponses() {
		return this.decisionAndResponse;
	}
	
	
	public DynamicValueContainer getDynamicValues() {
		return this.dynamicValues;
	}

	
	public void removeUnusedDecisions() {
		for (Iterator<Pair<DecisionRequest, AnalysisDecision>> pairIt = decisionAndResponse.iterator();
				pairIt.hasNext(); ) {
			Pair<DecisionRequest, AnalysisDecision> pair = pairIt.next();
			if (!pair.getSecond().isDecisionUsed())
				pairIt.remove();
		}
	}
}
