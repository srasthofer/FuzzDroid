package de.tu_darmstadt.sse.dynamiccfg;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import soot.Unit;


public class Callgraph implements Cloneable {
	
	private Set<Edge> edges = new HashSet<>();
	private Map<Unit, Set<Edge>> edgesOut = new HashMap<>();
	
	
	public Callgraph() {
		
	}
	
	
	private Callgraph(Callgraph original) {
		this.edges.addAll(original.edges);
		this.edgesOut.putAll(original.edgesOut);
	}
	
	
	public boolean addEdge(Edge edge) {
		if (!this.edges.add(edge))
			return false;
		
		// Add the lookup values
		Set<Edge> edgeSet = edgesOut.get(edge.getCallSite());
		if (edgeSet == null)
			edgesOut.put(edge.getCallSite(), edgeSet = new HashSet<>());
		return edgeSet.add(edge);
	}
	
	
	public Set<Edge> getEdges() {
		return Collections.unmodifiableSet(this.edges);
	}
	
	
	public Set<Edge> getEdgesOutOf(Unit u) {
		return edgesOut.get(u);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((edges == null) ? 0 : edges.hashCode());
		result = prime * result
				+ ((edgesOut == null) ? 0 : edgesOut.hashCode());
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
		Callgraph other = (Callgraph) obj;
		if (edges == null) {
			if (other.edges != null)
				return false;
		} else if (!edges.equals(other.edges))
			return false;
		if (edgesOut == null) {
			if (other.edgesOut != null)
				return false;
		} else if (!edgesOut.equals(other.edgesOut))
			return false;
		return true;
	}
	
	@Override
	public Callgraph clone() {
		return new Callgraph(this);
	}

}
