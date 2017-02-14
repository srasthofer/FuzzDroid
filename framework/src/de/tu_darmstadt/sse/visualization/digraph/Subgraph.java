package de.tu_darmstadt.sse.visualization.digraph;

import java.util.List;

public class Subgraph {
	private String clusterName;
	private String label;
	private String color;
	private List<String> items;
	
	public Subgraph(String clusterName, String label, String color, List<String> items)
	{
		this.clusterName = clusterName;
		this.label = label;
		this.color = color;
		this.items = items;
	}
	
	public void addItem(String item)
	{
		if(!this.items.contains(item))
		{
			this.items.add(item);
		}		
	}
	
	public int hashCode()
	{
		return label.hashCode();
	}
	
	public boolean equals(Object obj)
	{
		if (!(obj instanceof Subgraph))
            return false;
        if (obj == this)
            return true;

        Subgraph rhs = (Subgraph) obj;
        return this.label.equals(rhs.label);
	}

	public String getLabel() {
		return label;
	}

	public String getColor() {
		return color;
	}

	public List<String> getItems() {
		return items;
	}

	public String getClusterName() {
		return clusterName;
	}	
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		
		sb.append(String.format("\tsubgraph %s { \n\t\t", this.getClusterName()));
		for(String item : this.getItems())
		{
			sb.append(String.format("\"%s\" ", item));
		}
		sb.append(String.format(";\n\tlabel = \"%s\";\n\tcolor = %s;",
				this.getLabel(), this.getColor()));
		
		sb.append("}");
		
		return sb.toString();
	}
}
