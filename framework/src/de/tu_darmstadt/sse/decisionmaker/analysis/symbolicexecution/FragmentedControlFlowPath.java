package de.tu_darmstadt.sse.decisionmaker.analysis.symbolicexecution;

import java.util.ArrayList;
import java.util.List;


public class FragmentedControlFlowPath {
	List<ControlFlowPath> fragmentedControlFlowPath = new ArrayList<ControlFlowPath>();
	
	public FragmentedControlFlowPath() {
		//do nothing
	}
	
	public FragmentedControlFlowPath(List<ControlFlowPath> fragmentedControlFlowPath) {
		this.fragmentedControlFlowPath = fragmentedControlFlowPath;
	}
	
	public void addNewControlFlowGraphFragment(ControlFlowPath cfp) {
		fragmentedControlFlowPath.add(cfp);
	}
	
	public FragmentedControlFlowPath deepCopy() {
		List<ControlFlowPath> newFragmentedControlFlowPath = new ArrayList<ControlFlowPath>();
		for(ControlFlowPath cfp : fragmentedControlFlowPath)
			newFragmentedControlFlowPath.add(cfp.deepCopy());
		return new FragmentedControlFlowPath(newFragmentedControlFlowPath);
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		for(ControlFlowPath cfp : fragmentedControlFlowPath) {
			sb.append(cfp + "\t\n");
		}
		
		return sb.toString();
	}
}
