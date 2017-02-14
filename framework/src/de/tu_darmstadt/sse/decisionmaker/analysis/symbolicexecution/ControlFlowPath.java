package de.tu_darmstadt.sse.decisionmaker.analysis.symbolicexecution;

import java.util.ArrayList;
import java.util.List;

import soot.Unit;


public class ControlFlowPath {
	//control flow path
	List<Unit> path = new ArrayList<Unit>();
	
	
	public void appendPath(ControlFlowPath pathToAppend) {
		this.path.addAll(pathToAppend.getPath());
	}
	
	
	public ControlFlowPath deepCopy() {
		ControlFlowPath copy = new ControlFlowPath();
		for(Unit oldUnit : path)
			copy.path.add(oldUnit);
		return copy;
	}
	
	
	public boolean containsUnit(Unit unit) {
		return path.contains(unit);
	}
	
	
	public void addStmt(Unit unit) {
		this.path.add(unit);
	}

	public List<Unit> getPath() {
		return path;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(Unit unit : path)
			sb.append(unit.toString() + "\n");
		return sb.toString();
	}
}
