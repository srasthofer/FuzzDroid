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
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.Stmt;
import soot.jimple.ThrowStmt;
import de.tu_darmstadt.sse.apkspecific.CodeModel.CodePosition;
import de.tu_darmstadt.sse.apkspecific.CodeModel.CodePositionManager;
import de.tu_darmstadt.sse.appinstrumentation.UtilInstrumenter;


public class DynamicCallGraphTracking extends AbstractInstrumentationTransformer {

	private final CodePositionManager codePositionManager;
	
	public DynamicCallGraphTracking(CodePositionManager codePositionManager) {
		this.codePositionManager = codePositionManager;
	}
	
	@Override
	protected void internalTransform(Body b, String phaseName,
			Map<String, String> options) {
		// Do not instrument methods in framework classes
		if (!canInstrumentMethod(b.getMethod()))
			return;
		
		// Create method references
		final SootMethodRef callMethodRef = Scene.v().makeMethodRef(
				Scene.v().getSootClass(UtilInstrumenter.JAVA_CLASS_FOR_CODE_POSITIONS),
				"reportMethodCallSynchronous",
				Collections.<Type>singletonList(IntType.v()),
				VoidType.v(),
				true);
		final SootMethodRef returnMethodRef = Scene.v().makeMethodRef(
				Scene.v().getSootClass(UtilInstrumenter.JAVA_CLASS_FOR_CODE_POSITIONS),
				"reportMethodReturnSynchronous",
				Collections.<Type>singletonList(IntType.v()),
				VoidType.v(),
				true);
		final SootMethodRef enterMethodRef = Scene.v().makeMethodRef(
				Scene.v().getSootClass(UtilInstrumenter.JAVA_CLASS_FOR_CODE_POSITIONS),
				"reportMethodEnterSynchronous",
				Collections.<Type>singletonList(IntType.v()),
				VoidType.v(),
				true);
		final SootMethodRef leaveMethodRef = Scene.v().makeMethodRef(
				Scene.v().getSootClass(UtilInstrumenter.JAVA_CLASS_FOR_CODE_POSITIONS),
				"reportMethodLeaveSynchronous",
				Collections.<Type>singletonList(IntType.v()),
				VoidType.v(),
				true);
		
		int lineNum = 0;
		boolean started = true;
		boolean firstNonIdentity = false;
		for (Iterator<Unit> unitIt = b.getUnits().snapshotIterator(); unitIt.hasNext(); ) {
			Stmt stmt = (Stmt) unitIt.next();
			CodePosition codePos = null;
			
			// Do not record trace data on system-generated code
			if (stmt.hasTag(InstrumentedCodeTag.name))
				continue;
			
			// Is this the first non-identity statement in the method
			if (!(stmt instanceof IdentityStmt)) {
				if (started)
					firstNonIdentity = true;
				else
					firstNonIdentity = false;
				started = false;
			}
			
			// Does the control flow leave the current method at the current
			// statement?
			boolean stmtLeavesMethod = stmt instanceof ReturnStmt
					|| stmt instanceof ReturnVoidStmt
					|| stmt instanceof ThrowStmt;
			
			// Get the current code position
			if (stmt.containsInvokeExpr()
					|| firstNonIdentity
					|| stmtLeavesMethod) {
				codePos = codePositionManager.getCodePositionForUnit(stmt,
						b.getMethod().getSignature(),
						lineNum,
						stmt.getJavaSourceStartLineNumber());
			}
			
			// Record method enters
			if (firstNonIdentity) {
				Stmt onEnterStmt = Jimple.v().newInvokeStmt(
						Jimple.v().newStaticInvokeExpr(enterMethodRef,
								IntConstant.v(codePos.getID())));
				onEnterStmt.addTag(new InstrumentedCodeTag());
				b.getUnits().insertBefore(onEnterStmt, stmt);
			}
			
			// Check for method calls
			if (stmt.containsInvokeExpr()) {
				Stmt onCallStmt = Jimple.v().newInvokeStmt(
						Jimple.v().newStaticInvokeExpr(callMethodRef,
								IntConstant.v(codePos.getID())));
				onCallStmt.addTag(new InstrumentedCodeTag());
				b.getUnits().insertBefore(onCallStmt, stmt);
				
				Stmt onReturnStmt = Jimple.v().newInvokeStmt(
						Jimple.v().newStaticInvokeExpr(returnMethodRef,
								IntConstant.v(codePos.getID())));
				onReturnStmt.addTag(new InstrumentedCodeTag());
				b.getUnits().insertAfter(onReturnStmt, stmt);
			}
						
			// Record method leaves
			if (stmtLeavesMethod) {
				Stmt onLeaveStmt = Jimple.v().newInvokeStmt(
						Jimple.v().newStaticInvokeExpr(leaveMethodRef,
								IntConstant.v(codePos.getID())));
				onLeaveStmt.addTag(new InstrumentedCodeTag());
				b.getUnits().insertBefore(onLeaveStmt, stmt);
			}
			
			lineNum++;
		}
	}

}
