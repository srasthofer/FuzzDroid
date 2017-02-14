package de.tu_darmstadt.sse.appinstrumentation.transformer;

import de.tu_darmstadt.sse.appinstrumentation.UtilInstrumenter;
import soot.BodyTransformer;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;


public abstract class AbstractInstrumentationTransformer extends
		BodyTransformer {
	
	
	protected boolean canInstrumentMethod(SootMethod sm) {
		// Check whether this is actually user code
		SootClass sClass = sm.getDeclaringClass();
		if (!UtilInstrumenter.isAppDeveloperCode(sClass))
			return false;
		
		// We do not instrument the dummy main method
		return !Scene.v().getEntryPoints().contains(sm.getSignature());
	}

}
