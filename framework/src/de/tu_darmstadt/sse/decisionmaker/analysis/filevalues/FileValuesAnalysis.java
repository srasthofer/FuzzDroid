package de.tu_darmstadt.sse.decisionmaker.analysis.filevalues;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import soot.RefType;
import soot.Type;
import soot.Unit;
import soot.jimple.AssignStmt;
import de.tu_darmstadt.sse.apkspecific.CodeModel.CodePositionManager;
import de.tu_darmstadt.sse.decisionmaker.analysis.AnalysisDecision;
import de.tu_darmstadt.sse.decisionmaker.analysis.FuzzyAnalysis;
import de.tu_darmstadt.sse.decisionmaker.server.ThreadTraceManager;
import de.tu_darmstadt.sse.decisionmaker.server.TraceManager;
import de.tu_darmstadt.sse.dynamiccfg.utils.FileUtils;
import de.tu_darmstadt.sse.sharedclasses.networkconnection.DecisionRequest;
import de.tu_darmstadt.sse.sharedclasses.networkconnection.ServerResponse;
import de.tu_darmstadt.sse.sharedclasses.util.Pair;


public class FileValuesAnalysis extends FuzzyAnalysis {

	private final static String RANDOM_VALUES_FILENAME = "." + File.separator + "files" + File.separator + "randomValues.txt";

	private CodePositionManager codePositionManager = CodePositionManager.getCodePositionManagerInstance();
	private Set<String> values;

	@Override
	public void doPreAnalysis(Set<Unit> targetUnits, TraceManager traceManager) {
		// Read the file
		this.values = FileUtils.textFileToLineSet(RANDOM_VALUES_FILENAME);
	}

	@Override
	public List<AnalysisDecision> resolveRequest(DecisionRequest clientRequest,
			ThreadTraceManager completeHistory) {
		Unit u = codePositionManager.getUnitForCodePosition(clientRequest.getCodePosition() + 1);
		if (!(u instanceof AssignStmt))
			return Collections.emptyList();
		AssignStmt assignStmt = (AssignStmt) u;
		
		// We only support strings at the moment
		if (assignStmt.getLeftOp().getType() != RefType.v("java.lang.String"))
			return Collections.emptyList();
		
		RefType stringType = RefType.v("java.lang.String");
		
		// Return the dynamically-obtained strings
		List<AnalysisDecision> decisions = new ArrayList<>(values.size());
		for (String value : values) {
			ServerResponse serverResponse = new ServerResponse();
			serverResponse.setResponseExist(true);
			
			if (clientRequest.isHookAfter()) {
				serverResponse.setReturnValue(value);
			}
			else if (assignStmt.containsInvokeExpr()) {
				Set<Pair<Integer, Object>> paramValues = new HashSet<>();
				for (int i = 0; i < assignStmt.getInvokeExpr().getArgCount(); i++) {
					Type paramType = assignStmt.getInvokeExpr().getMethod().getParameterType(i);
					if (paramType == stringType)
						paramValues.add(new Pair<Integer, Object>(i, value));
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

	@Override
	public void reset() {
		// nothing to do here
	}

	@Override
	public String getAnalysisName() {
		return "FileValues";
	}

}
