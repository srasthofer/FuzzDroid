package de.tu_darmstadt.sse.appinstrumentation.transformer;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import soot.Body;
import soot.IntType;
import soot.Scene;
import soot.SootMethodRef;
import soot.Type;
import soot.Unit;
import soot.VoidType;
import soot.jimple.IdentityStmt;
import soot.jimple.IntConstant;
import soot.jimple.Jimple;
import soot.jimple.Stmt;
import de.tu_darmstadt.sse.apkspecific.CodeModel.CodePosition;
import de.tu_darmstadt.sse.apkspecific.CodeModel.CodePositionManager;
import de.tu_darmstadt.sse.appinstrumentation.UtilInstrumenter;


public class CodePositionTracking extends AbstractInstrumentationTransformer {
	
	private final CodePositionManager codePositionManager;
	
	public CodePositionTracking(CodePositionManager codePositionManager) {
		this.codePositionManager = codePositionManager;
	}

	@Override
	protected void internalTransform(Body b, String phaseName, Map<String, String> options) {		
		// Do not instrument methods in framework classes
		if (!canInstrumentMethod(b.getMethod()))
			return;
		
		// Make a reference to the tracker method
		SootMethodRef ref = Scene.v().makeMethodRef(
				Scene.v().getSootClass(UtilInstrumenter.JAVA_CLASS_FOR_CODE_POSITIONS),
				"setLastExecutedStatement",
				Collections.<Type>singletonList(IntType.v()),
				VoidType.v(),
				true);
		final String methodSig = b.getMethod().getSignature();
		
		// Iterate over all the units and add a unit that sets the current
		// execution pointer
		int curLineNum = 0;
		for (Iterator<Unit> unitIt = b.getUnits().snapshotIterator(); unitIt.hasNext(); ) {
			Unit curUnit = unitIt.next();
			
			// If we're still inside the IdentityStmt block, there's nothing to
			// instrument
			if (curUnit instanceof IdentityStmt ||
					// If this unit was instrumented by another transformer, there's nothing to instrument
					curUnit.hasTag(InstrumentedCodeTag.name))
				continue;
			
			// Get the current code positions
			CodePosition codePos = codePositionManager.getCodePositionForUnit(curUnit,
					methodSig, curLineNum++, ((Stmt) curUnit).getJavaSourceStartLineNumber());
			
			Stmt setCodePosStmt = Jimple.v().newInvokeStmt(
					Jimple.v().newStaticInvokeExpr(ref, IntConstant.v(codePos.getID())));
			setCodePosStmt.addTag(new InstrumentedCodeTag());
			
			b.getUnits().insertAfter(setCodePosStmt, curUnit);
		}
	}

}
