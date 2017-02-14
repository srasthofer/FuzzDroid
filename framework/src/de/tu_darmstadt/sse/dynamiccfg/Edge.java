package de.tu_darmstadt.sse.dynamiccfg;

import soot.SootMethod;
import soot.Unit;


public class Edge {
	
	private final Unit callSite;
	private final SootMethod callee;
	
	
	public Edge(Unit callSite, SootMethod callee) {
		this.callSite = callSite;
		this.callee = callee;
	}
	
	@Override
	public String toString() {
		return this.callSite + " -> " + this.callee;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((callSite == null) ? 0 : callSite.hashCode());
		result = prime * result + ((callee == null) ? 0 : callee.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Edge other = (Edge) obj;
		if (callSite == null) {
			if (other.callSite != null)
				return false;
		} else if (!callSite.equals(other.callSite))
			return false;
		if (callee == null) {
			if (other.callee != null)
				return false;
		} else if (!callee.equals(other.callee))
			return false;
		return true;
	}
	
	
	public Unit getCallSite() {
		return this.callSite;
	}
	
	
	public SootMethod getCallee() {
		return this.callee;
	}

}
