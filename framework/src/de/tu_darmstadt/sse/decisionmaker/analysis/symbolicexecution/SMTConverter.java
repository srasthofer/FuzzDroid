package de.tu_darmstadt.sse.decisionmaker.analysis.symbolicexecution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import soot.BooleanType;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.Jimple;
import soot.jimple.Stmt;
import soot.jimple.infoflow.data.AccessPath;
import soot.jimple.infoflow.results.ResultSourceInfo;
import soot.jimple.infoflow.solver.cfg.IInfoflowCFG;
import soot.jimple.infoflow.source.data.SourceSinkDefinition;
import de.tu_darmstadt.sse.appinstrumentation.UtilInstrumenter;
import de.tu_darmstadt.sse.appinstrumentation.transformer.InstrumentedCodeTag;
import de.tu_darmstadt.sse.commandlinelogger.LoggerHelper;
import de.tu_darmstadt.sse.commandlinelogger.MyLevel;
import de.tu_darmstadt.sse.decisionmaker.analysis.dynamicValues.DynamicValueInformation;
import de.tu_darmstadt.sse.decisionmaker.analysis.smartconstantdataextractor.NotYetSupportedException;
import de.tu_darmstadt.sse.decisionmaker.analysis.symbolicexecution.datastructure.SMTBinding;
import de.tu_darmstadt.sse.decisionmaker.analysis.symbolicexecution.datastructure.SMTProgram;
import de.tu_darmstadt.sse.sharedclasses.util.Pair;


public class SMTConverter {	
	//all source definitions
	private Set<SourceSinkDefinition> sources;
	private Set<SMTProgram> smtPrograms;
	private Set<ResultSourceInfo> preparedDataFlowsForSMT;
	private Map<SMTProgram, Set<DynamicValueInformation>> dynamicValueInfos = new HashMap<SMTProgram, Set<DynamicValueInformation>>();
	
	public SMTConverter(Set<SourceSinkDefinition> sources) {
		this.sources = sources;
	}
	
	public void convertJimpleToSMT(Stmt[] jimpleDataFlowStatements,
			AccessPath[] accessPathPath, Set<Unit> targetUnits, IInfoflowCFG cfg, Table<Stmt, Integer, Set<String>> splitInfos) {
		
		//if the path contains a condition (e.g. a = b.equals("aa")), we over-approximate here and add a new statement which
		//assigns once true (a = true) and once false (a = false) to the left hand side of the assignment
		//this is an over-approximation and is necessary for the SMT solver
		List<Integer> indexesForConditions = new ArrayList<Integer>();
		for(int i = 0; i <  jimpleDataFlowStatements.length; i++) {
			Stmt stmt = jimpleDataFlowStatements[i];
			
			if(stmt instanceof AssignStmt) {
				AssignStmt assignment = (AssignStmt)stmt;
				if(assignment.getRightOp() instanceof InvokeExpr){
					InvokeExpr invoke = (InvokeExpr)assignment.getRightOp();
					if(!UtilInstrumenter.isAppDeveloperCode(invoke.getMethod().getDeclaringClass())){
						if(assignment.getRightOp().getType() == BooleanType.v()) {
							indexesForConditions.add(i);
						} 
					}
				}				
			}			
		}
		
		int amountConditions = indexesForConditions.size();
		
		Map<Integer, List<Integer>> truthTable = generateTruthTable(amountConditions);
				
		List<List<Stmt>> allDataFlows = new ArrayList<List<Stmt>>();
		List<List<AccessPath>> allAccessPath = new ArrayList<List<AccessPath>>();
		if(amountConditions == 0) {
			allDataFlows.add(Arrays.asList(jimpleDataFlowStatements));
			allAccessPath.add(Arrays.asList(accessPathPath));
		}
		else {
			for(Map.Entry<Integer, List<Integer>> entry : truthTable.entrySet()) {
				ArrayList<Stmt> currentDataflow = new ArrayList<Stmt> (Arrays.asList(jimpleDataFlowStatements));
				ArrayList<AccessPath> currentAccessPath = new ArrayList<AccessPath> (Arrays.asList(accessPathPath));
				allDataFlows.add(currentDataflow);
				allAccessPath.add(currentAccessPath);
				
				for(int i = 0; i < entry.getValue().size(); i++) {
					int indexToAdd = indexesForConditions.get(i);
					
					Stmt conditionalStmt = jimpleDataFlowStatements[indexToAdd];
					assert conditionalStmt instanceof AssignStmt;
					AssignStmt assignment = (AssignStmt)conditionalStmt;
					Value lhs = assignment.getLeftOp();
					
					int booleanValue = entry.getValue().get(i);
					IntConstant boolValueJimple = IntConstant.v(booleanValue);
					
					AssignStmt stmtToAdd = Jimple.v().newAssignStmt(lhs, boolValueJimple);
					
					currentDataflow.add(indexToAdd+1, stmtToAdd);
					currentAccessPath.add(indexToAdd+1, currentAccessPath.get(indexToAdd));
				}
			}
		}
		
		
		//###### end condition treatment #######
		
		//##### begin treatment of flows containing the split API method #####				
		Table<List<Stmt>, Stmt, List<List<String>>> splitAPIElementInfos = HashBasedTable.create();
		
		for(Stmt stmt : jimpleDataFlowStatements) {
			if(stmt.containsInvokeExpr()) {
				InvokeExpr inv = stmt.getInvokeExpr();
				
				if(inv.getMethod().getSignature().equals("<java.lang.String: java.lang.String[] split(java.lang.String)>")) {
					if(splitInfos.containsRow(stmt)) {
						//count all combinations, e.g. key{0,1}, value{foo, bar} = 2 x 2 = 4
						//this is important for generating different SMT programs. In this case 4 equal dataflows, but once
						//the split-API call get converted into an SMT formula, we return one key-value combination, e.g. {0, foo}  												
						
						List<List<String>> allElements = new ArrayList<List<String>>();
						List<Integer> initPos = new ArrayList<Integer>();
						for(Integer index : splitInfos.row(stmt).keySet()) {
							//add init value for position (will be used later)
							initPos.add(0);
							
							List<String> splitValues = new ArrayList<String>();
							for(String indexValue : splitInfos.get(stmt, index)) {
								splitValues.add(indexValue);
							}
							allElements.add(splitValues);
						}
						List<List<String>> resultElementCombinations = new ArrayList<List<String>>();
						
						generateAllSplitAPICombinations(allElements, initPos, resultElementCombinations);																											
						int countCominations = resultElementCombinations.size();						
						
						//prepare the dataflows:
						
						//duplicate amount of countCombinations for dataflows. The correct values will be added to the dataflow once
						//in the {@generateSMTSplitStmt} method.
						for(int i = 0; i < countCominations-1; i++) {
							ArrayList<Stmt> currentDataflow = new ArrayList<Stmt> (Arrays.asList(jimpleDataFlowStatements));
							ArrayList<AccessPath> currentAccessPath = new ArrayList<AccessPath> (Arrays.asList(accessPathPath));
							allDataFlows.add(currentDataflow);
							allAccessPath.add(currentAccessPath);
							
							//add all necessary split-information for this particular dataflow
							splitAPIElementInfos.put(currentDataflow, stmt, resultElementCombinations);
						}
					}
				}
					
			}
		}
		
		//##### end treatment of flows containing the split API method #####
		
		
		smtPrograms = new HashSet<SMTProgram>();
		for(int i = 0; i < allDataFlows.size(); i++) {
			List<Stmt> dataflow = allDataFlows.get(i);
			List<AccessPath> accessPath = allAccessPath.get(i);
			JimpleStmtVisitorImpl stmtVisitor = new JimpleStmtVisitorImpl(sources, dataflow, accessPath, targetUnits, cfg, splitAPIElementInfos);
			try{
				for(Stmt stmt : dataflow) {
					//does not make any sense to apply our own instrumented code
					if(!stmt.hasTag(InstrumentedCodeTag.name)) {						
						stmt.apply(stmtVisitor);
						
						//in case we do not support a specific statement yet, we will not produce any SMT program!
						if(stmtVisitor.notSupported)
							break;
					}
				}
				//in case we do not support a specific statement yet, we will not produce any SMT program!
				if(stmtVisitor.notSupported) {
					LoggerHelper.logWarning("SMT formular is not generated!");
					continue;
				}
				smtPrograms.addAll(stmtVisitor.getSMTPrograms());
				
				//add the dataflow source info to the DynamicValueInformation
				for(Map.Entry<SMTProgram, Set<DynamicValueInformation>> entry : stmtVisitor.getDynamicValueInfos().entrySet()) {
					for(DynamicValueInformation valueInfo : entry.getValue())
						valueInfo.setSourceOfDataflow(dataflow.get(0));
				}
				
				dynamicValueInfos.putAll(stmtVisitor.getDynamicValueInfos());				
				
			}catch(NotYetSupportedException ex) {
				LoggerHelper.logEvent(MyLevel.EXCEPTION_ANALYSIS, ex.getMessage());
				ex.printStackTrace();
			}catch(Exception ex) {
				LoggerHelper.logEvent(MyLevel.EXCEPTION_ANALYSIS, ex.getMessage());
				ex.printStackTrace();
			}
		}			
	}
	
	 
	private void generateAllSplitAPICombinations(List<List<String>> allElements, List<Integer> currentPos, List<List<String>> result){
		List<String> allValues = new ArrayList<String>();
		for(int i = 0; i < currentPos.size(); i++) {
			int posForListAtI = currentPos.get(i);
			String value = allElements.get(i).get(posForListAtI);
			allValues.add(value);
		 }
		 result.add(allValues);
		 
		 
		 for(int index = currentPos.size()-1; index >= 0; index--) {
			int currentValuePos = currentPos.get(index);
			 
			List<String> valuesAtPos = allElements.get(index);
			int sizeOfValuesAtPos = valuesAtPos.size();
			 
			if(currentValuePos < sizeOfValuesAtPos-1){
				currentPos.set(index, currentValuePos+1);
				for(int i = index+1; i <= currentPos.size()-1; i++)
					currentPos.set(i, 0);
				 generateAllSplitAPICombinations(allElements, currentPos, result);
			}
		}
		return;
	}
	
	
	
	private Map<Integer, List<Integer>> generateTruthTable(int n) {
		Map<Integer, List<Integer>> truthTable = new HashMap<Integer, List<Integer>>();
        int rows = (int) Math.pow(2,n);
        for (int i=0; i<rows; i++) {
            for (int j=n-1; j>=0; j--) {
            	int value = (i/(int) Math.pow(2, j))%2;
            	if(truthTable.keySet().contains(i)) {
            		truthTable.get(i).add(value);
            	}
            	else {
            		List<Integer> newList = new ArrayList<Integer>();
            		newList.add(value);	
            		truthTable.put(i, newList);
            	}
            }
        }
        return truthTable;
    }
	
	public void printProgramToCmdLine() {
		for(SMTProgram smtProgram : smtPrograms)
			System.out.println(smtProgram + "\n");
	}

	public Set<SMTProgram> getSmtPrograms() {
		return smtPrograms;
	}

	public Map<SMTProgram, Set<DynamicValueInformation>> getDynamicValueInfos() {
		return dynamicValueInfos;
	}		
}
