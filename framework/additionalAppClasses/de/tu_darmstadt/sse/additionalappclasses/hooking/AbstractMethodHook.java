package de.tu_darmstadt.sse.additionalappclasses.hooking;

import de.tu_darmstadt.sse.additionalappclasses.tracing.BytecodeLogger;


public abstract class AbstractMethodHook {
	
	
	protected int getLastCodePosition() {
		return BytecodeLogger.getLastExecutedStatement();
	}
	
	
	public abstract boolean isValueReplacementNecessary();

}
