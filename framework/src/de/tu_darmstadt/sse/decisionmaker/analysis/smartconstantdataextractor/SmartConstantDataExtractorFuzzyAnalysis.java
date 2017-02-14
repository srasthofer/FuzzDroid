package de.tu_darmstadt.sse.decisionmaker.analysis.smartconstantdataextractor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.codec.binary.Hex;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import de.tu_darmstadt.sse.FrameworkOptions;
import de.tu_darmstadt.sse.apkspecific.CodeModel.CodePosition;
import de.tu_darmstadt.sse.apkspecific.CodeModel.CodePositionManager;
import de.tu_darmstadt.sse.commandlinelogger.LoggerHelper;
import de.tu_darmstadt.sse.commandlinelogger.MyLevel;
import de.tu_darmstadt.sse.decisionmaker.analysis.AnalysisDecision;
import de.tu_darmstadt.sse.decisionmaker.analysis.FuzzyAnalysis;
import de.tu_darmstadt.sse.decisionmaker.analysis.dynamicValues.DynamicIntValue;
import de.tu_darmstadt.sse.decisionmaker.analysis.dynamicValues.DynamicStringValue;
import de.tu_darmstadt.sse.decisionmaker.analysis.dynamicValues.DynamicValue;
import de.tu_darmstadt.sse.decisionmaker.analysis.dynamicValues.DynamicValueContainer;
import de.tu_darmstadt.sse.decisionmaker.analysis.dynamicValues.DynamicValueInformation;
import de.tu_darmstadt.sse.decisionmaker.analysis.symbolicexecution.SMTConverter;
import de.tu_darmstadt.sse.decisionmaker.analysis.symbolicexecution.SMTExecutor;
import de.tu_darmstadt.sse.decisionmaker.analysis.symbolicexecution.datastructure.SMTAssertStatement;
import de.tu_darmstadt.sse.decisionmaker.analysis.symbolicexecution.datastructure.SMTBinding;
import de.tu_darmstadt.sse.decisionmaker.analysis.symbolicexecution.datastructure.SMTConstantValue;
import de.tu_darmstadt.sse.decisionmaker.analysis.symbolicexecution.datastructure.SMTProgram;
import de.tu_darmstadt.sse.decisionmaker.analysis.symbolicexecution.datastructure.SMTSimpleAssignment;
import de.tu_darmstadt.sse.decisionmaker.server.ThreadTraceManager;
import de.tu_darmstadt.sse.decisionmaker.server.TraceManager;
import de.tu_darmstadt.sse.decisionmaker.server.history.ClientHistory;
import de.tu_darmstadt.sse.sharedclasses.networkconnection.DecisionRequest;
import de.tu_darmstadt.sse.sharedclasses.networkconnection.ServerResponse;
import de.tu_darmstadt.sse.sharedclasses.util.Pair;
import soot.Scene;
import soot.Unit;
import soot.Value;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.infoflow.Infoflow;
import soot.jimple.infoflow.InfoflowConfiguration;
import soot.jimple.infoflow.android.data.parsers.PermissionMethodParser;
import soot.jimple.infoflow.android.source.AccessPathBasedSourceSinkManager;
import soot.jimple.infoflow.data.pathBuilders.DefaultPathBuilderFactory;
import soot.jimple.infoflow.data.pathBuilders.DefaultPathBuilderFactory.PathBuilder;
import soot.jimple.infoflow.handlers.ResultsAvailableHandler;
import soot.jimple.infoflow.results.InfoflowResults;
import soot.jimple.infoflow.results.ResultSourceInfo;
import soot.jimple.infoflow.solver.cfg.IInfoflowCFG;
import soot.jimple.infoflow.source.ISourceSinkManager;
import soot.jimple.infoflow.source.data.SourceSinkDefinition;
import soot.jimple.infoflow.taintWrappers.EasyTaintWrapper;


public class SmartConstantDataExtractorFuzzyAnalysis extends FuzzyAnalysis {

	private final static String TAINT_WRAPPER_PATH = FrameworkOptions.frameworkDir + "/src/de/tu_darmstadt/sse/decisionmaker/analysis/EasyTaintWrapperSource.txt";
	private static final String SOURCES_SINKS_FILE = FrameworkOptions.frameworkDir + "/src/de/tu_darmstadt/sse/decisionmaker/analysis/smartconstantdataextractor/SourcesAndSinks.txt";
	
	CodePositionManager codePositionManager = CodePositionManager.getCodePositionManagerInstance();
	Map<Integer, Set<Object>> constantBasedValuesToFuzz = new HashMap<Integer, Set<Object>>();
	Map<Integer, Set<Object>> dynamicValueBasedValuesToFuzz = new HashMap<Integer, Set<Object>>();
	
	
	Map<DataFlowObject, Set<SMTProgram>> dataFlowsToSMTPrograms = new HashMap<DataFlowObject, Set<SMTProgram>>();
	
	private Map<SMTProgram, Set<DynamicValueInformation>> dynamicValueInfos = new HashMap<SMTProgram, Set<DynamicValueInformation>>();
	
	private Set<Set<DynamicValue>> dynValuesOfRuns = new HashSet<Set<DynamicValue>>();
	
	private Set<Integer> staticValuesSent = new HashSet<Integer>();
	
	public SmartConstantDataExtractorFuzzyAnalysis() {
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
	
	@Override
	public String getAnalysisName() {
		return "SymbolicDataExtractor";
	}
	
	
	private class FuzzerResultsAvailableHandler implements ResultsAvailableHandler {
		
		private final Set<SourceSinkDefinition> sources;
		private final Set<Unit> targetUnits;
		
		public FuzzerResultsAvailableHandler(Set<SourceSinkDefinition> sources,
				Set<Unit> targetUnits) {
			this.sources = sources;
			this.targetUnits = targetUnits;
		}
		
		@Override
		public void onResultsAvailable(IInfoflowCFG cfg, InfoflowResults results) {
			System.out.println("############################# RESULTS: " + results.getResults().keySet().size());			
			SMTPreparationPhase smtPreparation = new SMTPreparationPhase(cfg, results);			
			Set<ResultSourceInfo> preparedDataFlowsForSMT = smtPreparation.prepareDataFlowPathsForSMTConverter();
			
			//pre-run for split methods
			Table<Stmt, Integer, Set<String>> splitInfos = HashBasedTable.create();
			for(ResultSourceInfo dataFlow : preparedDataFlowsForSMT) {									
				//pre-analysis especially for the split api call
				if(dataFlow.getPath()[0].containsInvokeExpr()) {
					InvokeExpr inv = dataFlow.getPath()[0].getInvokeExpr();
					//special treatment in case of a dataflow starting with a split method
					if(inv.getMethod().getSignature().equals("<java.lang.String: java.lang.String[] split(java.lang.String)>")) {
						
						//we remove the split-API method from the source list
						for(Iterator<SourceSinkDefinition> iterator = this.sources.iterator(); iterator.hasNext(); ) {
							SourceSinkDefinition source = iterator.next();
							if(source.getMethod().getSignature().equals("<java.lang.String: java.lang.String[] split(java.lang.String)>"))
								iterator.remove();
						}
						
						splitAPI_DataFlowtoSMTConvertion(dataFlow, cfg, preparedDataFlowsForSMT, splitInfos);											
					}																	
				}
			}
			
			//actual run:
			for(ResultSourceInfo dataFlow : preparedDataFlowsForSMT) {				
				if(dataFlow.getPath()[0].containsInvokeExpr()) {
					InvokeExpr inv = dataFlow.getPath()[0].getInvokeExpr();
					//standard case
					if(!inv.getMethod().getSignature().equals("<java.lang.String: java.lang.String[] split(java.lang.String)>")) {			
						standardDataFlowToSMTConvertion(dataFlow, cfg, preparedDataFlowsForSMT, splitInfos);
					}
				}
			}
			
		}
		
		
		private void standardDataFlowToSMTConvertion(ResultSourceInfo dataFlow, IInfoflowCFG cfg, Set<ResultSourceInfo> preparedDataFlowsForSMT, Table<Stmt, Integer, Set<String>> splitInfos) {
			SMTConverter converter = new SMTConverter(sources);
			for(int i = 0; i < dataFlow.getPath().length; i++) {				
				System.out.println("\t" + dataFlow.getPath()[i]);
				System.out.println("\t\t" + dataFlow.getPathAccessPaths()[i]);
			}
			
			converter.convertJimpleToSMT(dataFlow.getPath(),
					dataFlow.getPathAccessPaths(), targetUnits, cfg, splitInfos);
			
			dataFlowsToSMTPrograms.put(new DataFlowObject(dataFlow.getPath()), converter.getSmtPrograms());

			//dynamic value information
			dynamicValueInfos.putAll(converter.getDynamicValueInfos());
			
			converter.printProgramToCmdLine();
			
			File z3str2Script = new File(FrameworkOptions.Z3SCRIPT_LOCATION);
			if(!z3str2Script.exists())
				throw new RuntimeException("There is no z3-script available");
			SMTExecutor smtExecutor = new SMTExecutor(converter.getSmtPrograms(), z3str2Script);
			Set<File> smtFiles = smtExecutor.createSMTFile();
			
			Set<Object> values = new HashSet<Object>();
			for(File smtFile : smtFiles) {
				String loggingPointValue = smtExecutor.executeZ3str2ScriptAndExtractLoggingPointValue(smtFile);
				if(loggingPointValue != null) {					
					loggingPointValue = fixSMTSolverIntegerOutput(loggingPointValue, dataFlow.getPath()[0]);					
					
					//SMT solver only returns hex-based UTF-8 values in some cases; we fixed this with our own hexToUnicode converter
					if(loggingPointValue != null && loggingPointValue.contains("\\x")) 
						addAdditionalUnicodeValue(loggingPointValue, values);
					if(loggingPointValue != null)
						values.add(loggingPointValue);
					System.out.println(String.format("Extracted loggingpoint-value: %s", loggingPointValue));
				}
			}
			
			System.out.println("####################################");
			
			//add values to fuzzy-seed
			Stmt stmt = dataFlow.getSource();
			CodePosition position = codePositionManager.getCodePositionForUnit(stmt);
			if(constantBasedValuesToFuzz.containsKey(position.getID()))
				constantBasedValuesToFuzz.get(position.getID()).addAll(values);
			else
				constantBasedValuesToFuzz.put(position.getID(), values);
		}
		
		
		private void splitAPI_DataFlowtoSMTConvertion(ResultSourceInfo dataFlow, IInfoflowCFG cfg, Set<ResultSourceInfo> preparedDataFlowsForSMT, Table<Stmt, Integer, Set<String>> splitInfos) {			
			SMTConverter converter = new SMTConverter(sources);
			for(int i = 0; i < dataFlow.getPath().length; i++) {				
				System.out.println("\t" + dataFlow.getPath()[i]);
				System.out.println("\t\t" + dataFlow.getPathAccessPaths()[i]);
			}
			
			//we remove the first statement (split-API method)
			int n = dataFlow.getPath().length-1;
			Stmt[] reducedDataFlow = new Stmt[n];
			System.arraycopy(dataFlow.getPath(), 1, reducedDataFlow, 0, n);
						
			//currently only possible if there is a constant index for the array
			if(hasConstantIndexAtArrayForSplitDataFlow(reducedDataFlow)) {
				String valueOfInterest = getValueOfInterestForSplitDataflow(reducedDataFlow);
				
				converter.convertJimpleToSMT(reducedDataFlow,
						dataFlow.getPathAccessPaths(), targetUnits, cfg, null);
							
				converter.printProgramToCmdLine();
				
				File z3str2Script = new File(FrameworkOptions.Z3SCRIPT_LOCATION);
				if(!z3str2Script.exists())
					throw new RuntimeException("There is no z3-script available");
				SMTExecutor smtExecutor = new SMTExecutor(converter.getSmtPrograms(), z3str2Script);
				Set<File> smtFiles = smtExecutor.createSMTFile();
				
				for(File smtFile : smtFiles) {
					String loggingPointValue = smtExecutor.executeZ3str2ScriptAndExtractValue(smtFile, valueOfInterest);
					if(loggingPointValue != null) {
						Stmt splitStmt = dataFlow.getPath()[0];
						int index = getConstantArrayIndexForSplitDataFlow(reducedDataFlow);
						
						if(splitInfos.contains(splitStmt, index))
							splitInfos.get(splitStmt, index).add(loggingPointValue);
						else {
							Set<String> values = new HashSet<String>();
							values.add(loggingPointValue);
							splitInfos.put(splitStmt, index, values);
						}
					}
					System.out.println(loggingPointValue);
				}
				
				System.out.println("####################################");
			}			
		}
	}
	
	
	private String getValueOfInterestForSplitDataflow(Stmt[] dataflow) {
		Stmt firstAssign = dataflow[0];
		if(firstAssign instanceof AssignStmt) {
			AssignStmt ass = (AssignStmt)firstAssign;
			return ass.getLeftOp().toString();
		}
		else
			throw new RuntimeException("this should not happen - wrong assumption");
	}
	
	
	private boolean hasConstantIndexAtArrayForSplitDataFlow(Stmt[] dataflow) {
		Stmt firstAssign = dataflow[0];
		if(firstAssign instanceof AssignStmt) {
			AssignStmt ass = (AssignStmt)firstAssign;
			Value value = ass.getRightOp();
			if(value instanceof ArrayRef) {
				ArrayRef aRef = (ArrayRef)value;
				Value index = aRef.getIndex();
				
				if(index instanceof IntConstant)
					return true;
			}
		}
		else
			throw new RuntimeException("this should not happen - wrong assumption");
		
		return false;
	}
		
	private int getConstantArrayIndexForSplitDataFlow(Stmt[] dataflow) {
		Stmt firstAssign = dataflow[0];
		if(firstAssign instanceof AssignStmt) {
			AssignStmt ass = (AssignStmt)firstAssign;
			Value value = ass.getRightOp();
			if(value instanceof ArrayRef) {
				ArrayRef aRef = (ArrayRef)value;
				Value index = aRef.getIndex();
				
				if(index instanceof IntConstant)
					return ((IntConstant) index).value;
			}
		}
		else
			throw new RuntimeException("this should not happen - wrong assumption");
		
		return -1;
	}
	
	@Override
	public void doPreAnalysis(Set<Unit> targetUnits, TraceManager traceManager) {
		//necessary for update information once a new dynamic value is available
//		traceManager.addThreadTraceCreateHandler(new ThreadTraceManagerCreatedHandler() {
//			
//			@Override
//			public void onThreadTraceManagerCreated(
//					final ThreadTraceManager threadTraceManager) {
//				threadTraceManager.addOnCreateHandler(new ClientHistoryCreatedHandler() {
//					
//					@Override
//					public void onClientHistoryCreated(ClientHistory history) {
//						DynamicValueWorker worker = new DynamicValueWorker(threadTraceManager);
//						history.getDynamicValues().addDynamicValueUpdateHandler(worker);
//					}
//				});
//			}
//		});
		
		runAnalysis(targetUnits);
	}

	@Override
	public List<AnalysisDecision> resolveRequest(DecisionRequest clientRequest, ThreadTraceManager completeHistory) {		
		List<AnalysisDecision> decisions = new ArrayList<AnalysisDecision>();		
		int codePosition = clientRequest.getCodePosition();
		//Todo: why is there an 1 offset?
		codePosition += 1;
		
		ClientHistory history = completeHistory.getLastClientHistory();
		boolean dynValueCheckNecessary = areNewDynValuesOfHistory(history);
				
		//are there dynamic values available? static values are sent for the first request (so decision maker has all static values already)
		if(dynValueCheckNecessary && history != null) {
			dynValuesOfRuns.add(history.getDynamicValues().getValues());
			int SMT_ADDITIONAL_COMPUTATION_TIME = 3*60;
			DynamicValueContainer dynamicValues = history.getDynamicValues();			
			FrameworkOptions.forceTimeout = FrameworkOptions.forceTimeout + SMT_ADDITIONAL_COMPUTATION_TIME; 
			//depending on the argPos or the baseObject, we need to create a new value
			Table<SMTProgram, Stmt, List<Pair<DynamicValueInformation, DynamicValue>>> updateInfo = getSMTProgramUpdateInfos(dynamicValues);
			 if(!updateInfo.isEmpty()) {
				 for(SMTProgram smtProgram : updateInfo.rowKeySet()) {
						Set<Set<SMTUpdateInfo>> allAssertCombinations = getAllAssertCombinations(updateInfo.row(smtProgram));
						
						for(Set<SMTUpdateInfo> assertCombi : allAssertCombinations) {	
							Stmt sourceOfDataflow = null;
							for(SMTUpdateInfo assertInfo : assertCombi) {
								smtProgram.addAssertStatement(assertInfo.getAssertionStmt());
								if(sourceOfDataflow == null)
									sourceOfDataflow = assertInfo.getSourceOfDataflow();
								if(sourceOfDataflow != null) {
									if(!sourceOfDataflow.toString().equals(assertInfo.getSourceOfDataflow().toString()))
										LoggerHelper.logWarning("sourceOfDataflow have to be the same all the time!");
								}
							}							
							
							File z3str2Script = new File(FrameworkOptions.Z3SCRIPT_LOCATION);
							if(!z3str2Script.exists())
								throw new RuntimeException("There is no z3-script available");
							SMTExecutor smtExecutor = new SMTExecutor(Collections.singleton(smtProgram), z3str2Script);
							Set<File> smtFiles = smtExecutor.createSMTFile();
							
							//we need to remove it. if there are more dynamic values available, we need to get the clean
							//old program for the solver
							for(SMTUpdateInfo assertInfo : assertCombi) {								
								smtProgram.removeAssertStatement(assertInfo.getAssertionStmt());
							}							
														
							for(File smtFile : smtFiles) {						
								String loggingPointValue = smtExecutor.executeZ3str2ScriptAndExtractLoggingPointValue(smtFile);								
								if(loggingPointValue != null) {
									if(isSemanticallyCorrect(loggingPointValue, sourceOfDataflow)) {							
										System.out.println(loggingPointValue);
										for(SMTUpdateInfo assertInfo : assertCombi) {										
											//add values to fuzzy-seed
											Stmt stmt = assertInfo.getSourceOfDataflow();
											CodePosition position = codePositionManager.getCodePositionForUnit(stmt);
											if(dynamicValueBasedValuesToFuzz.containsKey(position.getID()))
												dynamicValueBasedValuesToFuzz.get(position.getID()).add(loggingPointValue);
											else {
												Set<Object> values = new HashSet<Object>();
												values.add(loggingPointValue);
												dynamicValueBasedValuesToFuzz.put(position.getID(), values);
											}
										}
																														
//										//SMT solver only returns hex-based UTF-8 values in some cases; we fixed this with our own hexToUnicode converter
//										if(loggingPointValue != null && loggingPointValue.contains("\\x")) 
//											addAdditionalUnicodeValue(loggingPointValue, values);
//										if(loggingPointValue != null)
//											values.add(loggingPointValue);
//										System.out.println(String.format("Extracted NEW DYNAMIC-BASED loggingpoint-value: %s", loggingPointValue));
										
										for(SMTUpdateInfo assertInfo : assertCombi) {
											//add values to fuzzy-seed
											Stmt stmt = assertInfo.getSourceOfDataflow();
											CodePosition position = codePositionManager.getCodePositionForUnit(stmt);
											if(dynamicValueBasedValuesToFuzz.containsKey(position.getID()))
												dynamicValueBasedValuesToFuzz.get(position.getID()).add(loggingPointValue);
											else {
												Set<Object> values = new HashSet<Object>();
												values.add(loggingPointValue);
												dynamicValueBasedValuesToFuzz.put(position.getID(), values);
											}
										}
									}
								}
																	
							}																							
						}																	 	
				 }
			 }
			 
			if(dynamicValueBasedValuesToFuzz.containsKey(codePosition)) {
				//we return all extracted values at once!
				 for(Iterator<Object> valueIt = dynamicValueBasedValuesToFuzz.get(codePosition).iterator(); valueIt.hasNext(); ) {
			        	Object valueToFuzz = valueIt.next();
			        	LoggerHelper.logEvent(MyLevel.SMT_SOLVER_VALUE, String.format("<---- dyn-values (first run) : " + valueToFuzz));			        	
						ServerResponse sResponse = new ServerResponse();
						sResponse.setAnalysisName(getAnalysisName());
						sResponse.setResponseExist(true);
						sResponse.setReturnValue(valueToFuzz);
				        AnalysisDecision finalDecision = new AnalysisDecision();
				        finalDecision.setAnalysisName(getAnalysisName());
				        finalDecision.setDecisionWeight(12);
				        finalDecision.setServerResponse(sResponse);
				        decisions.add(finalDecision);
				 }	
			}
			
			FrameworkOptions.forceTimeout = FrameworkOptions.forceTimeout - SMT_ADDITIONAL_COMPUTATION_TIME; 
			 
		}
		else if(dynamicValueBasedValuesToFuzz.containsKey(codePosition)) {
			//we return all extracted values at once!
			 for(Iterator<Object> valueIt = dynamicValueBasedValuesToFuzz.get(codePosition).iterator(); valueIt.hasNext(); ) {
		        	Object valueToFuzz = valueIt.next();
		        	LoggerHelper.logEvent(MyLevel.SMT_SOLVER_VALUE, String.format("<---- dyn-values: " + valueToFuzz));		        	
					ServerResponse sResponse = new ServerResponse();
					sResponse.setResponseExist(true);
					sResponse.setAnalysisName(getAnalysisName());
					sResponse.setReturnValue(valueToFuzz);
			        AnalysisDecision finalDecision = new AnalysisDecision();
			        finalDecision.setAnalysisName(getAnalysisName());
			        finalDecision.setDecisionWeight(12);
			        finalDecision.setServerResponse(sResponse);
			        decisions.add(finalDecision);
			 }	
		}
		//second all constant-based values
		else if(constantBasedValuesToFuzz.containsKey(codePosition) && !staticValuesAlreadySend(codePosition)) {
			staticValuesSent.add(codePosition);
			//we return all extracted values at once!
			 for(Iterator<Object> valueIt = constantBasedValuesToFuzz.get(codePosition).iterator(); valueIt.hasNext(); ) {
		        	Object valueToFuzz = valueIt.next();
		        	LoggerHelper.logEvent(MyLevel.SMT_SOLVER_VALUE, "<---- static-values: " + valueToFuzz);		        
					ServerResponse sResponse = new ServerResponse();
					sResponse.setResponseExist(true);
					sResponse.setAnalysisName(getAnalysisName());
					sResponse.setReturnValue(valueToFuzz);
			        AnalysisDecision finalDecision = new AnalysisDecision();
			        finalDecision.setAnalysisName(getAnalysisName());
			        finalDecision.setDecisionWeight(8);
			        finalDecision.setServerResponse(sResponse);
			        decisions.add(finalDecision);
			 }	
		}
		
		//no decision found
		if(decisions.isEmpty()) {
			ServerResponse sResponse = new ServerResponse();
			sResponse.setResponseExist(false);
			sResponse.setAnalysisName(getAnalysisName());
			AnalysisDecision noDecision = new AnalysisDecision();
			noDecision.setAnalysisName(getAnalysisName());
	        noDecision.setDecisionWeight(8);
	        noDecision.setServerResponse(sResponse);
	        return Collections.singletonList(noDecision);
		}
		
		return decisions;
	}
	
	
	private boolean areNewDynValuesOfHistory(ClientHistory history) {
		boolean dynValueCheckNecessary = true;
		if(history != null) {
			Set<DynamicValue> currValues = history.getDynamicValues().getValues();
			for(Set<DynamicValue> values : dynValuesOfRuns) {
				for(DynamicValue value : values) {
				if(!currValues.contains(value))
					break;
				}
				dynValueCheckNecessary = false;
			}
		}
		return dynValueCheckNecessary;
	}
	
	
	private boolean staticValuesAlreadySend(int codePosition) {
		return staticValuesSent.contains(codePosition);
	}
	
	
	private void addAdditionalUnicodeValue(String loggingPointValue, Set<Object> values) {
		String delim = "#################################################################################################################";
		List<String> allStrings = new ArrayList<String>();
		List<String> hexValues = new ArrayList<String>();
		
		String currentHexValue = "";
		String currentNormalString = "";
		
		for(int i = 0; i < loggingPointValue.length(); i++) {
			char c = loggingPointValue.charAt(i);
			if(c == '\\') {
				if(loggingPointValue.charAt(i+1) == 'x') {
					if(currentNormalString.length() > 0) {
						allStrings.add(new String(currentNormalString));
						currentNormalString = "";
					}
					i = i+2;
					//look ahead
					currentHexValue += loggingPointValue.charAt(i);
					++i;
					currentHexValue += loggingPointValue.charAt(i);
				}
				else {
					if(currentHexValue.length() > 0) {
						hexValues.add(new String(currentHexValue));
						allStrings.add(delim);
						currentHexValue = "";
					}
					currentNormalString += c;
					
				}
			}
			else{
				if(currentHexValue.length() > 0) {
					hexValues.add(new String(currentHexValue));
					allStrings.add(delim);
					currentHexValue = "";
				}
				currentNormalString += c;
			}
			
			//last values
			if(i+1 == loggingPointValue.length()) {
				if(currentHexValue.length() > 0){
					hexValues.add(new String(currentHexValue));
					allStrings.add(delim);
					currentHexValue = "";
				}
				if(currentNormalString.length() > 0){
					allStrings.add(new String(currentNormalString));
					currentNormalString = "";
				}
			}
					
		}
		
		
		for(String hexValue : hexValues) {
			byte[] tmp1;
			String newValue = null;
			try {
				tmp1 = Hex.decodeHex(hexValue.toCharArray());
				newValue = new String(tmp1, "UTF-8");
				int replaceIndex = allStrings.indexOf(delim);
				allStrings.set(replaceIndex, new String(newValue));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		String newValue = "";
		for(String string : allStrings)
			newValue += string;

		
		if(!newValue.equals("")){
			values.add(newValue);
			System.out.println(String.format("Extracted loggingpoint-value: %s", newValue));
		}
	}
		
	
	
	private void runAnalysis(final Set<Unit> targetUnits) {
		try {
			Scene.v().getOrMakeFastHierarchy();
			
			InplaceInfoflow infoflow = new InplaceInfoflow();
//			InfoflowConfiguration.setAccessPathLength(2);
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
			
			infoflow.addResultsAvailableHandler(new FuzzerResultsAvailableHandler(pmp.getSources(),
					targetUnits));
			infoflow.runAnalysis(srcSinkManager);
		}
		catch (IOException ex) {
			throw new RuntimeException("Could not read source/sink file", ex);
		}
	}

	@Override
	public void reset() {
		
	}
	
	
	private String fixSMTSolverIntegerOutput(String loggingPoint, Stmt stmt) {
		if(stmt.containsInvokeExpr()) {
			InvokeExpr inv = stmt.getInvokeExpr();
			String metSig = inv.getMethod().getSignature();
			if(metSig.equals("<android.telephony.TelephonyManager: java.lang.String getSimOperator()>") 
					|| metSig.equals("<android.telephony.TelephonyManager: java.lang.String getNetworkOperator()>")
				) {
				String newLoggingPoint = "";
				for(char c : loggingPoint.toCharArray()) {
					if(c < '0' || c > '9') {
						Random rand = new Random();
						int num = rand.nextInt(10);
						newLoggingPoint += num;
					}
					else
						newLoggingPoint += c;
				}
				return newLoggingPoint;				
			}
		}
		return loggingPoint;
	}
	
	
	private boolean isSemanticallyCorrect(String loggingPoint, Stmt stmt) {
		if(loggingPoint == null)
			return false;
		if(stmt.containsInvokeExpr()) {
			InvokeExpr inv = stmt.getInvokeExpr();
			String metSig = inv.getMethod().getSignature();
			if(metSig.equals("<android.telephony.TelephonyManager: java.lang.String getSimOperator()>") 
					|| metSig.equals("<android.telephony.TelephonyManager: java.lang.String getNetworkOperator()>")
				) {
				for(char c : loggingPoint.toCharArray()) {
					if(c < '0' || c > '9') 
						return false;
				}
			}
		}
		return true;
	}
	
	
	private Table<SMTProgram, Stmt, List<Pair<DynamicValueInformation, DynamicValue>>> getSMTProgramUpdateInfos(DynamicValueContainer dynamicValues) {
		Table<SMTProgram, Stmt, List<Pair<DynamicValueInformation, DynamicValue>>> updateInfoTable = HashBasedTable.create();
				
		for(DynamicValue value : dynamicValues.getValues()) {		
			Unit unit = codePositionManager.getUnitForCodePosition(value.getCodePosition()+1);
			int paramIdx = value.getParamIdx();
			
			for(Map.Entry<SMTProgram, Set<DynamicValueInformation>> entry : dynamicValueInfos.entrySet()) {
				for(DynamicValueInformation valueInfo : entry.getValue()) {
					if(valueInfo.getStatement().equals(unit)) {
						//base object
						if(paramIdx == -1) {
							if(valueInfo.isBaseObject()) {								
								if(!updateInfoTable.contains(entry.getKey(), valueInfo.getStatement())) 
									updateInfoTable.put(entry.getKey(), valueInfo.getStatement(), new ArrayList<Pair<DynamicValueInformation, DynamicValue>>());
								updateInfoTable.get(entry.getKey(), valueInfo.getStatement()).add(new Pair<DynamicValueInformation, DynamicValue>(valueInfo, value));		
													
							}
						}
						//method arguments
						else{
							if(valueInfo.getArgPos() == paramIdx) {
								if(!updateInfoTable.contains(entry.getKey(), valueInfo.getStatement())) 
									updateInfoTable.put(entry.getKey(), valueInfo.getStatement(), new ArrayList<Pair<DynamicValueInformation, DynamicValue>>());
								updateInfoTable.get(entry.getKey(), valueInfo.getStatement()).add(new Pair<DynamicValueInformation, DynamicValue>(valueInfo, value));	
							}
						}
					}
				}
			}
		}

		return updateInfoTable;
	}
	

	
	private Set<Set<SMTUpdateInfo>> getAllAssertCombinations(Map<Stmt, List<Pair<DynamicValueInformation, DynamicValue>>> map) {
		Set<Set<SMTUpdateInfo>> allAssertions = new HashSet<Set<SMTUpdateInfo>>();
		int[] currentPos = new int[map.keySet().size()];
		List<Stmt> keys = new ArrayList<Stmt>(map.keySet());
		List<Integer> maxSize = new ArrayList<Integer>();
		for(Stmt stmt : keys) 
			maxSize.add(map.get(stmt).size());
				
		List<int[]> allPermutations = new ArrayList<int[]>();		
		generateAllPermutations(maxSize, currentPos, currentPos.length-1, allPermutations);

		
		for(int[] combinations : allPermutations) {
			Set<SMTUpdateInfo> currentAssertions = new HashSet<SMTUpdateInfo>();
			for(int i = 0; i < combinations.length; i++) {
				Stmt stmt = keys.get(i);
				Pair<DynamicValueInformation, DynamicValue> valueInfo = map.get(stmt).get(combinations[i]);
				
				SMTSimpleAssignment assignment = null;
				DynamicValue dynValue = valueInfo.getSecond();
				SMTBinding bindingToUpdate = valueInfo.getFirst().getBinding();
				if(dynValue instanceof DynamicStringValue) {
					String stringValue = ((DynamicStringValue)dynValue).getStringValue();
					assignment = new SMTSimpleAssignment(bindingToUpdate, new SMTConstantValue<String>(stringValue));
				}
				else if(dynValue instanceof DynamicIntValue) {
					int intValue = ((DynamicIntValue)dynValue).getIntValue();
					assignment = new SMTSimpleAssignment(bindingToUpdate, new SMTConstantValue<Integer>(intValue));
				}
									
				SMTAssertStatement assignAssert = new SMTAssertStatement(assignment);
				currentAssertions.add(new SMTUpdateInfo(assignAssert, stmt, valueInfo.getFirst().getSourceOfDataflow()));				
			}
			allAssertions.add(currentAssertions);
		}		
		
		return allAssertions;
	}
	
	
	public void generateAllPermutations(List<Integer> maxSize, int[] currArray, int currIndex, List<int[]> allPermutations) {
		if(currIndex == -1)
			return;
		int startPos = currArray.length - 1;
		int currValue = currArray[startPos];
		if(currValue+1 < maxSize.get(startPos)) {
			currArray[startPos] = currArray[startPos] + 1;
			allPermutations.add(currArray.clone());
		}
		else {
			currValue = currArray[currIndex];
			//increment index
			if(currValue+1 < maxSize.get(currIndex)) {
				currArray[currIndex] = currArray[currIndex] + 1;
				for(int i = currIndex+1; i < currArray.length; i++)
					currArray[i] = 0;
				allPermutations.add(currArray.clone());
			}
			else{
				//find next index to update
				for(; currIndex >= 0; currIndex--) {
					currValue = currArray[currIndex];
					if(currValue+1 < maxSize.get(currIndex))
						break;
				}
				if(currIndex == -1)
					return;
				currArray[currIndex] = currArray[currIndex] + 1;
				for(int i = currIndex+1; i < currArray.length; i++)
					currArray[i] = 0;
				currIndex = currArray.length-1;
				allPermutations.add(currArray.clone());
			}
		}

		generateAllPermutations(maxSize, currArray, currIndex, allPermutations);
		
	}

	
	
//	public class DynamicValueWorker implements DynamicValueUpdateHandler {
//		
//		private final ThreadTraceManager traceManager;
//		
//		public DynamicValueWorker(ThreadTraceManager manager) {
//			this.traceManager = manager;
//		}
//
//		@Override
//		public void onDynamicValueAvailable(DynamicValue dynValue, int lastExecutedStatement) {
//			int paramIdx = dynValue.getParamIdx();
//			//depending on the argPos or the baseObject, we need to create a new value
//			 Map<SMTProgram, Set<DynamicValueInformation>> updateInfo = getSMTProgramUpdateInfos(lastExecutedStatement, paramIdx);
//			//we need a new SMT run for extracting a more precise value
//			if(!updateInfo.isEmpty()) {						
//				for(Map.Entry<SMTProgram, Set<DynamicValueInformation>> entry : updateInfo.entrySet()) {
//					for(DynamicValueInformation valueInfo : entry.getValue()) {
//						SMTBinding bindingToUpdate = valueInfo.getBinding();
//						SMTProgram smtProg = entry.getKey();
//						
//						SMTSimpleAssignment assignment = null;
//						if(dynValue instanceof DynamicStringValue) {
//							String stringValue = ((DynamicStringValue)dynValue).getStringValue();
//							assignment = new SMTSimpleAssignment(bindingToUpdate, new SMTConstantValue<String>(stringValue));
//							System.out.println("+++++++: " + lastExecutedStatement + ": " + stringValue);
//						}
//						else if(dynValue instanceof DynamicIntValue) {
//							int intValue = ((DynamicIntValue)dynValue).getIntValue();
//							assignment = new SMTSimpleAssignment(bindingToUpdate, new SMTConstantValue<Integer>(intValue));
//							System.out.println("+++++++: " + lastExecutedStatement + ": " + intValue);
//						}
//											
//						SMTAssertStatement assignAssert = new SMTAssertStatement(assignment);					
//						smtProg.addAssertStatement(assignAssert);
//						
//						
//						File z3str2Script = new File(FrameworkOptions.Z3SCRIPT_LOCATION);
//						if(!z3str2Script.exists())
//							throw new RuntimeException("There is no z3-script available");
//						SMTExecutor smtExecutor = new SMTExecutor(Collections.singleton(smtProg), z3str2Script);
//						Set<File> smtFiles = smtExecutor.createSMTFile();
//						
//						//we need to remove it. if there are more dynamic values available, we need to get the clean
//						//old program for the solver
//						smtProg.removeAssertStatement(assignAssert);
//						
//						Set<Object> values = new HashSet<Object>();
//						for(File smtFile : smtFiles) {						
//							String loggingPointValue = smtExecutor.executeZ3str2ScriptAndExtractLoggingPointValue(smtFile);
//
//							if(isSemanticallyCorrect(loggingPointValue, valueInfo.getSourceOfDataflow())) {																		
//								//SMT solver only returns hex-based UTF-8 values in some cases; we fixed this with our own hexToUnicode converter
//								if(loggingPointValue != null && loggingPointValue.contains("\\x")) 
//									addAdditionalUnicodeValue(loggingPointValue, values);
//								if(loggingPointValue != null)
//									values.add(loggingPointValue);
//								System.out.println(String.format("Extracted NEW DYNAMIC-BASED loggingpoint-value: %s", loggingPointValue));
//							}
//						}
//						
//						//add values to fuzzy-seed
//						Stmt stmt = valueInfo.getSourceOfDataflow();
//						CodePosition position = codePositionManager.getCodePositionForUnit(stmt);
//						if(dynamicValueBasedValuesToFuzz.containsKey(position.getID()))
//							dynamicValueBasedValuesToFuzz.get(position.getID()).addAll(values);
//						else
//							dynamicValueBasedValuesToFuzz.put(position.getID(), values);										
//					}
//				}
//			}
//			
//		}
//	}
}
