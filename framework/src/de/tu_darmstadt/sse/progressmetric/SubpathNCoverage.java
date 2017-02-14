package de.tu_darmstadt.sse.progressmetric;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import soot.Unit;
import soot.jimple.infoflow.solver.cfg.InfoflowCFG;
import de.tu_darmstadt.sse.decisionmaker.server.history.ClientHistory;
import de.tu_darmstadt.sse.sharedclasses.util.Pair;


public class SubpathNCoverage implements IProgressMetric {
	
	private Set<List<Pair<Unit, Boolean>>> coveredPathFragment = new HashSet<List<Pair<Unit, Boolean>>>();
	
	private int fragmentLength;
	
	private InfoflowCFG cfg;
	
	
	public SubpathNCoverage(Collection<Unit> targetUnits, InfoflowCFG cfg) {
		this(targetUnits, 2);
		this.cfg = cfg;
	}

	
	public SubpathNCoverage(Collection<Unit> targetUnits,
			int pathFragmentLength) {
		fragmentLength = pathFragmentLength;
	}
	
	
	@Override
	public int update(ClientHistory history){
		List<Pair<Unit, Boolean>> trace = history.getPathTrace();
		int retval = 0;

		if(fragmentLength > 0 && trace.size()>fragmentLength){ //coverage of path fragments of length = fragmentLength
			for(int i = 0; i < trace.size()-fragmentLength; i++){
				if (update(trace.subList(i, i+fragmentLength))){
					retval++;
				}
			}
		}
		else if (update(trace)){ //path coverage
			retval++;
		}
		
		history.setProgressValue(getMetricIdentifier(), getNumCovered());
		return getNumCovered();
	}
	
	private boolean update(List<Pair<Unit, Boolean>> l){
		boolean retval = false;
		if(coveredPathFragment.add(new ArrayList<Pair<Unit, Boolean>>(l))){
			retval = true;
		} 
		return retval;
	}
	
	
	public int getNumCovered(){
		return coveredPathFragment.size();
	}
	
	public int getFragmentLength(){
		return fragmentLength;
	}
	
	@Override
	public String getMetricName() {
		return "SubpathNCoverage";
	}

	@Override
	public String getMetricIdentifier() {
		return "SubpathNCoverage";
	}

	@Override
	public void initalize() {
		// TODO Auto-generated method stub		
	}

	@Override
	public void setCurrentTargetLocation(Unit currentTargetLocation) {
		// TODO Auto-generated method stub
		
	}
}
