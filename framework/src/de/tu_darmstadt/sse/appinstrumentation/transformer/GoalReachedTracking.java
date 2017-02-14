package de.tu_darmstadt.sse.appinstrumentation.transformer;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import soot.Body;
import soot.Scene;
import soot.SootMethodRef;
import soot.Type;
import soot.Unit;
import soot.VoidType;
import soot.jimple.Jimple;
import soot.jimple.Stmt;
import de.tu_darmstadt.sse.appinstrumentation.UtilInstrumenter;

public class GoalReachedTracking extends AbstractInstrumentationTransformer {
	
	private final Set<Unit> targetSignatures;
	
	public GoalReachedTracking(Set<Unit> targetSignatures) {
		this.targetSignatures = targetSignatures;
	}

	@Override
	protected void internalTransform(Body b, String phaseName,
			Map<String, String> options) {
		// Do not instrument methods in framework classes
		if (!canInstrumentMethod(b.getMethod()))
			return;
		
		// Create method references
		final SootMethodRef targetReachedRef = Scene.v().makeMethodRef(
				Scene.v().getSootClass(UtilInstrumenter.JAVA_CLASS_FOR_CODE_POSITIONS),
				"reportTargetReachedSynchronous",
				Collections.<Type>emptyList(),
				VoidType.v(),
				true);
		
		// Iterate over the method and find calls to the target methods
		for (Iterator<Unit> unitIt = b.getUnits().snapshotIterator(); unitIt.hasNext(); ) {
			Stmt stmt = (Stmt) unitIt.next();
			
			if(targetSignatures.contains(stmt)){
				// Notify the server that the target was reached
				Stmt reachedStmt = Jimple.v().newInvokeStmt(
						Jimple.v().newStaticInvokeExpr(targetReachedRef));
				reachedStmt.addTag(new InstrumentedCodeTag());
				b.getUnits().insertBefore(reachedStmt, stmt);
			}
		}
	}

}
