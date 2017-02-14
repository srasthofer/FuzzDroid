package de.tu_darmstadt.sse.decisionmaker.analysis.smartconstantdataextractor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.BooleanType;
import soot.Local;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.Stmt;
import soot.jimple.infoflow.InfoflowConfiguration;
import soot.jimple.infoflow.data.AccessPath;
import soot.jimple.infoflow.results.InfoflowResults;
import soot.jimple.infoflow.results.ResultSinkInfo;
import soot.jimple.infoflow.results.ResultSourceInfo;
import soot.jimple.infoflow.solver.cfg.IInfoflowCFG;
import de.tu_darmstadt.sse.FrameworkOptions;
import de.tu_darmstadt.sse.decisionmaker.analysis.symbolicexecution.ControlFlowPath;
import de.tu_darmstadt.sse.decisionmaker.analysis.symbolicexecution.UtilSMT;
import de.tu_darmstadt.sse.sharedclasses.util.Pair;


public class SMTPreparationPhase {
	private final IInfoflowCFG cfg;
	private final InfoflowResults results;
	
	public SMTPreparationPhase(IInfoflowCFG cfg, InfoflowResults results) {
		this.cfg = cfg;
		this.results = results;
	}
	
	
	public Set<ResultSourceInfo> prepareDataFlowPathsForSMTConverter() {	
		//This step is necessary for storing the ResultSourceInfo elements into a set
		//The result ResultSourceInfo object does only represent a source and not the dataflow.
		//But with the PathAgnosticResults flag, one can force the ResultSourceInfo object 
		//to consider the path (see equals method)
		InfoflowConfiguration.setPathAgnosticResults(false);
		
		//control flow involved
		return prepareDataFlowsDependingOnControlFlow(results, FrameworkOptions.mergeDataFlows);
	}
	
	
	private Set<ResultSourceInfo> prepareDataFlowsDependingOnControlFlow(InfoflowResults results, boolean mergeDataFlows) {
		Set<ResultSourceInfo> dataFlows = new HashSet<ResultSourceInfo>();
		
		for (ResultSinkInfo sink : results.getResults().keySet()) {			
			for (ResultSourceInfo source : results.getResults().get(sink)) {
				for(Stmt stmt : source.getPath()) {
					System.out.println("######"+ stmt + "\n");
				}
				dataFlows.add(source);
			}
			System.out.println("###################");
		}
		
		//Remove dataflows where all the elements in a particular dataflow are included in another dataflow
		Set<ResultSourceInfo> minimalDataFlowSet = new HashSet<ResultSourceInfo>();
		for (ResultSourceInfo dataflow1 : dataFlows) {
			List<Stmt> flow1 = new ArrayList<Stmt>(Arrays.asList(dataflow1.getPath()));
			boolean addFlow1 = true;
			for (ResultSourceInfo dataflow2 : dataFlows) {
				List<Stmt> flow2 = new ArrayList<Stmt>(Arrays.asList(dataflow2.getPath()));
				
				if(dataflow1 != dataflow2 && flow2.containsAll(flow1))
					addFlow1 = false;
			}
			if(addFlow1)
				minimalDataFlowSet.add(dataflow1);
		}
		
		
		Map<Stmt, AccessPath> allStmtsFromAllDataFlowPaths = new HashMap<Stmt, AccessPath>();
		//Merge all dataflow-elements together for a faster search
		for (ResultSourceInfo dataflow : minimalDataFlowSet) {
			List<Stmt> statementsDataFlow = new ArrayList<Stmt>(Arrays.asList(dataflow.getPath()));
			List<AccessPath> accessPathDataFlow = new ArrayList<AccessPath>(Arrays.asList(dataflow.getPathAccessPaths()));
			
			assert statementsDataFlow.size() == accessPathDataFlow.size();
			
			for(int i = 0; i < statementsDataFlow.size(); i++) {
				Stmt key = statementsDataFlow.get(i);
				AccessPath value = accessPathDataFlow.get(i);
				allStmtsFromAllDataFlowPaths.put(key, value);
			}
		}

				
		Set<ResultSourceInfo> allDataFlows = new HashSet<ResultSourceInfo>();
		int tmp_counter_removeMe = 0;
		for (ResultSourceInfo dataflow1 : minimalDataFlowSet) {										
			long completeTimePath = System.currentTimeMillis();
			
			List<Stmt> statementsDataFlow1 = new ArrayList<Stmt>(Arrays.asList(dataflow1.getPath()));
			List<AccessPath> accessPathDataFlow1 = new ArrayList<AccessPath>(Arrays.asList(dataflow1.getPathAccessPaths()));
			
			//if one wants to be more precise, he needs to merge the dataflows for a more precise contraint
			if(mergeDataFlows){						
				for(int indexDataflow1Statements = 0; indexDataflow1Statements < statementsDataFlow1.size() - 1; indexDataflow1Statements++) {								
					Stmt fromStmt_Dataflow1 = statementsDataFlow1.get(indexDataflow1Statements);
					Stmt toStmt_Dataflow1 = statementsDataFlow1.get(indexDataflow1Statements+1);
					
					Set<ControlFlowPath> allCFGPathsBetweenTwoUnitsOfDataflow1 = UtilSMT.getControlFlowPathsBetweenTwoDataFlowStmts(fromStmt_Dataflow1, toStmt_Dataflow1, cfg);								
					
					for(ControlFlowPath singlePath : allCFGPathsBetweenTwoUnitsOfDataflow1) {
						List<Unit> singleCfgPathDataflow1BetweenTwoStmts = singlePath.getPath();
						
						int additionalOffset = 0;
						for(Map.Entry<Stmt, AccessPath> entry : allStmtsFromAllDataFlowPaths.entrySet()) {
							
							
							Stmt betweenStmt = entry.getKey();
							AccessPath betweenStmt_AccessPath = entry.getValue();
							if(singleCfgPathDataflow1BetweenTwoStmts.contains(betweenStmt) &&
									!statementsDataFlow1.contains(betweenStmt)) {
								//special treatment in case of a condition
								if(betweenStmt.containsInvokeExpr() &&
										betweenStmt instanceof AssignStmt &&
										betweenStmt.getInvokeExpr().getType() instanceof BooleanType) {									
									
									//betweenStmt_Dataflow2 = conditional statement
									boolean mergeCondition = UtilSMT.needToAddConditionalUnit(cfg, toStmt_Dataflow1, betweenStmt);								 								
																			
									if(mergeCondition) {											
										statementsDataFlow1.add(indexDataflow1Statements+1+additionalOffset, betweenStmt);
										accessPathDataFlow1.add(indexDataflow1Statements+1+additionalOffset, betweenStmt_AccessPath);
										++additionalOffset;
									}																										
								}
								//in case of a linear (no condition) path, just add the new statement to dataflow1
								else {
									statementsDataFlow1.add(indexDataflow1Statements+1+additionalOffset, betweenStmt);
									accessPathDataFlow1.add(indexDataflow1Statements+1+additionalOffset, betweenStmt_AccessPath);
									++additionalOffset;
								}
							}						
						}	
					}
				}
			}
			ResultSourceInfo tmp = new ResultSourceInfo(accessPathDataFlow1.get(0), statementsDataFlow1.get(0), null, statementsDataFlow1, accessPathDataFlow1);
			allDataFlows.add(tmp);
			completeTimePath = System.currentTimeMillis() - completeTimePath;
			System.out.println(String.format("[%s / %s] (%s path elements)\n\t time: %s ms", tmp_counter_removeMe, minimalDataFlowSet.size(), statementsDataFlow1.size(), completeTimePath));	
			++tmp_counter_removeMe;
		}
		
		
		
		/*
		for (ResultSourceInfo dataflow1 : dataFlows) {
			List<Stmt> statementsDataFlow1 = new ArrayList<Stmt>(Arrays.asList(dataflow1.getPath()));
			List<AccessPath> accessPathDataFlow1 = new ArrayList<AccessPath>(Arrays.asList(dataflow1.getPathAccessPaths()));
			
			for(int indexDataflow1Statements = 0; indexDataflow1Statements < statementsDataFlow1.size() - 1; indexDataflow1Statements++) {
				Stmt fromStmt_Dataflow1 = statementsDataFlow1.get(indexDataflow1Statements);
				Stmt toStmt_Dataflow1 = statementsDataFlow1.get(indexDataflow1Statements+1);
				
				Set<ControlFlowPath> allCFGPathsBetweenTwoUnitsOfDataflow1 = UtilSMT.getControlFlowPathsBetweenTwoDataFlowStmts(fromStmt_Dataflow1, toStmt_Dataflow1, cfg);
				
//				if(allCFGPathsBetweenTwoUnitsOfDataflow1.size() > 1)
//					throw new RuntimeException("todo");
					
				int countCFGsBetweenTwUnits = 0;
				for(ControlFlowPath singlePath : allCFGPathsBetweenTwoUnitsOfDataflow1) {
					List<Unit> singleCfgPathDataflow1BetweenTwoStmts = singlePath.getPath();
					
					for (ResultSourceInfo dataflow2 : dataFlows) {
						
						if(dataflow1 != dataflow2) {
							Stmt[] statementsDataFlow2 = dataflow2.getPath();
							AccessPath[] accessPathDataFlow2 = dataflow2.getPathAccessPaths();													
							
							//in case there are more than 1 statement to add to dataflow1, we also have to update the
							//index of dataflow-statemetents1. We do this with an additional offset variable
							int additionalOffset = 0;
							for(int indexDataflow2Statements = 0; indexDataflow2Statements < statementsDataFlow2.length; indexDataflow2Statements++) {							
								Stmt betweenStmt_Dataflow2 = statementsDataFlow2[indexDataflow2Statements];
								AccessPath betweenStmt_Dataflow2_AccessPath = accessPathDataFlow2[indexDataflow2Statements];
								
								if(singleCfgPathDataflow1BetweenTwoStmts.contains(betweenStmt_Dataflow2) &&
										!statementsDataFlow1.contains(betweenStmt_Dataflow2)) {
									//special treatment in case of a condition
									if(betweenStmt_Dataflow2.containsInvokeExpr() &&
											betweenStmt_Dataflow2 instanceof AssignStmt &&
											betweenStmt_Dataflow2.getInvokeExpr().getType() instanceof BooleanType) {										
										//betweenStmt_Dataflow2 = conditional statement
										boolean mergeCondition = UtilSMT.needToAddConditionalUnit(cfg, toStmt_Dataflow1, betweenStmt_Dataflow2);
																				
										if(mergeCondition) {											
											statementsDataFlow1.add(indexDataflow1Statements+1+additionalOffset, betweenStmt_Dataflow2);
											accessPathDataFlow1.add(indexDataflow1Statements+1+additionalOffset, betweenStmt_Dataflow2_AccessPath);
											++additionalOffset;
//											//This step is the weakness in this analysis
//											//It does ALWAYS assume that the conditional-stmt is directly used by the condition
//											//We either improve this with a slicing approach or we may produce wrong contraints
//											LoggerHelper.logInfo("We might produce a wrong constraint here...");
//											if(conditionsForReachingTarget.size() == 1) {
//												AssignStmt conditionAssignment = (AssignStmt)betweenStmt_Dataflow2;
//												int condition = (conditionsForReachingTarget.iterator().next() == true) ? 1 : 0;
//												Value booleanLocalVariable = conditionAssignment.getLeftOp();
//												
//												Stmt newAssignStmt = Jimple.v().newAssignStmt(booleanLocalVariable, IntConstant.v(condition));
//												
//												statementsDataFlow1.add(indexDataflow1Statements+2, newAssignStmt);
//												accessPathDataFlow1.add(indexDataflow1Statements+2, null);
//											}
//											else
//												throw new RuntimeException("todo");
										}																										
									}
									//in case of a linear (no condition) path, just add the new statement to dataflow1
									else {
										statementsDataFlow1.add(indexDataflow1Statements+1+additionalOffset, betweenStmt_Dataflow2);
										accessPathDataFlow1.add(indexDataflow1Statements+1+additionalOffset, betweenStmt_Dataflow2_AccessPath);
										++additionalOffset;
									}																		
								}
							}
						}
					}
					++countCFGsBetweenTwUnits;
				}
			}
			
			ResultSourceInfo tmp = new ResultSourceInfo(accessPathDataFlow1.get(0), statementsDataFlow1.get(0), null, statementsDataFlow1, accessPathDataFlow1);
			allDataFlows.add(tmp);
		}
		*/		
		allDataFlows = UtilSMT.removeDuplicatedFlows(allDataFlows);		
		return allDataFlows;
	}
	
	
	
	private static ResultSourceInfo mergeDataFlowsIntoSingleDataFlow(Stmt statementToEnrich, ResultSourceInfo originalPath, ResultSourceInfo pathToMerge) {		
		List<Stmt> pathStmts = new ArrayList<Stmt>(Arrays.asList(originalPath.getPath()));
		List<AccessPath> accessPaths = new ArrayList<AccessPath>(Arrays.asList(originalPath.getPathAccessPaths()));
		
		List<Stmt> pathToMergeStmts = new ArrayList<Stmt>(Arrays.asList(pathToMerge.getPath()));
		List<AccessPath> pathToMergeAccessPaths = new ArrayList<AccessPath>(Arrays.asList(pathToMerge.getPathAccessPaths()));
		
		
		
		int index = pathStmts.indexOf(statementToEnrich);
//		if(index < 0)
//			throw new RuntimeException("Woops, there is something wonkey here");
//		
//		for(int i = 0; i < pathToMergeStmts.size(); i++) {
//			pathStmts.add(index, pathToMergeStmts.get(i));
//			accessPaths.add(index, pathToMergeAccessPaths.get(i));
//			index +=1;
//		}
		
		
		
		List<Pair<Stmt,AccessPath>> dataToMerge = new ArrayList<Pair<Stmt,AccessPath>>();
		
		int position;
		for(position = 0; position < pathToMergeStmts.size(); position++) {			
			if(pathStmts.contains(pathToMergeStmts.get(position)) && !dataToMerge.isEmpty()) {
				int indexToInsertBefore = pathStmts.indexOf(pathToMergeStmts.get(position));
				indexToInsertBefore -= 1;
				
//				for(Pair<Stmt,AccessPath> pair : dataToMerge) {
//					pathStmts.add(indexToInsertBefore, pair.getFirst());
//					accessPaths.add(indexToInsertBefore, pair.getSecond());
//					++indexToInsertBefore;
//				}
			}						
			else if(!pathStmts.contains(pathToMergeStmts.get(position))) {
				dataToMerge.add(new Pair<Stmt,AccessPath>(pathToMergeStmts.get(position), pathToMergeAccessPaths.get(position)));
			}
		}
		
		if(!dataToMerge.isEmpty()) {
			for(Pair<Stmt,AccessPath> pair : dataToMerge) {
				pathStmts.add(index, pair.getFirst());
				accessPaths.add(index, pair.getSecond());
				++index;
			}
		}
		
		return new ResultSourceInfo(accessPaths.get(0), pathStmts.get(0), null, pathStmts, accessPaths);
	}
	
	
	private ResultSourceInfo findDataFlowPathForSink(Stmt sinkStmt, Local sinkLokal, List<ResultSourceInfo> allDataFlows) {
		for(ResultSourceInfo singleFlow : allDataFlows){
			Stmt[] statements = singleFlow.getPath();
			AccessPath[] accessPath = singleFlow.getPathAccessPaths();
			
			for(int i = 0; i < statements.length; i++) {	
				Stmt currentStmt = statements[i];
				if(currentStmt == sinkStmt) {
					if(accessPath[i].getPlainValue() == sinkLokal)
						return singleFlow;
				}
				
				else if(currentStmt instanceof AssignStmt) {
					AssignStmt assignStmt = (AssignStmt)currentStmt;
					Value lhs = assignStmt.getLeftOp();
				
					if(lhs == sinkLokal)						
						return singleFlow;		
				}
			}
		}
		return null;
	}
}
