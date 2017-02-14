package de.tu_darmstadt.sse.decisionmaker.analysis.stringtotypeextractor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.Scene;
import soot.Unit;
import soot.jimple.infoflow.Infoflow;
import soot.jimple.infoflow.android.data.parsers.PermissionMethodParser;
import soot.jimple.infoflow.android.source.AccessPathBasedSourceSinkManager;
import soot.jimple.infoflow.data.pathBuilders.DefaultPathBuilderFactory;
import soot.jimple.infoflow.data.pathBuilders.DefaultPathBuilderFactory.PathBuilder;
import soot.jimple.infoflow.source.ISourceSinkManager;
import soot.jimple.infoflow.taintWrappers.EasyTaintWrapper;
import de.tu_darmstadt.sse.FrameworkOptions;
import de.tu_darmstadt.sse.decisionmaker.analysis.AnalysisDecision;
import de.tu_darmstadt.sse.decisionmaker.analysis.FuzzyAnalysis;
import de.tu_darmstadt.sse.decisionmaker.server.ThreadTraceManager;
import de.tu_darmstadt.sse.decisionmaker.server.TraceManager;
import de.tu_darmstadt.sse.sharedclasses.networkconnection.DecisionRequest;
import de.tu_darmstadt.sse.sharedclasses.networkconnection.ServerResponse;


public class StringToPrimitiveTypeFuzzer extends FuzzyAnalysis{
	private final static String TAINT_WRAPPER_PATH = FrameworkOptions.frameworkDir + "/src/de/tu_darmstadt/sse/decisionmaker/analysis/EasyTaintWrapperSource.txt";
	private static final String SOURCES_SINKS_FILE = FrameworkOptions.frameworkDir + "/src/de/tu_darmstadt/sse/decisionmaker/analysis/stringtotypeextractor/SourcesAndSinks.txt";
	
	private final Map<Integer, Set<Object>> valuesToFuzz = new HashMap<Integer, Set<Object>>();
	
	@Override
	public void doPreAnalysis(Set<Unit> targetUnits, TraceManager traceManager) {
		runDataflowAnalysis();		
	}

	@Override
	public List<AnalysisDecision> resolveRequest(DecisionRequest clientRequest, ThreadTraceManager completeHistory) {
		List<AnalysisDecision> decisions = new ArrayList<AnalysisDecision>();		
		int codePosID = clientRequest.getCodePosition();
		//we have to add one to it
		codePosID += 1;
		
		if(valuesToFuzz.keySet().contains(codePosID)) {
			Set<Object> values = valuesToFuzz.get(codePosID);			
			for(Object value : values) {
				ServerResponse response = new ServerResponse();
				response.setAnalysisName(getAnalysisName());
		        response.setResponseExist(true);
		        response.setReturnValue(value);
				AnalysisDecision finalDecision = new AnalysisDecision();
				finalDecision.setAnalysisName(getAnalysisName());
				finalDecision.setDecisionWeight(10);
			    finalDecision.setServerResponse(response);		        
			    decisions.add(finalDecision);
			}
		}
		
		return decisions;
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public String getAnalysisName() {
		return "StringToPrimitiveInference";
	}
	
	private void runDataflowAnalysis() {
		try{
			Scene.v().getOrMakeFastHierarchy();
			
			InplaceInfoflow infoflow = new InplaceInfoflow();	
			infoflow.setPathBuilderFactory(new DefaultPathBuilderFactory(
					PathBuilder.ContextSensitive, true));
			infoflow.setTaintWrapper(new EasyTaintWrapper(TAINT_WRAPPER_PATH));
			infoflow.getConfig().setEnableExceptionTracking(false);
			infoflow.getConfig().setEnableArraySizeTainting(false);
//			infoflow.getConfig().setCallgraphAlgorithm(CallgraphAlgorithm.CHA);
			
			System.out.println("Running data flow analysis...");
			PermissionMethodParser pmp = PermissionMethodParser.fromFile(SOURCES_SINKS_FILE);
			AccessPathBasedSourceSinkManager srcSinkManager =
					new AccessPathBasedSourceSinkManager(pmp.getSources(), pmp.getSinks());
						
			infoflow.addResultsAvailableHandler(new StringToPrimitiveTypeExtractorDataflowHandler(valuesToFuzz));
			infoflow.runAnalysis(srcSinkManager);
		}catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	
	private class InplaceInfoflow extends Infoflow {
		
		public InplaceInfoflow() {
			super();
		}

		@Override
		public void runAnalysis(final ISourceSinkManager sourcesSinks) {
			super.runAnalysis(sourcesSinks);
		}		
	}	

}
