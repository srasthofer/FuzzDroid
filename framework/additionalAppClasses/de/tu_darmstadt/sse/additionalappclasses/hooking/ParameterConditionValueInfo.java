package de.tu_darmstadt.sse.additionalappclasses.hooking;


public class ParameterConditionValueInfo {
	
	private final int paramIndex;
	
	private final Condition condition;
	
	private final Object newValue;
	
	public ParameterConditionValueInfo(int paramIndex, Condition condition, Object newValue) {
		this.paramIndex = paramIndex;
		this.condition = condition;
		this.newValue = newValue;
	}

	public int getParamIndex() {
		return paramIndex;
	}

	public Condition getCondition() {
		return condition;
	}

	public Object getNewValue() {
		return newValue;
	}				
}
