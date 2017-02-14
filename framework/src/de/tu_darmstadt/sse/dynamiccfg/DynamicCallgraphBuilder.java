package de.tu_darmstadt.sse.dynamiccfg;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import soot.SootMethod;
import soot.Unit;
import de.tu_darmstadt.sse.apkspecific.CodeModel.CodePositionManager;
import de.tu_darmstadt.sse.apkspecific.CodeModel.StaticCodeIndexer;
import de.tu_darmstadt.sse.sharedclasses.dynamiccfg.AbstractDynamicCFGItem;
import de.tu_darmstadt.sse.sharedclasses.dynamiccfg.MethodCallItem;
import de.tu_darmstadt.sse.sharedclasses.dynamiccfg.MethodEnterItem;
import de.tu_darmstadt.sse.sharedclasses.dynamiccfg.MethodReturnItem;


public class DynamicCallgraphBuilder {
	
	private final Callgraph callgraph;
	private final StaticCodeIndexer codeIndexer;
	private final CodePositionManager codePositionManager;
	
	private final Queue<AbstractDynamicCFGItem> itemQueue = new LinkedBlockingQueue<>();
	
	
	public DynamicCallgraphBuilder(Callgraph cg,
			CodePositionManager codePositionManager,
			StaticCodeIndexer codeIndexer) {
		this.callgraph = cg;
		this.codeIndexer = codeIndexer;
		this.codePositionManager = codePositionManager;
	}
	
	
	public void enqueueItem(AbstractDynamicCFGItem item) {
		this.itemQueue.add(item);
	}
	
	
	public void updateCFG() {
		Unit lastCallSite = null;
		while (itemQueue.size() >= 2) {
			AbstractDynamicCFGItem item = itemQueue.poll();
			if (item == null)
				break;
			
			if (item instanceof MethodCallItem) {
				MethodCallItem mci = (MethodCallItem) item;
				lastCallSite = codePositionManager.getUnitForCodePosition(
						mci.getLastExecutedStatement());
			}
			else if (item instanceof MethodEnterItem && lastCallSite != null) {
				MethodEnterItem mei = (MethodEnterItem) item;
				Unit newMethodUnit = codePositionManager.getUnitForCodePosition(
						mei.getLastExecutedStatement());
				SootMethod callee = codeIndexer.getMethodOf(newMethodUnit);
				
				// Create the callgraph edge
				callgraph.addEdge(new Edge(lastCallSite, callee));
				lastCallSite = null;
			}
			else if (item instanceof MethodReturnItem) {
				// This ends the current call
				lastCallSite = null;
			}
		}
	}
	
}
