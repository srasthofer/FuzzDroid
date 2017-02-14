package de.tu_darmstadt.sse.additionalappclasses.hooking;


public class PersistentMethodHookAfter extends AbstractMethodHookAfter {
	
	private final Object returnValue;
	
	
	public PersistentMethodHookAfter(Object returnValue) {
		this.returnValue = returnValue;
	}
	
	
	@Override
	public Object getReturnValue() {
		return returnValue;
	}

	
	@Override
	public boolean isValueReplacementNecessary() {
		return true;
	}	
	
}
