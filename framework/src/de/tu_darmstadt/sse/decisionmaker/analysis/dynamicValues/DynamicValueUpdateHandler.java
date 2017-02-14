package de.tu_darmstadt.sse.decisionmaker.analysis.dynamicValues;


public interface DynamicValueUpdateHandler {
	
	public void onDynamicValueAvailable(DynamicValue stringValue, int lastExecutedStatement);
}
