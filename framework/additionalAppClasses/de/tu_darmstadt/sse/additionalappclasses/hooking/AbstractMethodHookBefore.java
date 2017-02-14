package de.tu_darmstadt.sse.additionalappclasses.hooking;

import java.util.Set;

import de.tu_darmstadt.sse.sharedclasses.util.Pair;


public abstract class AbstractMethodHookBefore extends AbstractMethodHook {
	
	public AbstractMethodHookBefore() {
	}	

	
	public abstract Set<Pair<Integer, Object>> getParamValuesToReplace();	
}
