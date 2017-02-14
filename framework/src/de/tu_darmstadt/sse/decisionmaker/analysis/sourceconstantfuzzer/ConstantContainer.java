package de.tu_darmstadt.sse.decisionmaker.analysis.sourceconstantfuzzer;

import java.util.HashMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class ConstantContainer {

	// map class -> constants in a set to avoid duplicates
	private HashMap<String, ConcurrentSkipListSet<Object>> tmpSetMap = new HashMap<String, ConcurrentSkipListSet<Object>>();
	// map class -> constants in array for random access via index
	private HashMap<String, Object[]> arrayMap = new HashMap<String, Object[]>();

	private ConcurrentSkipListSet<Object> allValuesSet = new ConcurrentSkipListSet<Object>();
	private Object[] allValues = new Object[] {};
	
	private boolean isEmpty = true;

	
	public HashMap<String, Object[]> getArrayMap() {
		return arrayMap;
	}

	public Object[] getAllValues() {
		return allValues;
	}

	
	public void insertTmpValue(String className, Object value) {
		ConcurrentSkipListSet<Object> constantSet = null;

		if (tmpSetMap.containsKey(className)) {
			constantSet = tmpSetMap.get(className);
		} else {
			constantSet = new ConcurrentSkipListSet<Object>();
			tmpSetMap.put(className, constantSet);
		}
		constantSet.add(value);
		allValuesSet.add(value);
		
		isEmpty = false;
	}

	public boolean isEmpty() {
		return isEmpty;
	}

	
	public void convertSetsToArrays() {
		for (String clazz : tmpSetMap.keySet()) {
			ConcurrentSkipListSet<Object> constantSet = tmpSetMap.get(clazz);
			Object[] constantObjects = constantSet.toArray(new Object[constantSet.size()]);

			arrayMap.put(clazz, constantObjects);
		}

		allValues = allValuesSet.toArray();
	}

}
