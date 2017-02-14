package de.tu_darmstadt.sse.progressmetric;

import soot.Unit;
import de.tu_darmstadt.sse.decisionmaker.server.history.ClientHistory;


public interface IProgressMetric {
	
	
	public int update(ClientHistory history);
	
	
	public String getMetricName();
	
	
	public String getMetricIdentifier();
	
	
	public void initalize();
	
	
	public void setCurrentTargetLocation(Unit currentTargetLocation);

}
