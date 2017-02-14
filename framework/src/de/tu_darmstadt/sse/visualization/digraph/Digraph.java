package de.tu_darmstadt.sse.visualization.digraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Digraph {
	private Map<Subgraph, List<Subgraph>> subgraphs;
	private Map<String, List<String>> connections;
	private Map<String, String> methodSignatureToShortName;
	
	private final String METHOD_COLOR = "blue";
	private final String COMPONENT_COLOR = "black";
	
	public Digraph()
	{
		this.subgraphs = new HashMap<Subgraph, List<Subgraph>>();
		this.connections = new HashMap<String, List<String>>();
		this.methodSignatureToShortName = new HashMap<String, String>();
	}
	
	public void addComponentSubgraph(String clusterName, String label)
	{
		String clusterPseudoName = String.format("cluster_%s", clusterName);
		Subgraph subgraph = new Subgraph(clusterPseudoName, label, COMPONENT_COLOR, new ArrayList<String>());
		if(!subgraphs.containsKey(subgraph))
		{
			this.subgraphs.put(subgraph, new ArrayList<Subgraph>());
		}
	}
	
	public String getInitialItemOfMethod(String componentLabel, String methodLabel)
	{
		Subgraph subgraphForMethod = this.getSubgraphForMethod(componentLabel, methodLabel);
		if(subgraphForMethod == null ||
				subgraphForMethod.getItems().isEmpty())
		{
			throw new RuntimeException("There should be at least one item in the items list");
		}
		return subgraphForMethod.getItems().get(0);
	}
	
	public String getCallerMethodItem(String componentLabel, String methodLabel, String calleeMethodSignature)
	{
		Subgraph subgraphForMethod = this.getSubgraphForMethod(componentLabel, methodLabel);
		if(subgraphForMethod == null ||
				subgraphForMethod.getItems().isEmpty())
		{
			throw new RuntimeException("There should be at least one item in the items list");
		}
		
		for(int i = subgraphForMethod.getItems().size()-1; i >= 0; i--)
		{
			String item = subgraphForMethod.getItems().get(i);
			String shortName = this.generateShortMethodName(calleeMethodSignature);
			if(item.endsWith(String.format("caller_%s", shortName)))
			{
				return item;
			}
		}
		return null;
	}
	
	
	public String addMethodSubgraph(String clusterName, String methodSignature, String componentLabel)
	{	
		String clusterPseudoName = String.format("cluster_%s", this.generateShortMethodName(clusterName));
		String methodPseudoName = this.generateShortMethodName(methodSignature);
		String initialItem = String.format("%s__init", methodPseudoName);
		Subgraph subgraphForMethod = this.getSubgraphForMethod(componentLabel, methodSignature);
		if(subgraphForMethod == null)
		{
			List<String> itemsList = new ArrayList<String>();
			itemsList.add(initialItem);
			subgraphForMethod = new Subgraph(clusterPseudoName, methodPseudoName, METHOD_COLOR, itemsList);
			Subgraph subgraphForComponent = this.getSubgraphForComponent(componentLabel);
			subgraphs.get(subgraphForComponent).add(subgraphForMethod);
		}
		return initialItem;
	}
	
	public String addCallerItemToMethodSubgraph(String componentLabel, String methodLabel, String callerMethodSignature)
	{
		Subgraph subgraphForMethod = this.getSubgraphForMethod(componentLabel, methodLabel);
		String callerShortName = this.generateShortMethodName(callerMethodSignature);		
		String callerItem = String.format("%s__caller_%s", subgraphForMethod.getLabel(), callerShortName);
		subgraphForMethod.addItem(callerItem);		
		return callerItem;
	}
	
	public String addAPIItemToMethodSubgraph(String componentLabel, String methodLabel, String returnValue, String apiCallSignatureWithValues)
	{
		Subgraph subgraphForMethod = this.getSubgraphForMethod(componentLabel, methodLabel);
		String apiItem = null;
		if(returnValue == null)
		{
			apiItem = apiCallSignatureWithValues;
		}
		else
		{
			apiItem = String.format("%s <- %s", returnValue, apiCallSignatureWithValues);
		}
		subgraphForMethod.addItem(apiItem);	
		return apiItem;
	}
	
	public String addReturnItemToMethodSubgraph(String componentLabel, String methodLabel, String returnValue)
	{
		Subgraph subgraphForMethod = this.getSubgraphForMethod(componentLabel, methodLabel);
		String itemContent = String.format("%s__return %s", subgraphForMethod.getLabel(), returnValue);
		subgraphForMethod.addItem(itemContent);
		return itemContent;
	}
	
	public void addConnection(String from, String to)
	{
		if(this.connections.containsKey(from))
		{
			if(!this.connections.get(from).contains(to))
			{
				this.connections.get(from).add(to);
			}			
		}
		else
		{	
			List<String> toList = new ArrayList<String>();
			toList.add(to);
			if(from == null)
			{
				throw new RuntimeException("This should not happen!");
			}
			this.connections.put(from, toList);
		}
	}
	
	private Subgraph getSubgraphForComponent(String label)
	{
		for(Subgraph subgraph : subgraphs.keySet())
		{
			if(subgraph.getLabel().equals(label))
				return subgraph;
		}
		return null;
	}

	
	public Subgraph getSubgraphForMethod(String componentLabel, String methodLabel)
	{
		String methodPseudoName = this.generateShortMethodName(methodLabel);
		Subgraph subgraphForComponent = this.getSubgraphForComponent(componentLabel);
		if(subgraphForComponent == null)
		{
			throw new RuntimeException("There should be already a component subgraph");
		}
		for(Subgraph subgraph : subgraphs.get(subgraphForComponent))
		{
			if(subgraph.getLabel().equals(methodPseudoName))
			{
				return subgraph;
			}
		}
		return null;
	}
	
	private String generateShortMethodName(String methodSignature)
	{
		if(this.methodSignatureToShortName.containsKey(methodSignature))
		{
			return this.methodSignatureToShortName.get(methodSignature);
		}
		else
		{
			String pseudoName = String.format("method%s", this.methodSignatureToShortName.keySet().size());
			this.methodSignatureToShortName.put(methodSignature, pseudoName);
			return pseudoName;
		}
	}
	
	@Override
	public String toString()
	{
		List<String> tmp = new ArrayList<String>();
		StringBuilder sb = new StringBuilder();
		sb.append("digraph d{\n\n");
		
		for(Map.Entry<Subgraph, List<Subgraph>> entry : subgraphs.entrySet())
		{
			sb.append(String.format("subgraph %s {\n \tlabel = \"%s\"; \n\t color = %s;\n\n", 
					entry.getKey().getClusterName(), 
					entry.getKey().getLabel(), 
					entry.getKey().getColor()));
			
			for(Subgraph subgraph : entry.getValue())
			{
				sb.append(String.format("%s\n\n", subgraph.toString()));
			}
			
			sb.append("}");
		}
		
		sb.append("\n\nsubgraph cluster_legend {\n\t");
		for(Map.Entry<String, String> entry : methodSignatureToShortName.entrySet())
		{
			String line = String.format("\"%s -> %s\"\n", entry.getKey(), entry.getValue()); 
			sb.append(line);
			tmp.add(line);
		}
		sb.append(";} \n\n");
		
		//connections
		for(Map.Entry<String, List<String>> entry : connections.entrySet())
		{
			for(String to : entry.getValue())
			{
				sb.append(String.format("\t\"%s\" -> \"%s\";\n", entry.getKey(), to));
			}
		}
		
		for(int i = 0; i < tmp.size()-1; i++)
		{
			sb.append(String.format("\t%s -> %s [style=invis];\n", tmp.get(i).trim(), tmp.get(i+1).trim()));
		}
		
		sb.append("}");
		
		return sb.toString();
	}
}
