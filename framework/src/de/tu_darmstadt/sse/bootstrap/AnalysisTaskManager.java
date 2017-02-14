package de.tu_darmstadt.sse.bootstrap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class AnalysisTaskManager {
	
	private List<AnalysisTask> openTasks = new ArrayList<>();
	private Set<AnalysisTask> finishedTasks = new HashSet<>();
	private AnalysisTask currentTask = null;
	
	public AnalysisTaskManager() {
		
	}
	
	
	public AnalysisTask scheduleNextTask() {
		if (openTasks.isEmpty())
			currentTask = null;
		else
			currentTask = openTasks.remove(0);
		return currentTask;
	}
	
	
	public AnalysisTask getCurrentTask() {
		return currentTask;
	}
	
	public boolean enqueueAnalysisTask(AnalysisTask task) {
		if (finishedTasks.contains(task))
			return false;
		
		openTasks.add(task);
		finishedTasks.add(task);
		return true;
	}

}
