package de.tu_darmstadt.sse.decisionmaker.analysis.symbolicexecution;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import soot.SootMethod;
import soot.Unit;
import soot.jimple.AddExpr;
import soot.jimple.AndExpr;
import soot.jimple.AssignStmt;
import soot.jimple.BinopExpr;
import soot.jimple.CmpExpr;
import soot.jimple.CmpgExpr;
import soot.jimple.CmplExpr;
import soot.jimple.ConditionExpr;
import soot.jimple.DivExpr;
import soot.jimple.EqExpr;
import soot.jimple.GeExpr;
import soot.jimple.GtExpr;
import soot.jimple.IfStmt;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.LeExpr;
import soot.jimple.LtExpr;
import soot.jimple.MulExpr;
import soot.jimple.NeExpr;
import soot.jimple.OrExpr;
import soot.jimple.RemExpr;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.ShlExpr;
import soot.jimple.ShrExpr;
import soot.jimple.Stmt;
import soot.jimple.SubExpr;
import soot.jimple.UshrExpr;
import soot.jimple.XorExpr;
import soot.jimple.infoflow.results.ResultSourceInfo;
import soot.jimple.infoflow.solver.cfg.IInfoflowCFG;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.MHGPostDominatorsFinder;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.SimpleLocalDefs;
import soot.toolkits.scalar.SimpleLocalUses;
import soot.toolkits.scalar.UnitValueBoxPair;
import de.tu_darmstadt.sse.appinstrumentation.UtilInstrumenter;
import de.tu_darmstadt.sse.appinstrumentation.transformer.InstrumentedCodeTag;
import de.tu_darmstadt.sse.commandlinelogger.LoggerHelper;


public class UtilSMT {
	
	public static Set<Boolean> extractConditionForReachingAUnit(IInfoflowCFG cfg, Stmt conditionStmt, Set<Unit> targetUnits) {
		boolean firstConditionVisited = false;
		
		int currentBranchTaken = -1;
		BinopExpr branchCondition = null;
		Unit elseBranchStmt = null;
		Set<Unit> reachedUnits = new HashSet<Unit>();
		Stack<Unit> worklist = new Stack<Unit>();
		worklist.add(conditionStmt);
//		worklist.addAll(cfg.getSuccsOf(conditionStmt));
		Set<Boolean> conditionsToReturn = new HashSet<Boolean>();
		
		while(!worklist.isEmpty()) {
			Unit currentUnit = worklist.pop();	
			
			//we always take the then branch first, therefore, there else branch comes last
			if(currentUnit == elseBranchStmt)
				currentBranchTaken = 0;
			
			if(targetUnits.contains(currentUnit)) {
				//figure out what branch is taken first
				if(branchCondition == null)
					throw new RuntimeException("there has to be a condition");
				else{	

					if(branchCondition.getOp2() == null) {
						LoggerHelper.logWarning("There is a \"null\" check in a condition; we do not consider this");
						if(currentBranchTaken == 0)
							conditionsToReturn.add(true);
						else if(currentBranchTaken == 1)
							conditionsToReturn.add(false);
						else
							throw new RuntimeException("should not happen...");
					}
					else {
						IntConstant intConst = (IntConstant)branchCondition.getOp2();
						
						
						int intConstValue = -1;
						if(branchCondition instanceof NeExpr) {
							if(intConst.value == 0)
								intConstValue = 1;
							else if(intConst.value == 1)
								intConstValue = 0;
						}
						else if(branchCondition instanceof EqExpr) {
							intConstValue = intConst.value;
						}
						else if(branchCondition instanceof GeExpr) {
							throw new RuntimeException("todo");
						}
						else if(branchCondition instanceof GtExpr) {
							throw new RuntimeException("todo");
						}
						else if(branchCondition instanceof LeExpr) {
							throw new RuntimeException("todo");
						}
						else if(branchCondition instanceof LtExpr) {
							throw new RuntimeException("todo");
						}
						else if(branchCondition instanceof NeExpr) {
							throw new RuntimeException("todo");
						}		
						else if(branchCondition instanceof AddExpr) {
							throw new RuntimeException("todo");
						}
						else if(branchCondition instanceof AndExpr) {
							throw new RuntimeException("todo");
						}
						else if(branchCondition instanceof CmpExpr) {
							throw new RuntimeException("todo");
						}
						else if(branchCondition instanceof CmpgExpr) {
							throw new RuntimeException("todo");
						}
						else if(branchCondition instanceof CmplExpr) {
							throw new RuntimeException("todo");
						}
						else if(branchCondition instanceof ConditionExpr) {
							throw new RuntimeException("todo");
						}
						else if(branchCondition instanceof DivExpr) {
							throw new RuntimeException("todo");
						}
						else if(branchCondition instanceof MulExpr) {
							throw new RuntimeException("todo");
						}
						else if(branchCondition instanceof OrExpr) {
							throw new RuntimeException("todo");
						}
						else if(branchCondition instanceof RemExpr) {
							throw new RuntimeException("todo");
						}
						else if(branchCondition instanceof ShlExpr) {
							throw new RuntimeException("todo");
						}
						else if(branchCondition instanceof ShrExpr) {
							throw new RuntimeException("todo");
						}
						else if(branchCondition instanceof SubExpr) {
							throw new RuntimeException("todo");
						}
						else if(branchCondition instanceof UshrExpr) {
							throw new RuntimeException("todo");
						}
						else if(branchCondition instanceof XorExpr) {
							throw new RuntimeException("todo");
						}
						
						
						
						//this branch taken
						if(intConstValue == 0) {
							//which branch is actually taken by the CFG depth search?
							if(currentBranchTaken == 0)
								conditionsToReturn.add(true);
							else if(currentBranchTaken == 1)
								conditionsToReturn.add(false);
							else
								throw new RuntimeException("should not happen...");
						}
						//else branch taken
						else{
							//which branch is actually taken by the CFG depth search?
							if(currentBranchTaken == 0)
								conditionsToReturn.add(false);
							else if(currentBranchTaken == 1)
								conditionsToReturn.add(true);
							else
								throw new RuntimeException("should not happen...");
						}
					}
				}
				
				//in case we already discovered that the this and the else branch leads 
				// to the logging point, we can stop our analysis
				if(conditionsToReturn.size() == 2) {
					worklist.clear();
					//reset reached units
					reachedUnits.clear();
					
					continue;
				}
				else					
					continue;
			}
			
			//statement already passed?
			if(reachedUnits.contains(currentUnit))
				continue;
			else {
				reachedUnits.add(currentUnit);
//				System.out.println(String.format("(%d) Reached: %s", reachedUnits.size(), currentUnit));
			}
			
			SootMethod currentMethod = cfg.getMethodOf(currentUnit);
			//there is no need to look into the dummy main method
			if(currentMethod.getDeclaringClass().toString().equals("dummyMainClass"))
				continue;						
			
			if(cfg.isCallStmt(currentUnit)) {
				InvokeExpr invokeExpr = null;
				if(currentUnit instanceof AssignStmt) {
					AssignStmt assign = (AssignStmt) currentUnit;
					invokeExpr = assign.getInvokeExpr();
				}
				else if(currentUnit instanceof InvokeStmt){
					InvokeStmt tmp = (InvokeStmt) currentUnit;
					invokeExpr = tmp.getInvokeExpr();
				}
				
				//special handling for non-api calls
				SootMethod sm = invokeExpr.getMethod();
				if(UtilInstrumenter.isAppDeveloperCode(sm.getDeclaringClass())) {
					Collection<SootMethod> callees = cfg.getCalleesOfCallAt(currentUnit);
					for(SootMethod callee : callees) {
						Collection<Unit> startPoints = cfg.getStartPointsOf(callee);
						for(Unit unit : startPoints) {
							//since we are context-sensitive, it does not make sense to check for already reached methods here
//							if(!reachedUnits.contains(unit))
								worklist.push(unit);
						}
					}
				}
				//find successor for api calls
				else{
					List<Unit> successor = cfg.getSuccsOf(currentUnit);
					for(Unit unit : successor) {
//						if(!reachedUnits.contains(unit))
							worklist.push(unit);
					}
				}
					
			}
			else if(currentUnit instanceof ReturnStmt
					|| currentUnit instanceof ReturnVoidStmt) {
				SootMethod sm = cfg.getMethodOf(currentUnit);
				for(Unit caller : cfg.getCallersOf(sm)) {
					for(Unit unit : cfg.getSuccsOf(caller)) {
//						if(!reachedUnits.contains(unit))
							worklist.push(unit);
					}
				}
				continue;
			}
			else if(currentUnit instanceof IfStmt) {	
				IfStmt ifStmt = (IfStmt)currentUnit;
				List<Unit> bothBranches = cfg.getSuccsOf(currentUnit);
				Unit branchOne = bothBranches.get(0);
				Unit branchTwo = bothBranches.get(1);
				
				if(!firstConditionVisited) {
					firstConditionVisited = true;
					assert ifStmt.getCondition() instanceof BinopExpr;
					branchCondition = (BinopExpr)ifStmt.getCondition();
									
					//we always take the then-branch first
					currentBranchTaken = 1;
					//therefore, we push the else branch first to the worklist
					if(cfg.isFallThroughSuccessor(currentUnit, branchOne)) {
						if(!reachedUnits.contains(branchOne))
							worklist.push(branchOne);
						if(!reachedUnits.contains(branchTwo))
							worklist.push(branchTwo);
						elseBranchStmt = branchOne;
					}else {
						if(!reachedUnits.contains(branchTwo))
							worklist.push(branchTwo);
						if(!reachedUnits.contains(branchOne))
							worklist.push(branchOne);
						elseBranchStmt = branchTwo;
					}
				
					continue;					
				}
				else {
					if(!reachedUnits.contains(branchOne))
						worklist.add(branchOne);
					if(!reachedUnits.contains(branchTwo))
						worklist.add(branchTwo);
					continue;
				}

			}
			//just take the successors
			else{			
				List<Unit> successor = cfg.getSuccsOf(currentUnit);
				for(Unit unit : successor) {
//					if(!reachedUnits.contains(unit))
						worklist.push(unit);
				}
			}
		}
		
		return conditionsToReturn;
	}
	

	
	public static Set<ResultSourceInfo> removeDuplicatedFlows(Set<ResultSourceInfo> allDataFlows) {
		Set<ResultSourceInfo> copy = new HashSet<ResultSourceInfo>(allDataFlows);
		
		for(ResultSourceInfo dataFlow1 : allDataFlows) {
			Stmt[] dataFlowPath1 = dataFlow1.getPath();
			for(ResultSourceInfo dataFlow2 : allDataFlows) {
				Stmt[] dataFlowPath2 = dataFlow2.getPath();
				if(dataFlowPath1 != dataFlowPath2 &&
						Arrays.asList(dataFlowPath2).containsAll(Arrays.asList(dataFlowPath1)))
					copy.remove(dataFlow1);
			}
		}
		
		return copy;
	}
	
	
	
	public static Set<FragmentedControlFlowPath> getControlFlowPaths(Stmt[] dataFlowPath, IInfoflowCFG cfg) {
		Set<FragmentedControlFlowPath> fragmentedControlFlowPaths = new HashSet<FragmentedControlFlowPath>();
		
		for(int i = 0; i < dataFlowPath.length-1; i++) {
			Set<ControlFlowPath> subControlFlowPaths = getControlFlowPathsBetweenTwoDataFlowStmts(dataFlowPath[i], dataFlowPath[i+1], cfg);
			
			if(fragmentedControlFlowPaths.isEmpty()) {
				for(ControlFlowPath cfp : subControlFlowPaths) {
					FragmentedControlFlowPath fcfp = new FragmentedControlFlowPath();
					fcfp.addNewControlFlowGraphFragment(cfp.deepCopy());
					fragmentedControlFlowPaths.add(fcfp);
				}
			}
			else{			
				//we have to add more sub-paths to the current controlFlowPaths
				if(subControlFlowPaths.size() > 1) {		
					for(FragmentedControlFlowPath fcfp : fragmentedControlFlowPaths) {											
						for(ControlFlowPath subControlFlowPath : subControlFlowPaths) {					
							FragmentedControlFlowPath clonedFcfp = fcfp.deepCopy();
							clonedFcfp.addNewControlFlowGraphFragment(subControlFlowPath);
							fragmentedControlFlowPaths.add(clonedFcfp);
						}
						
						//we already added it, so we can remove this one
						fragmentedControlFlowPaths.remove(fcfp);
					}
				}
				//there is only one path to add
				else{
					ControlFlowPath subControlFlowPath = subControlFlowPaths.iterator().next();
					//remove first element of path since it is already included in the controlFlowPaths
//					subControlFlowPath.getPath().remove(0);
					
					for(FragmentedControlFlowPath fcfp : fragmentedControlFlowPaths)
						fcfp.addNewControlFlowGraphFragment(subControlFlowPath);
				}
			}
		}
		
		return fragmentedControlFlowPaths;
	}

	
	public static Set<ControlFlowPath> getControlFlowPathsBetweenTwoDataFlowStmts(Stmt from, Stmt to, IInfoflowCFG cfg) {
		//is necessary at conditions where two branches are taken. The control-flow path until the condition is stored
		//and re-used for the then- and else-branch
		Map<Unit, Set<ControlFlowPath>> controlFlowPathsAtUnit = new HashMap<Unit, Set<ControlFlowPath>>();
		Set<ControlFlowPath> controlFlowPaths = new HashSet<ControlFlowPath>();
		Set<ControlFlowPath> currentPaths = new HashSet<ControlFlowPath>();
		Stack<Unit> worklist = new Stack<Unit>();
		worklist.add(from);				
		
		while(!worklist.isEmpty()) {
			Unit currentUnit = worklist.pop();			
			currentPaths = getCurrentControlFlowGraphsForUnit(currentUnit, controlFlowPathsAtUnit, currentPaths);
			
			//we only add cfg-statements of the original code
			//our own instrumented statements will not be part of the path
			if(!currentUnit.hasTag(InstrumentedCodeTag.name))
				addCurrentStmtToPaths(currentPaths, currentUnit);
			
			//we reached the to-statement
			if(currentUnit == to) {
				return currentPaths;
			}
			
			//we can stop the path traversal in case we reached the dummyMain class
			SootMethod currentMethod = cfg.getMethodOf(currentUnit);
			//there is no need to look into the dummy main method
			if(currentMethod.getDeclaringClass().toString().equals("dummyMainClass"))
				continue;
			
			if(cfg.isCallStmt(currentUnit)) {
				InvokeExpr invokeExpr = null;
				if(currentUnit instanceof AssignStmt) {
					AssignStmt assign = (AssignStmt) currentUnit;
					invokeExpr = assign.getInvokeExpr();
				}
				else if(currentUnit instanceof InvokeStmt){
					InvokeStmt tmp = (InvokeStmt) currentUnit;
					invokeExpr = tmp.getInvokeExpr();
				}
				
				//special handling for non-api calls
				SootMethod sm = invokeExpr.getMethod();
				if(UtilInstrumenter.isAppDeveloperCode(sm.getDeclaringClass())) {
					Collection<SootMethod> callees = cfg.getCalleesOfCallAt(currentUnit);
					for(SootMethod callee : callees) {
						Collection<Unit> startPoints = cfg.getStartPointsOf(callee);
						for(Unit nextUnit : startPoints) {
							if(proceedWithNextUnit(currentUnit, nextUnit, currentPaths, controlFlowPathsAtUnit)) {
								worklist.push(nextUnit);
							}
						}
					}
				}
				//find successor for api calls
				else{
					List<Unit> successor = cfg.getSuccsOf(currentUnit);
					for(Unit nextUnit : successor) {
						if(proceedWithNextUnit(currentUnit, nextUnit, currentPaths, controlFlowPathsAtUnit)) {
							worklist.push(nextUnit);
						}
					}
				}					
			}
			else if(currentUnit instanceof ReturnStmt
					|| currentUnit instanceof ReturnVoidStmt) {
				SootMethod sm = cfg.getMethodOf(currentUnit);
				for(Unit caller : cfg.getCallersOf(sm)) {
					for(Unit nextUnit : cfg.getSuccsOf(caller)) {
						if(proceedWithNextUnit(currentUnit, nextUnit, currentPaths, controlFlowPathsAtUnit)) {
							worklist.push(nextUnit);
						}
					}
				}
				continue;
			}
			else if(currentUnit instanceof IfStmt) {	
				List<Unit> bothBranches = cfg.getSuccsOf(currentUnit);
				Unit branchOne = bothBranches.get(0);
				Unit branchTwo = bothBranches.get(1);
				
				//special treatment of successors for conditions
				//we have to save the current-path(s) for both successors; otherwise this information will be lost
				//in our worklist-iteration-process
				worklist.push(branchOne);
				saveControlFlowGraphForUnit(branchOne, currentPaths, controlFlowPathsAtUnit);
				worklist.push(branchTwo);
				saveControlFlowGraphForUnit(branchTwo, currentPaths, controlFlowPathsAtUnit);
				
				continue;
			}
			//just take the successors
			else{			
				boolean saveControlFlowPath = false;
				List<Unit> successors = cfg.getSuccsOf(currentUnit);
				//in case there are more than one successors, we have to save the control flow path 
				//at the successor statements since the CFG splits (similar to IfStmt)
				if(successors.size() > 1)
					saveControlFlowPath = true;
				for(Unit nextUnit : successors) {
					if(proceedWithNextUnit(currentUnit, nextUnit, currentPaths, controlFlowPathsAtUnit)) {
						worklist.push(nextUnit);
						if(saveControlFlowPath)
							saveControlFlowGraphForUnit(nextUnit, currentPaths, controlFlowPathsAtUnit);
					}
				}
			}
		}
				
		return controlFlowPaths;
	}
	
	
	public static void saveControlFlowGraphForUnit(Unit unitToProceedNext, final Set<ControlFlowPath> currentControlFlowPaths, Map<Unit, Set<ControlFlowPath>> controlFlowPathsAtUnit) {
		if(controlFlowPathsAtUnit.containsKey(unitToProceedNext)) {
			for(ControlFlowPath cfp : currentControlFlowPaths){
				if(controlFlowPathsAtUnit.get(unitToProceedNext) == currentControlFlowPaths)
					;//do nothing					
				else
					controlFlowPathsAtUnit.get(unitToProceedNext).add(cfp.deepCopy());
			}
		}
		else {
			Set<ControlFlowPath> newControlFlowPathSet = new HashSet<ControlFlowPath>();
			for(ControlFlowPath cfp : currentControlFlowPaths)
				newControlFlowPathSet.add(cfp.deepCopy());
			controlFlowPathsAtUnit.put(unitToProceedNext, newControlFlowPathSet);
		}
	}
	
	
	
	
	public static Set<ControlFlowPath> getCurrentControlFlowGraphsForUnit(Unit currentUnit, Map<Unit, Set<ControlFlowPath>> controlFlowPathsAtUnit, Set<ControlFlowPath> currentPaths) {
		if(controlFlowPathsAtUnit.containsKey(currentUnit))
			return controlFlowPathsAtUnit.get(currentUnit);
		else
			return currentPaths;
	}
	
	
	public static void addCurrentStmtToPaths(Set<ControlFlowPath> currentPaths, Unit currentUnit) {
		if(currentPaths.isEmpty())
			currentPaths.add(new ControlFlowPath());
		for(ControlFlowPath cfp : currentPaths)
			cfp.addStmt(currentUnit);
	}
	
	
	private static boolean proceedWithNextUnit(Unit currentUnit, Unit nextUnit, Set<ControlFlowPath> currentControlFlowPaths, Map<Unit, Set<ControlFlowPath>> controlFlowPathsAtUnit) {
		if(controlFlowPathsAtUnit.containsKey(currentUnit)) {
			Set<ControlFlowPath> allControlFlowPathsOfUnit = controlFlowPathsAtUnit.get(currentUnit);
			for(ControlFlowPath singlePath : allControlFlowPathsOfUnit) {
				if(singlePath.containsUnit(nextUnit))
					return false;
			}
				
			return true;
		}
		else {
			for(ControlFlowPath cfp : currentControlFlowPaths)
				if(cfp.containsUnit(nextUnit))
					return false;
			return true;
		}		
	}
	
	
	public static Unit getPostDominatorOfUnit(IInfoflowCFG cfg, Unit dataFlowStatement) {		
		Map<Unit, Set<ControlFlowPath>> controlFlowPathsAtUnit = new HashMap<Unit, Set<ControlFlowPath>>();
		Set<ControlFlowPath> currentPaths = new HashSet<ControlFlowPath>();
		Stack<Unit> worklist = new Stack<Unit>();
		worklist.add(dataFlowStatement);	

		while(!worklist.isEmpty()) {
			Unit currentUnit = worklist.pop();
			
			if(currentUnit.hasTag(InstrumentedCodeTag.name)) {
				List<Unit> successors = cfg.getSuccsOf(currentUnit);
				
				for(Unit nextUnit : successors) {
					if(proceedWithNextUnit(currentUnit, nextUnit, currentPaths, controlFlowPathsAtUnit)) {
						worklist.push(nextUnit);				
					}
				}
				continue;
			}
			
			SootMethod currentMethod = cfg.getMethodOf(currentUnit);
			
			//this is a kind of hack: We excluded exception-edges here and also keep in mind that ALL dominator-algorithms are intra-procedural
			MHGPostDominatorsFinder<Unit> postdominatorFinder = new MHGPostDominatorsFinder<Unit>(new BriefUnitGraph(currentMethod.retrieveActiveBody()));
			Unit immediatePostDominator = postdominatorFinder.getImmediateDominator(currentUnit);
			while(immediatePostDominator.hasTag(InstrumentedCodeTag.name)) {
				immediatePostDominator = postdominatorFinder.getImmediateDominator(immediatePostDominator);
			}
			return immediatePostDominator;
		}	
		return null;
	}
	
	
	public static boolean needToAddConditionalUnit(IInfoflowCFG cfg, Unit to, Unit booleanUnit) {		
		//1st: We have to check if there is a condition for the boolean-unit
		IfStmt conditionOfBooleanUnit = findConditionalStatementForBooleanUnit(cfg, booleanUnit);
		if(conditionOfBooleanUnit == null)
			return false;
		else {
			
		}
		
		//2nd: Get the post-dominator of the IfStmt from step 1.
		Unit postDominator = cfg.getPostdominatorOf(conditionOfBooleanUnit).getUnit();
		
		//since we know that the booleanUnit (param) occurs "before" the to unit (param), we can
		//assume that the conditionOfBooleanUnit directly affects the to unit in case of a null of the
		//post-dominator
		if(postDominator == null) {
			return true;
		}
		
		//3rd: Starting from the conditionOfBooleanUnit, we take the then and else branch and check 
		//whether the "to"-unit is part of the path starting from the conditionOfBooleanUnit till the postDominator.
		//if this is the case, we know that we have to consider the booleanUnit (param) into the contraint,
		//if not, we do not consider it in the contraint.
		
		Stack<Unit> worklist = new Stack<Unit>();
		Set<Unit> processedUnits = new HashSet<Unit>();
		worklist.add(conditionOfBooleanUnit);				
		
		while(!worklist.isEmpty()) {
			Unit currentUnit = worklist.pop();
			//in case of a loop or recursion
			if(processedUnits.contains(currentUnit))
				continue;
			
			processedUnits.add(currentUnit);
			
			//in case we hit the booleanUnit (param), we know that the condition influences the to unit (param)
			if(currentUnit == booleanUnit)
				return true;
			
			//we reached the post-dominator, we have to continue until the second branch is completeley traversed
			if(currentUnit == postDominator) {
				continue;
			}						
			
			//we can stop the path traversal in case we reached the dummyMain class
			SootMethod currentMethod = cfg.getMethodOf(currentUnit);
			//there is no need to look into the dummy main method
			if(currentMethod.getDeclaringClass().toString().equals("dummyMainClass"))
				continue;
			
			if(cfg.isCallStmt(currentUnit)) {
				InvokeExpr invokeExpr = null;
				if(currentUnit instanceof AssignStmt) {
					AssignStmt assign = (AssignStmt) currentUnit;
					invokeExpr = assign.getInvokeExpr();
				}
				else if(currentUnit instanceof InvokeStmt){
					InvokeStmt tmp = (InvokeStmt) currentUnit;
					invokeExpr = tmp.getInvokeExpr();
				}
				
				//special handling for non-api calls
				SootMethod sm = invokeExpr.getMethod();
				if(UtilInstrumenter.isAppDeveloperCode(sm.getDeclaringClass())) {
					Collection<SootMethod> callees = cfg.getCalleesOfCallAt(currentUnit);
					for(SootMethod callee : callees) {
						Collection<Unit> startPoints = cfg.getStartPointsOf(callee);
						worklist.addAll(startPoints);
					}
				}
				//find successor for api calls
				else{
					List<Unit> successors = cfg.getSuccsOf(currentUnit);					
					worklist.addAll(successors);
				}					
			}
			else if(currentUnit instanceof ReturnStmt
					|| currentUnit instanceof ReturnVoidStmt) {
				SootMethod sm = cfg.getMethodOf(currentUnit);
				for(Unit caller : cfg.getCallersOf(sm)) {
					List<Unit> successors = cfg.getSuccsOf(caller);
					worklist.addAll(successors);
				}
				continue;
			}			
			//just take the successors
			else{							
				List<Unit> successors = cfg.getSuccsOf(currentUnit);
				worklist.addAll(successors);				
			}
		}
		return false;
	}
	
	
	private static IfStmt findConditionalStatementForBooleanUnit(IInfoflowCFG cfg, Unit booleanUnit) {
		Stack<Unit> worklist = new Stack<Unit>();
		Set<Unit> processedUnits = new HashSet<Unit>();
		worklist.add(booleanUnit);	
			
		while(!worklist.isEmpty()) {
			Unit currentUnit = worklist.pop();
			//in case of a loop or recursion
			if(processedUnits.contains(currentUnit))
				continue;
			processedUnits.add(currentUnit);
			
			//skip our own instrumented code
			if(currentUnit.hasTag(InstrumentedCodeTag.name))
				continue;
			
			
			//we reached the condition
			if(currentUnit instanceof IfStmt) {
				return (IfStmt)currentUnit;		 	
			}
			
			SootMethod methodOfBooleanUnit = cfg.getMethodOf(booleanUnit);		
			DirectedGraph<Unit> graph = cfg.getOrCreateUnitGraph(methodOfBooleanUnit);
			//Comment: Steven said it should always be a UnitGraph + he will implement a more convenient way in the near future :-)
			UnitGraph unitGraph = (UnitGraph)graph;

			SimpleLocalDefs defs = new SimpleLocalDefs(unitGraph);
	        SimpleLocalUses uses = new SimpleLocalUses(unitGraph, defs);	        
	        List<UnitValueBoxPair> usesOfCurrentUnit = uses.getUsesOf(booleanUnit);
	        for(UnitValueBoxPair valueBoxPair : usesOfCurrentUnit)
	        	worklist.add(valueBoxPair.getUnit());
			
		}
		return null;
	}
}
