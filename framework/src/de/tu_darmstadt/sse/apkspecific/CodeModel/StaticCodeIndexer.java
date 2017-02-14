package de.tu_darmstadt.sse.apkspecific.CodeModel;

import java.util.HashMap;
import java.util.Map;

import de.tu_darmstadt.sse.appinstrumentation.UtilInstrumenter;
import de.tu_darmstadt.sse.appinstrumentation.transformer.InstrumentedCodeTag;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;


public class StaticCodeIndexer {
	
	private Map<Unit, SootMethod> unitToMethod = new HashMap<>();
	
	
	public StaticCodeIndexer() {
		initializeUnitToMethod();
	}
	
	
	private void initializeUnitToMethod() {
		for (SootClass sc : Scene.v().getApplicationClasses())
			if (UtilInstrumenter.isAppDeveloperCode(sc) && sc.isConcrete())
				for (SootMethod sm : sc.getMethods())
					if (sm.isConcrete())
						for (Unit u : sm.retrieveActiveBody().getUnits())
							if (!u.hasTag(InstrumentedCodeTag.name))
								unitToMethod.put(u, sm);
	}
	
	
	public SootMethod getMethodOf(Unit u) {
		return unitToMethod.get(u);
	}

}
