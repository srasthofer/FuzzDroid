package de.tu_darmstadt.sse.additionalappclasses.hooking;


public class PersistentFieldHookAfter extends AbstractFieldHookAfter{
	private final Object newValue;
	
	public PersistentFieldHookAfter(Object newValue) {
		this.newValue = newValue;
	}
	
	@Override
	public boolean isValueReplacementNecessary() {
		return true;
	}

	@Override
	public Object getNewValue() {
		return newValue;
	}		
}
