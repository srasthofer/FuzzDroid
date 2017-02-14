package de.tu_darmstadt.sse.decisionmaker.server;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.tu_darmstadt.sse.decisionmaker.analysis.AnalysisDecision;
import de.tu_darmstadt.sse.decisionmaker.server.history.ClientHistory;
import de.tu_darmstadt.sse.decisionmaker.server.history.ClientHistoryCreatedHandler;
import de.tu_darmstadt.sse.sharedclasses.networkconnection.DecisionRequest;


public class ThreadTraceManager {
	
	private final long threadID;
	private final List<ClientHistory> clientHistories = new ArrayList<>();
	private final List<ClientHistory> shadowHistories = new ArrayList<>();
	private final Set<ClientHistoryCreatedHandler> onCreateHandlers = new HashSet<>();
	
	
	public ThreadTraceManager(long threadID) {
		this.threadID = threadID;
	}
	
	
	public boolean ensureHistorySize(int size) {
		if (clientHistories.size() >= size)
			return false;
		
		for (int i = clientHistories.size(); i < size; i++) {
			ClientHistory dummyHistory = new ClientHistory();
			clientHistories.add(dummyHistory);
			for (ClientHistoryCreatedHandler handler : onCreateHandlers)
				handler.onClientHistoryCreated(dummyHistory);
		}
		return true;
	}
	
	
	public void ensureHistorySize(int size, ClientHistory history) {
		while (this.clientHistories.size() < size) {
			this.clientHistories.add(history);
			for (ClientHistoryCreatedHandler handler : onCreateHandlers)
				handler.onClientHistoryCreated(history);
		}
	}
	
	
	public long getThreadID() {
		return this.threadID;
	}
	
	
	public List<ClientHistory> getHistories() {
		return this.clientHistories;
	}
	
	
	public ClientHistory getNewestClientHistory() {
		if (this.clientHistories.isEmpty())
			return null;
		return this.clientHistories.get(this.clientHistories.size() - 1);
	}
	
	
	public ClientHistory getLastClientHistory() {
		if (this.clientHistories.size() < 2)
			return null;
		return this.clientHistories.get(this.clientHistories.size() - 2);
	}
	
	
	public void addShadowHistory(ClientHistory history) {
		// Do not add a shadow history that is a prefix of an existing history or
		// shadow history
		for (ClientHistory existingHistory : clientHistories)
			if (history.isPrefixOf(existingHistory))
				return;
		for (ClientHistory existingHistory : shadowHistories)
			if (history.isPrefixOf(existingHistory))
				return;
		
		// Add the new shadow history
		shadowHistories.add(history);
		
		// Notify our handlers
		for (ClientHistoryCreatedHandler handler : onCreateHandlers)
			handler.onClientHistoryCreated(history);
	}
	
	
	public List<ClientHistory> getShadowHistories() {
		return this.shadowHistories;
	}
	
	
	public void addOnCreateHandler(ClientHistoryCreatedHandler handler) {
		this.onCreateHandlers.add(handler);
	}

	
	public int getHistoryAndShadowCount() {
		return clientHistories.size() + shadowHistories.size();
	}

	
	public boolean isEmpty() {
		return clientHistories.isEmpty();
	}

	
	public AnalysisDecision getBestResponse(DecisionRequest request) {
		// Accumulate the decisions we have so far for the given request
		AnalysisDecision bestDecision = null;
		int bestScore = 0;
		for (ClientHistory history : clientHistories) {
			int score = history.getProgressValue("ApproachLevel");
			if (score > bestScore) {
				AnalysisDecision decision = history.getResponseForRequest(request);
				if (decision != null && decision.getServerResponse().doesResponseExist()) {
					bestDecision = decision;
					bestScore = score;
				}
			}
		}
		return bestDecision;
	}
	
}
