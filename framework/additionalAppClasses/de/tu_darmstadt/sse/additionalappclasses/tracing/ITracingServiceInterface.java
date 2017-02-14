package de.tu_darmstadt.sse.additionalappclasses.tracing;

import de.tu_darmstadt.sse.sharedclasses.tracing.TraceItem;


interface ITracingServiceInterface {
	
	
	public void dumpQueue();
	
	
	public void enqueueTraceItem(TraceItem ti);
	
}
