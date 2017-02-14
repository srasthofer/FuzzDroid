package de.tu_darmstadt.sse.additionalappclasses.hooking;

import de.tu_darmstadt.sse.additionalappclasses.tracing.BytecodeLogger;


public abstract class AbstractFieldHookAfter {
	
	protected int getLastCodePosition() {
		return BytecodeLogger.getLastExecutedStatement();
	}
	
	
	public abstract boolean isValueReplacementNecessary();

	
	public abstract Object getNewValue();
}
