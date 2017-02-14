package de.tu_darmstadt.sse.decisionmaker.analysis.dynamicValues;

import java.util.HashSet;
import java.util.Set;

import soot.util.ConcurrentHashMultiMap;
import soot.util.MultiMap;


public class DynamicValueContainer implements Cloneable {
	
	private Set<DynamicValueUpdateHandler> dynValueHandlers = new HashSet<DynamicValueUpdateHandler>();
	private MultiMap<Integer, DynamicValue> values = new ConcurrentHashMultiMap<>();
	
	
	public DynamicValueContainer() {
		//
	}
	
	
	private DynamicValueContainer(DynamicValueContainer original) {
		this.dynValueHandlers.addAll(original.dynValueHandlers);
		this.values.putAll(original.values);
	}
	
	
	public void add(int codePosition, DynamicValue value) {
		values.put(codePosition, value);
		for(DynamicValueUpdateHandler handler : dynValueHandlers)
			handler.onDynamicValueAvailable(value, codePosition);
	}
	
	
	public Set<DynamicValue> getValues() {
		return values.values();
	}
	
	
	public Set<DynamicValue> getValuesAtCodePosition(int codePosition) {
		return values.get(codePosition);
	}

	
	public void clear() {
		values.clear();
	}
	
	public void addDynamicValueUpdateHandler(DynamicValueUpdateHandler handler) {
		dynValueHandlers.add(handler);
	}
	
	@Override
	public DynamicValueContainer clone() {
		return new DynamicValueContainer(this);
	}
	
}
