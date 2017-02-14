package de.tu_darmstadt.sse.decisionmaker.analysis.symbolicexecution.datastructure;

public class SMTConstantValue<T> implements SMTValue {
	private final T constantValue;
	
	public SMTConstantValue(T constantValue) {
		this.constantValue = constantValue;
	}
	
	public String toString() {
		String returnValue = null;
		if(constantValue instanceof String)
			returnValue = String.format("\"%s\"", constantValue.toString());
		else
			returnValue = constantValue.toString();
		return returnValue;
	}
}
