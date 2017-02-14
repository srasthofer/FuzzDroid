package de.tu_darmstadt.sse.decisionmaker.server.history;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import de.tu_darmstadt.sse.FrameworkOptions;
import de.tu_darmstadt.sse.FrameworkOptions.TraceConstructionMode;
import de.tu_darmstadt.sse.commandlinelogger.LoggerHelper;
import de.tu_darmstadt.sse.decisionmaker.DeterministicRandom;
import de.tu_darmstadt.sse.decisionmaker.analysis.AnalysisDecision;
import de.tu_darmstadt.sse.sharedclasses.networkconnection.DecisionRequest;
import de.tu_darmstadt.sse.sharedclasses.util.Pair;


public class GeneticCombination {
	
	
	private static final float MUTATION_PROBABILITY = 0.1f;
	
	private static final int MULTIPLIER = 10000;
	
	private static final float LESSER_COMBINATION_PROBABILITY = 0.1f;
	
	private static final float CRASH_PICK_PROBABILITY = 0.01f;
	
	
	private ClientHistory combine(ClientHistory history1, ClientHistory history2,
			Set<ClientHistory> mutationHistories) {
		Set<DecisionRequest> requests = new HashSet<>();
		for (Pair<DecisionRequest, AnalysisDecision> reqResp : history1.getDecisionAndResponse())
			requests.add(reqResp.getFirst());
		for (Pair<DecisionRequest, AnalysisDecision> reqResp : history2.getDecisionAndResponse())
			requests.add(reqResp.getFirst());
		
		// Create the combined history
		ClientHistory newHistory = new ClientHistory();
		
		for (DecisionRequest request : requests) {
			// For each request, randomly get a value from one of the two histories
			AnalysisDecision response = null;
			boolean mutate = DeterministicRandom.theRandom.nextInt(MULTIPLIER)
					< MULTIPLIER * MUTATION_PROBABILITY;
			if (mutate) {
				Set<ClientHistory> remaining = new HashSet<>(mutationHistories);
				remaining.remove(history1);
				remaining.remove(history2);
				response = pickRandomResponse(request, remaining);				
			}
			else {
				if (DeterministicRandom.theRandom.nextBoolean()) {
					response = history1.getResponseForRequest(request);
					if (response == null || !response.getServerResponse().doesResponseExist())
						response = history2.getResponseForRequest(request);
				}
				else  {
					response = history2.getResponseForRequest(request);
					if (response == null || !response.getServerResponse().doesResponseExist())
						response = history1.getResponseForRequest(request);
				}
			}
			
			// If we have a response, we record it in the new history
			if (response != null && response.getServerResponse().doesResponseExist())
				newHistory.addDecisionRequestAndResponse(request, response);
		}
		
		return newHistory;
	}
	
	
	private AnalysisDecision pickRandomResponse(DecisionRequest request,
			Set<ClientHistory> histories) {
		while (!histories.isEmpty()) {
			// Randomly pick a new history
			int index = DeterministicRandom.theRandom.nextInt(histories.size());
			ClientHistory history = null;
			{
			int curIdx = 0;
			for (ClientHistory hist : histories) {
				if (curIdx == index) {
					history = hist;
					break;
				}
				curIdx++;
			}
			}
			histories.remove(history);
			
			// Does this history define a response for the given request?
			AnalysisDecision response = history.getResponseForRequest(request);
			if (response != null)
				return response;
		}
		
		// No value found
		return null;
	}
	
	
	public ClientHistory combineGenetically(Collection<ClientHistory> histories) {
		// Clean up the set of histories
		Set<ClientHistory> hists = new HashSet<>(histories);
		boolean pickCrashed = DeterministicRandom.theRandom.nextInt(MULTIPLIER)
				< CRASH_PICK_PROBABILITY * MULTIPLIER;
		for (Iterator<ClientHistory> histIt = histories.iterator(); histIt.hasNext(); ) {
			ClientHistory curHist = histIt.next();
			
			// Do we allow picking crashed traces?
			if (!pickCrashed && curHist.getCrashException() != null)
				histIt.remove();
			// Remove empty traces
			else if (curHist.hasOnlyEmptyDecisions())
				histIt.remove();
		}
		
		// If we only have one history, there is nothing to combine
		LoggerHelper.logInfo("Genetically combining " + hists.size() + " histories...");
		if (hists.size() < 2)
			return null;
		
		while (!hists.isEmpty()) {
			// Do we do random pick?
			ClientHistory bestHistory = null;
			ClientHistory secondBestHistory = null;
			if (FrameworkOptions.traceConstructionMode == TraceConstructionMode.RandomCombine
					|| DeterministicRandom.theRandom.nextInt(MULTIPLIER)
						< LESSER_COMBINATION_PROBABILITY * MULTIPLIER) {
				List<ClientHistory> histList = new ArrayList<>(hists);
				bestHistory = histList.get(DeterministicRandom.theRandom.nextInt(
						histList.size()));
			}
			else {
				// Get the best histories
				int bestValue = 0;
				for (ClientHistory curHistory : hists) {
					int curValue = curHistory.getProgressValue("ApproachLevel");
					if (curValue > bestValue) {
						bestValue = curValue;
					}
				}
				List<ClientHistory> tempList = new ArrayList<>(hists.size());
				getHistoriesByApproachLevel(tempList, hists, bestValue);
				
				// If we have shadow traces, we only pick from those with the highest
				// confidence in the last (not yet tried) decision
				List<ClientHistory> bestHistories = new ArrayList<>(hists.size());
				filterBestShadowHistories(bestHistories, tempList);
				
				// Randomly pick one of the best histories
				bestHistory = bestHistories.get(DeterministicRandom
						.theRandom.nextInt(bestHistories.size()));
			
				// If we have a second history of the same quality, we take that
				if (bestHistories.size() > 1) {
					while (secondBestHistory == null || secondBestHistory == bestHistory)
						secondBestHistory = bestHistories.get(DeterministicRandom
								.theRandom.nextInt(bestHistories.size()));
				}
			}
			
			// Once again, allow for random pick
			if (FrameworkOptions.traceConstructionMode == TraceConstructionMode.RandomCombine
					|| DeterministicRandom.theRandom.nextInt(MULTIPLIER)
						< LESSER_COMBINATION_PROBABILITY * MULTIPLIER) {
				List<ClientHistory> histList = new ArrayList<>(hists);
				while (secondBestHistory == null || secondBestHistory == bestHistory) {
					secondBestHistory = histList.remove(DeterministicRandom.theRandom.nextInt(
							histList.size()));
				}
			}
			else {
				// If we don't have a history of the same quality, we need to get a
				// lesser one
				if (secondBestHistory == null) {
					int secondBestValue = 0;
					for (ClientHistory curHistory : hists) {
						int curValue = curHistory.getProgressValue("ApproachLevel");
						if (curHistory != bestHistory && curValue > secondBestValue) {
							secondBestValue = curValue;
						}
					}
					
					List<ClientHistory> tempList = new ArrayList<>(hists.size());
					getHistoriesByApproachLevel(tempList, hists, secondBestValue);
					
					// If we have shadow traces, we only pick from those with the highest
					// confidence in the last (not yet tried) decision
					List<ClientHistory> secondBestHistories = new ArrayList<>(hists.size());
					filterBestShadowHistories(secondBestHistories, tempList);
					
					// Randomly pick one of the second-best histories
					while ((secondBestHistory == null || secondBestHistory == bestHistory)
							&& !secondBestHistories.isEmpty()) {
						int idx = DeterministicRandom.theRandom.nextInt(secondBestHistories.size());
						secondBestHistory = secondBestHistories.remove(idx);
					}
				}
			}
			
			// If we did not find two histories to combine, try again
			if (bestHistory == null || secondBestHistory == null)
				continue;
			
			// Combine the two histories
			ClientHistory hist = combine(bestHistory, secondBestHistory, hists);
			if (hist.hasOnlyEmptyDecisions()) {
				System.err.println("Result of genetic combination has only empty decisions");
				hists.remove(bestHistory);
				continue;
			}
			
			// If the genetic recombination gives us a new history that we have
			// already tried, we remove the best candidate and give the weaker
			// ones a chance
			if (hists.contains(hist))
				hists.remove(bestHistory);
			else
				return hist;
		}
		
		// nothing left to combine
		return null;
	}

	
	private void filterBestShadowHistories(
			List<ClientHistory> secondBestHistories,
			List<ClientHistory> tempList) {
		// First pass: Copy over all non-shadow traces and find the highest confidence
		// value for the shadow traces
		int highestConfidence = 0;
		for (ClientHistory history : tempList) {
			if (history.isShadowTrace()) {
				Pair<DecisionRequest, AnalysisDecision> lastDecision = history.getDecisionAndResponse()
						.get(history.getDecisionAndResponse().size() - 1);
				highestConfidence = Math.max(highestConfidence,
						lastDecision.getSecond().getDecisionWeight());
			}
			else
				secondBestHistories.add(history);
		}
		
		// Second pass: Copy over all shadow traces with the confidence value found in
		// step 1
		for (ClientHistory history : tempList) {
			if (history.isShadowTrace()) {
				Pair<DecisionRequest, AnalysisDecision> lastDecision = history.getDecisionAndResponse()
						.get(history.getDecisionAndResponse().size() - 1);
				if (lastDecision.getSecond().getDecisionWeight() == highestConfidence)
					secondBestHistories.add(history);
			}
		}
	}

	
	private void getHistoriesByApproachLevel(Collection<ClientHistory> bestHistories,
			Collection<ClientHistory> hists, int bestValue) {
		for (ClientHistory curHistory : hists) {
			int curValue = curHistory.getProgressValue("ApproachLevel");
			if (curValue == bestValue) {
				bestHistories.add(curHistory);
			}
		}
	}

}
