package de.tu_darmstadt.sse.additionalappclasses.hooking;

import java.io.Serializable;


public class DummyValue implements Serializable{
	private static final long serialVersionUID = -3619572732272288459L;
	
	@Override
	public String toString() {
		return "<DUMMY>";
	}
}
