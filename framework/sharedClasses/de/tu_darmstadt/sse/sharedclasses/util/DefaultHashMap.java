package de.tu_darmstadt.sse.sharedclasses.util;

import java.util.HashMap;

public class DefaultHashMap<K,V> extends HashMap<K,V> {
	private static final long serialVersionUID = -1099648480486824057L;
	
	protected V defaultValue;
	  public DefaultHashMap(V defaultValue) {
	    this.defaultValue = defaultValue;
	  }
	  @Override
	  public V get(Object k) {
	    return containsKey(k) ? super.get(k) : defaultValue;
	  }
}