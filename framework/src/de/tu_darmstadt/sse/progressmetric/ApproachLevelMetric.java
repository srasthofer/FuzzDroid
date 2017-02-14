package de.tu_darmstadt.sse.progressmetric;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.infoflow.solver.cfg.InfoflowCFG;

import com.google.common.collect.HashBasedTable;

import de.tu_darmstadt.sse.appinstrumentation.UtilInstrumenter;
import de.tu_darmstadt.sse.decisionmaker.server.history.ClientHistory;
import de.tu_darmstadt.sse.sharedclasses.util.Pair;


public class ApproachLevelMetric implements IProgressMetric {
	
	private HashBasedTable<Unit, Unit, Integer> targetBasedDistancemap = HashBasedTable.create();
	
	
	private Integer bestSoFar = Integer.MAX_VALUE;
	
	
	private Unit currentTargetLocation;

	
	private Collection<Unit> targetUnits;
	
	private InfoflowCFG cfg;
	
	public ApproachLevelMetric(Collection<Unit> targetUnits, InfoflowCFG cfg) {
		this.targetUnits = targetUnits;
		this.cfg = cfg;
	}
	
	private class ApproachLevelItem {
		
		private Unit currentUnit;
		private int approachLevel;
		
		public ApproachLevelItem(Unit currentUnit, int approachLevel) {
			this.currentUnit = currentUnit;
			this.approachLevel = approachLevel;
		}
		
	}
	
	@Override
	public void initalize() {		
		if(currentTargetLocation == null)
			throw new RuntimeException("we have to have a target!");
		//we have to do this for every target unit
		for(Unit singleTarget : targetUnits) {
//			DefaultHashMap<Unit, Integer> distanceMap = new DefaultHashMap<Unit, Integer>(Integer.MAX_VALUE);
			
			//check if this method is reachable; otherwise we can not create an inter-procedurarl CFG
			if (!cfg.isReachable(singleTarget))
				continue;
			
			Set<Unit> reachedUnits = new HashSet<Unit>();			
			List<ApproachLevelItem> worklist = new LinkedList<ApproachLevelItem>();
			
			worklist.add(new ApproachLevelItem(singleTarget, Integer.MAX_VALUE));
						
			while(!worklist.isEmpty()) {
				// get front element
				ApproachLevelItem curItem = worklist.remove(0);
				Unit currentUnit = curItem.currentUnit;
				int currentMetricValue = curItem.approachLevel;
				
				// Do not in circles
				if (!reachedUnits.add(currentUnit)){
					continue;
				}
				
				// If we already have a better approach level from another path, do not
				// continue here
				if (targetBasedDistancemap.contains(singleTarget, currentUnit)) {
					int oldMetricValue = targetBasedDistancemap.get(singleTarget, currentUnit);
					if (oldMetricValue > currentMetricValue)
						continue;
				}
				
				// We decrease the approach level for every statement that we travel farther
				// away from the target location
				currentMetricValue--;
				targetBasedDistancemap.put(singleTarget, currentUnit, currentMetricValue);	
				
				//in case we reached the start of the method (vice verse in backward analysis)
				if (cfg.isStartPoint(currentUnit)) {
					SootMethod sm = cfg.getMethodOf(currentUnit);
					Collection<Unit> callers = cfg.getCallersOf(sm);
					for (Unit caller : callers)
						for (Unit callerPred : cfg.getPredsOf(caller))
							worklist.add(new ApproachLevelItem(callerPred, currentMetricValue));
				}
				if(cfg.isExitStmt(currentUnit)) {
					SootMethod sm = cfg.getMethodOf(currentUnit);
					//first: get all callers
					Collection<Unit> callers = cfg.getCallersOf(sm);
					for(Unit caller : callers) {
						for (Unit retSite : cfg.getReturnSitesOfCallAt(caller)) {
							//second: add distance info to all callers
							targetBasedDistancemap.put(singleTarget, retSite, currentMetricValue);
							//third get the predecessors (aka succs of cfg) of the callers and add them to the worklist
							worklist.add(new ApproachLevelItem(retSite, currentMetricValue));
						}
					}
					//there is no need for further progress
					continue;
				}
				//in case of a non-api call
				else if(cfg.isCallStmt(currentUnit)) {
					for (SootMethod callee : cfg.getCalleesOfCallAt(currentUnit)) {
						SootClass clazzOfInvoke = callee.getDeclaringClass();
						
						if(UtilInstrumenter.isAppDeveloperCode(clazzOfInvoke)) {
							//get all return statements
							Collection<Unit> returnStmts = cfg.getStartPointsOf(callee);
							for(Unit returnStmt : returnStmts) {
								//We have to do this, since SPARK has a well known issue with 
								//iterators for instance where the correct building of a call graph is NOT possible.
								//changing to CHA would solve the issue, but would blow up the call graph
								if(cfg.getMethodOf(returnStmt) != null) {
									worklist.add(new ApproachLevelItem(returnStmt, currentMetricValue));
								}							
							}
							continue;
						}
					}
				}
				
				List<Unit> nextUnits = cfg.getPredsOf(currentUnit);
				for(Unit unit : nextUnits) {
					worklist.add(new ApproachLevelItem(unit, currentMetricValue));
				}
			}			
		}
	}

	private Pair<Unit, Integer> getBestApproachLevel(Collection<Unit> path) {	
		Pair<Unit, Integer> retval = new Pair<Unit, Integer>(null, Integer.MAX_VALUE);
		for (Unit unit: path) {
			Integer distance = targetBasedDistancemap.get(currentTargetLocation, unit);
			//in case we are not able to extract the the distance information, we take the old one
			if(distance == null) {
//				LoggerHelper.logWarning("not able to extract the distance information for: " + unit);
				retval.setFirst(unit);
				retval.setSecond(retval.getSecond());
			}
			else if (distance < retval.getSecond()){
				retval.setFirst(unit);
				retval.setSecond(distance);
			}

		}
		return retval;
	}


	@Override
	public int update(ClientHistory history) {		
		int value = getBestApproachLevel(history.getCodePostions()).getSecond();
		bestSoFar = java.lang.Math.min(bestSoFar, value);
		// Set progress value
		history.setProgressValue(getMetricIdentifier(), value);
		return value;
	}
	
	@Override
	public String getMetricName() {
		return "ApproachLevel";
	}

	@Override
	public String getMetricIdentifier() {
		return "ApproachLevel";
	}
	
	@Override
	public void setCurrentTargetLocation(Unit currentTargetLocation) {		
		this.currentTargetLocation = currentTargetLocation;
	}	
}
