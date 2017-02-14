package de.tu_darmstadt.sse.bootstrap;

import java.util.HashSet;
import java.util.Set;


public class AnalysisTask implements Cloneable {
	
	private Set<DexFile> dexFilesToMerge = new HashSet<>();
	private Set<InstanceIndependentCodePosition> statementsToRemove = new HashSet<>();
	
	
	public AnalysisTask() {
		
	}
	
	
	public AnalysisTask deriveNewTask(DexFile fileToMerge) {
		return deriveNewTask(fileToMerge, null);
	}
	
	
	public AnalysisTask deriveNewTask(DexFile fileToMerge,
			Set<InstanceIndependentCodePosition> toRemove) {
		AnalysisTask newTask = clone();
		newTask.dexFilesToMerge.add(fileToMerge);
		if (toRemove != null)
			newTask.statementsToRemove.addAll(toRemove);
		return newTask;		
	}
	
	@Override
	public AnalysisTask clone() {
		AnalysisTask clone = new AnalysisTask();
		clone.dexFilesToMerge.addAll(dexFilesToMerge);
		return clone;
	}
	
	
	public Set<DexFile> getDexFilesToMerge() {
		return this.dexFilesToMerge;
	}
	
	
	public Set<InstanceIndependentCodePosition> getStatementsToRemove() {
		return this.statementsToRemove;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((dexFilesToMerge == null) ? 0 : dexFilesToMerge.hashCode());
		result = prime * result
				+ ((statementsToRemove == null) ? 0 : statementsToRemove.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AnalysisTask other = (AnalysisTask) obj;
		if (dexFilesToMerge == null) {
			if (other.dexFilesToMerge != null)
				return false;
		} else if (!dexFilesToMerge.equals(other.dexFilesToMerge))
			return false;
		if (statementsToRemove == null) {
			if (other.statementsToRemove != null)
				return false;
		} else if (!statementsToRemove.equals(other.statementsToRemove))
			return false;
		return true;
	}

}
