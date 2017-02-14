//package de.tu_darmstadt.sse.visualization.inputformat;
//
//import java.io.BufferedWriter;
//import java.io.File;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import de.tu_darmstadt.sse.visualization.digraph.Digraph;
//import de.tu_darmstadt.sse.visualization.events.AbstractPathExecutionEvent;
//import de.tu_darmstadt.sse.visualization.events.MethodCalleeEvent;
//import de.tu_darmstadt.sse.visualization.events.MethodCallerEvent;
//import de.tu_darmstadt.sse.visualization.events.ReturnEvent;
//
//
//public abstract class AbstractConverter {
//	
//	private final File inputFile;
//	
//	private final File dotOutputFile;
//	
//	protected List<AbstractPathExecutionEvent> allOrderedEvents = new ArrayList<AbstractPathExecutionEvent>();
//	private final Map<AbstractPathExecutionEvent, String> eventToItem = new HashMap<AbstractPathExecutionEvent, String>();
//	
//	public AbstractConverter(File inputFile, File dotOutputFile) {
//		this.inputFile = inputFile;
//		this.dotOutputFile = dotOutputFile;
//	}
//	
//	public abstract List<AbstractPathExecutionEvent> readEventsFromInput();
//	
//	
//	public File getInputFile() {
//		return inputFile;
//	}
//
//	
//	public File getDotOutputFile() {
//		return dotOutputFile;
//	}
//	
//	
//	public void storeEvents(){
//		allOrderedEvents = readEventsFromInput();
//	}
//
//	
//	public void generateDotFile() {
//		Digraph digraph = new Digraph();
//		
//		for(AbstractPathExecutionEvent event : allOrderedEvents)
//		{
//			if(event instanceof MethodCalleeEvent)
//			{
//				handleMethodCalleeEvent((MethodCalleeEvent)event, digraph);
//			}
//			else if(event instanceof MethodCallerEvent)
//			{
//				handleMethodCallerEvent((MethodCallerEvent)event, digraph);				
//			}
//			else if(event instanceof ReturnEvent)
//			{
//				handleReturnEvent((ReturnEvent)event, digraph);
//			}
//		}
//		
//		//write to file
//		this.writeTofFile(dotOutputFile, digraph);	
//	}
//	
//	
//	private void handleMethodCalleeEvent(MethodCalleeEvent calleeEvent, Digraph digraph)
//	{
//		//Subgraph creation
//		String componentSubgraphLabel = String.format("%s", calleeEvent.getProcessId()); 
//		String componentSubgraphClusterName = String.format("%s", calleeEvent.getProcessId());
//		digraph.addComponentSubgraph(componentSubgraphClusterName, componentSubgraphLabel);
//		
//		String methodSubgraphLabel = calleeEvent.getMethodSignature();
//		String initItem = digraph.addMethodSubgraph(calleeEvent.getMethodSignature(), methodSubgraphLabel, componentSubgraphLabel);
//		
//		if(!eventToItem.containsKey(calleeEvent))
//		{
//			eventToItem.put(calleeEvent, initItem);
//		}
//		
//		MethodCallerEvent callerEvent = this.findCallerEvent(calleeEvent);
//		
//		if(callerEvent != null)
//		{		
//			String callerEventMethodItem = digraph.getCallerMethodItem(
//															String.format("%s", callerEvent.getProcessId()), 
//															callerEvent.getMethodSignature(),
//															calleeEvent.getMethodSignature()
//			);
//			if(callerEventMethodItem == null)
//			{
//				throw new RuntimeException("There should be an item for the caller event");
//			}
//			String calleeEventInitialItem = digraph.getInitialItemOfMethod(String.format("%s", calleeEvent.getProcessId()), calleeEvent.getMethodSignature());
//			
//			digraph.addConnection(callerEventMethodItem, calleeEventInitialItem);
//		}
//	}
//	
//	
//	private void handleMethodCallerEvent(MethodCallerEvent event, Digraph digraph)
//	{
//		String callerItem = digraph.addCallerItemToMethodSubgraph(String.format("%s", event.getProcessId()), event.getMethodSignature(), event.getMethodCalleeSignature());
//		if(!eventToItem.containsKey(event))
//		{
//			eventToItem.put(event, callerItem);
//		}
//		AbstractPathExecutionEvent lastEventInSameMethod = this.findLastEventInSameMethod(event);
//		if(!eventToItem.containsKey(lastEventInSameMethod))
//		{
//			throw new RuntimeException("There should be a subgraph-item for this event!");
//		}
//		
//		digraph.addConnection(eventToItem.get(lastEventInSameMethod), callerItem);
//	}
//	
//	
//	private void handleReturnEvent(ReturnEvent returnEvent, Digraph digraph)
//	{
//		String componentSubgraphLabel = String.format("%s", returnEvent.getProcessId()); 
//		String methodSubgraphLabel = returnEvent.getMethodSignature();
//		
//		MethodCallerEvent callerEvent = this.findCallerEvent(returnEvent);
//		
//		if(callerEvent != null)
//		{		
//			String returnItem = digraph.addReturnItemToMethodSubgraph(componentSubgraphLabel, methodSubgraphLabel, returnEvent.getReturnValue());
//			
//			if(!eventToItem.containsKey(returnEvent))
//			{
//				eventToItem.put(returnEvent, returnItem);
//			}
//			
//			String callerEventMethodItem = digraph.getCallerMethodItem(
//					String.format("%s", callerEvent.getProcessId()), 
//					callerEvent.getMethodSignature(),
//					returnEvent.getMethodSignature()
//			);
//			if(callerEventMethodItem == null)
//			{
//				throw new RuntimeException("There should be an item for the caller event");
//			}
//			
//			digraph.addConnection(returnItem, callerEventMethodItem);
//			
//			AbstractPathExecutionEvent lastEventInSameMethod = this.findLastEventInSameMethod(returnEvent);
//			if(!eventToItem.containsKey(lastEventInSameMethod))
//			{
//				throw new RuntimeException("There should be a subgraph-item for this event!");
//			}
//			
//			digraph.addConnection(eventToItem.get(lastEventInSameMethod), returnItem);
//		}
//	}
//	
//	
//	private MethodCallerEvent findCallerEvent(AbstractPathExecutionEvent event)
//	{
//		if(!allOrderedEvents.contains(event))
//		{
//			throw new RuntimeException("Event " + event + " should be in the list!");
//		}
//		else{
//			int currentIndex = allOrderedEvents.indexOf(event);
//			for(int i = currentIndex-1; i >= 0; i--)
//			{
//				if(allOrderedEvents.get(i) instanceof MethodCallerEvent)
//				{
//					MethodCallerEvent methodCallEvent = (MethodCallerEvent)allOrderedEvents.get(i);
//					if(methodCallEvent.getMethodCalleeSignature().equals(event.getMethodSignature()))
//					{
//						return methodCallEvent;
//					}
//				}
//			}
//		}
//		return null; 
//	}
//	
//	
//	private AbstractPathExecutionEvent findLastEventInSameMethod(AbstractPathExecutionEvent event)
//	{
//		if(!allOrderedEvents.contains(event))
//		{
//			throw new RuntimeException("Event " + event + " should be in the list!");
//		}
//		else{
//			String currentEventMethodSig = event.getMethodSignature();
//			int currentIndex = allOrderedEvents.indexOf(event);
//			for(int i = currentIndex-1; i >= 0; i--)
//			{
//				String otherEventMethodSig = allOrderedEvents.get(i).getMethodSignature();
//				if(otherEventMethodSig.equals(currentEventMethodSig))
//				{
//					return allOrderedEvents.get(i);
//				}
//			}
//		}
//		return null;
//	}
//	
//	
//	private void writeTofFile(File outputFile, Digraph digraph)
//	{
//		String content = digraph.toString();
//		try {
//			if(!outputFile.exists())
//			{				
//				outputFile.createNewFile();
//			}
//			
//			FileWriter fw = new FileWriter(outputFile.getAbsoluteFile());
//			BufferedWriter bw = new BufferedWriter(fw);
//			bw.write(content);
//			bw.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
//}
