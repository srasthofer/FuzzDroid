package de.tu_darmstadt.sse.decisionmaker.analysis.dynamicValues;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import soot.IntType;
import soot.RefType;
import soot.Type;
import soot.Unit;
import soot.jimple.Stmt;
import de.tu_darmstadt.sse.apkspecific.CodeModel.CodePositionManager;
import de.tu_darmstadt.sse.decisionmaker.analysis.AnalysisDecision;
import de.tu_darmstadt.sse.decisionmaker.analysis.FuzzyAnalysis;
import de.tu_darmstadt.sse.decisionmaker.server.ThreadTraceManager;
import de.tu_darmstadt.sse.decisionmaker.server.TraceManager;
import de.tu_darmstadt.sse.sharedclasses.networkconnection.DecisionRequest;
import de.tu_darmstadt.sse.sharedclasses.networkconnection.ServerResponse;
import de.tu_darmstadt.sse.sharedclasses.util.Pair;


public class DynamicValueAnalysis extends FuzzyAnalysis {

	private CodePositionManager codePositionManager = CodePositionManager.getCodePositionManagerInstance();
	
	
	public DynamicValueAnalysis() {
		//
	}
	
	@Override
	public void doPreAnalysis(Set<Unit> targetUnits, TraceManager traceManager) {
		// nothing to do here
	}

	@Override
	public List<AnalysisDecision> resolveRequest(DecisionRequest clientRequest,
			ThreadTraceManager completeHistory) {
		Stmt s = (Stmt) codePositionManager.getUnitForCodePosition(clientRequest.getCodePosition() + 1);
		if (!s.containsInvokeExpr())
			return Collections.emptyList();
		
		RefType stringType = RefType.v("java.lang.String");
		
		// Return the dynamically-obtained values
		Set<DynamicValue> runtimeValues = completeHistory.getNewestClientHistory().getDynamicValues().getValues();
		List<AnalysisDecision> decisions = new ArrayList<>(runtimeValues.size());
		for (DynamicValue value : runtimeValues) {
			ServerResponse serverResponse = new ServerResponse();
			serverResponse.setAnalysisName(getAnalysisName());
			serverResponse.setResponseExist(true);
			
			Type returnType = s.getInvokeExpr().getMethod().getReturnType();
			if (clientRequest.isHookAfter() && isSupported(returnType)) {
				serverResponse.setReturnValue(checkAndGet(returnType, value));
			}
			else {
				Set<Pair<Integer, Object>> paramValues = new HashSet<>();
				for (int i = 0; i < s.getInvokeExpr().getArgCount(); i++) {
					Type paramType = s.getInvokeExpr().getMethod().getParameterType(i);
					if (paramType == stringType) {
						Object newParamVal = checkAndGet(paramType, value);
						if (newParamVal != null)
							paramValues.add(new Pair<Integer, Object>(i, newParamVal));
					}
				}
				serverResponse.setParamValues(paramValues);
			}
			
			AnalysisDecision decision = new AnalysisDecision();
			decision.setAnalysisName(getAnalysisName());
			decision.setServerResponse(serverResponse);
			decision.setDecisionWeight(5);
			decisions.add(decision);
		}
		return decisions;
	}
	
	
	private Object checkAndGet(Type tp, DynamicValue value) {
		if (tp == IntType.v() && value instanceof DynamicIntValue)
			return ((DynamicIntValue) value).getIntValue();
		else if (tp == RefType.v("java.lang.String") && value instanceof DynamicStringValue)
			return ((DynamicStringValue) value).getStringValue();
		else
			return null;
	}

	
	private boolean isSupported(Type returnType) {
		return returnType == RefType.v("java.lang.String")
				|| returnType == IntType.v();
	}

	@Override
	public void reset() {
		// nothing to do here
	}

	@Override
	public String getAnalysisName() {
		return "DynamicValues";
	}

}
