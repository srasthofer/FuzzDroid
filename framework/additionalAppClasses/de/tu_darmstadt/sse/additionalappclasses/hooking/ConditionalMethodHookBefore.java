package de.tu_darmstadt.sse.additionalappclasses.hooking;

import java.util.HashSet;
import java.util.Set;

import com.morgoo.hook.zhook.MethodHook.MethodHookParam;

import de.tu_darmstadt.sse.sharedclasses.util.Pair;

public class ConditionalMethodHookBefore extends AbstractMethodHookBefore{
	
	private boolean valueReplacementNecessary = false;
	
	private final Set<ParameterConditionValueInfo> paramConditions;
	
	private Set<Pair<Integer, Object>> newParamObjectPairs = new HashSet<Pair<Integer, Object>>();
	
	public ConditionalMethodHookBefore(Set<ParameterConditionValueInfo> paramConditions) {
		this.paramConditions = paramConditions;
	}
	
	
	public void testConditionSatisfaction(MethodHookParam originalMethodInfo) {
		for(ParameterConditionValueInfo paramConditionValuePair : paramConditions) {
			Condition paramCondition = paramConditionValuePair.getCondition();
			if(paramCondition.isConditionSatisfied(originalMethodInfo)) {
				valueReplacementNecessary = true;
				int paramIndex = paramConditionValuePair.getParamIndex();
				Object newParamValue = paramConditionValuePair.getNewValue();
				newParamObjectPairs.add(new Pair<Integer, Object>(paramIndex, newParamValue));
			}
		}
	}
	
	@Override
	public Set<Pair<Integer, Object>> getParamValuesToReplace() {
		return newParamObjectPairs;
	}

	@Override
	public boolean isValueReplacementNecessary() {
		return valueReplacementNecessary;
	}		
}
