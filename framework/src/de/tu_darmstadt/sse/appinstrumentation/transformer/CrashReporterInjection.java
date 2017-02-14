package de.tu_darmstadt.sse.appinstrumentation.transformer;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import soot.Scene;
import soot.SceneTransformer;
import soot.SootMethod;
import soot.SootMethodRef;
import soot.Type;
import soot.Unit;
import soot.VoidType;
import soot.jimple.IdentityStmt;
import soot.jimple.Jimple;
import soot.jimple.Stmt;
import de.tu_darmstadt.sse.appinstrumentation.UtilInstrumenter;


public class CrashReporterInjection extends SceneTransformer {

	private final Set<String> methodsToInstrument;
	
	
	public CrashReporterInjection(Set<String> methodsToInstrument) {
		this.methodsToInstrument = methodsToInstrument;
	}
	
	@Override
	protected void internalTransform(String phaseName,
			Map<String, String> options) {
		// Make a reference to the registration method
		SootMethodRef ref = Scene.v().makeMethodRef(
				Scene.v().getSootClass(UtilInstrumenter.JAVA_CLASS_FOR_CRASH_REPORTING),
				"registerExceptionHandler",
				Collections.<Type>emptyList(),
				VoidType.v(),
				true);
		
		for (String sig : methodsToInstrument) {
			try{
				SootMethod sm = Scene.v().grabMethod(sig);
				if(sm == null)
					continue;
				
				for (Iterator<Unit> unitIt = sm.getActiveBody().getUnits()
						.snapshotIterator(); unitIt.hasNext(); ) {
					Unit curUnit = unitIt.next();
					
					// If we're still inside the IdentityStmt block, there's nothing to
					// instrument
					if (curUnit instanceof IdentityStmt)
						continue;
					
					// Put the registration in
					Stmt stmt = Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(ref));
					stmt.addTag(new InstrumentedCodeTag());					
					sm.getActiveBody().getUnits().insertAfter(stmt, curUnit);
					break;
				}
			}catch(Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	
}
