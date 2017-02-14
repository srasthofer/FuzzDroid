package de.tu_darmstadt.sse.decisionmaker.analysis;

import java.util.ArrayList;
import java.util.List;

import de.tu_darmstadt.sse.frameworkevents.FrameworkEvent;
import de.tu_darmstadt.sse.sharedclasses.networkconnection.ServerResponse;


public class AnalysisDecision implements Comparable<AnalysisDecision>, Cloneable {
	
	private int decisionWeight = 0;	
	
	private ServerResponse serverResponse;
	
	private List<FrameworkEvent> eventTaskList = new ArrayList<FrameworkEvent>();
	
	private String analysisName;
	
	private boolean decisionUsed = false;

	
	public AnalysisDecision() {
		
	}
	
	
	public AnalysisDecision(AnalysisDecision original) {
		this.decisionWeight = original.decisionWeight;
		this.serverResponse = original.serverResponse.clone();
		this.eventTaskList.addAll(original.eventTaskList);
		this.analysisName = original.analysisName;
		
		// We do not copy over the decisionUsed flag. This must always be set
		// explicitly.
	}
	
	public int getDecisionWeight() {
		return decisionWeight;
	}

	public void setDecisionWeight(int decisionWeight) {
		this.decisionWeight = decisionWeight;
	}

	public ServerResponse getServerResponse() {
		return serverResponse;
	}

	public void setServerResponse(ServerResponse serverResponse) {
		this.serverResponse = serverResponse;
	}

	public List<FrameworkEvent> getEventTaskList() {
		return eventTaskList;
	}
	
	public void addNewEvent(FrameworkEvent event) {
		eventTaskList.add(event);
	}		
	
	public String getAnalysisName() {
		return analysisName;
	}

	public void setAnalysisName(String analysisName) {
		this.analysisName = analysisName;
	}
	
	
	public void setDecisionUsed() {
		this.decisionUsed = true;
	}
	
	
	public boolean isDecisionUsed() {
		return this.decisionUsed;
	}

	@Override
	public int compareTo(AnalysisDecision other) {
		return -Integer.compare(this.decisionWeight, other.decisionWeight);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((analysisName == null) ? 0 : analysisName.hashCode());
		result = prime * result + (decisionUsed ? 1231 : 1237);
		result = prime * result + decisionWeight;
		result = prime * result
				+ ((eventTaskList == null) ? 0 : eventTaskList.hashCode());
		result = prime * result
				+ ((serverResponse == null) ? 0 : serverResponse.hashCode());
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
		AnalysisDecision other = (AnalysisDecision) obj;
		if (analysisName == null) {
			if (other.analysisName != null)
				return false;
		} else if (!analysisName.equals(other.analysisName))
			return false;
		if (decisionUsed != other.decisionUsed)
			return false;
		if (decisionWeight != other.decisionWeight)
			return false;
		if (eventTaskList == null) {
			if (other.eventTaskList != null)
				return false;
		} else if (!eventTaskList.equals(other.eventTaskList))
			return false;
		if (serverResponse == null) {
			if (other.serverResponse != null)
				return false;
		} else if (!serverResponse.equals(other.serverResponse))
			return false;
		return true;
	}
	
	@Override
	public AnalysisDecision clone() {
		return new AnalysisDecision(this);
	}
	
	@Override
	public String toString() {
		return "[" + analysisName + "] " + serverResponse;
	}
	
}
