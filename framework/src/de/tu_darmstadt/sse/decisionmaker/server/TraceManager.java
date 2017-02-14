package de.tu_darmstadt.sse.decisionmaker.server;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


public class TraceManager {
	
	private final ConcurrentMap<Long, ThreadTraceManager> threadToManager = new ConcurrentHashMap<>();
	private final Set<ThreadTraceManagerCreatedHandler> onCreateHandler = new HashSet<>();

	
	public TraceManager() {
		//
	}
	
	
	public Collection<ThreadTraceManager> getAllThreadTraceManagers() {
		return threadToManager.values();
	}

	
	public ThreadTraceManager getThreadTraceManager(long threadId) {
		return threadToManager.get(threadId);
	}

	
	public ThreadTraceManager getOrCreateThreadTraceManager(long threadId) {
		ThreadTraceManager newManager = new ThreadTraceManager(threadId);
		ThreadTraceManager existingManager = threadToManager.putIfAbsent(threadId, newManager);
		if (existingManager == null) {
			for (ThreadTraceManagerCreatedHandler handler : onCreateHandler)
				handler.onThreadTraceManagerCreated(newManager);
			return newManager;
		}
		else
			return existingManager;
	}
	
	
	public void addThreadTraceCreateHandler(ThreadTraceManagerCreatedHandler handler) {
		this.onCreateHandler.add(handler);
	}

}
