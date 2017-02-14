package de.tu_darmstadt.sse.sharedclasses.dynamiccfg;

import de.tu_darmstadt.sse.sharedclasses.tracing.TraceItem;


public abstract class AbstractDynamicCFGItem extends TraceItem {
	
	
	private static final long serialVersionUID = -5500762826791899632L;

	public AbstractDynamicCFGItem() {
		super();
	}
	
	public AbstractDynamicCFGItem(int lastExecutedStatement) {
		super(lastExecutedStatement);
	}

}
