package de.tu_darmstadt.sse.additionalappclasses.hooking;

import com.morgoo.hook.zhook.MethodHook.MethodHookParam;


public class ConditionalMethodHookAfter extends AbstractMethodHookAfter{
	
	private final Condition condition;
	
	private final Object returnValue;
	
	private boolean valueReplacementNecessary;
	
	
	public ConditionalMethodHookAfter(Condition condition, Object returnValue) {
		this.condition = condition;
		this.returnValue = returnValue;
	}
	
	
	public void testConditionSatisfaction(MethodHookParam originalMethodInfo) {
		valueReplacementNecessary = condition.isConditionSatisfied(originalMethodInfo);
	}
	
	
	@Override
	public Object getReturnValue() {				
		return returnValue;
	}

	
	@Override
	public boolean isValueReplacementNecessary() {
		return valueReplacementNecessary;
	}	
	
}
