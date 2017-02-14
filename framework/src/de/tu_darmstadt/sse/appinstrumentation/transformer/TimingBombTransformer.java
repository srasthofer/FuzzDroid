package de.tu_darmstadt.sse.appinstrumentation.transformer;

import java.util.Iterator;
import java.util.Map;

import soot.Body;
import soot.Local;
import soot.LongType;
import soot.Scene;
import soot.SootMethod;
import soot.SootMethodRef;
import soot.Unit;
import soot.Value;
import soot.jimple.AddExpr;
import soot.jimple.AssignStmt;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.LongConstant;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.Stmt;
import de.tu_darmstadt.sse.appinstrumentation.UtilInstrumenter;


public class TimingBombTransformer extends AbstractInstrumentationTransformer {

	@Override
	protected void internalTransform(Body body, String phaseName,
			Map<String, String> options) {
		
		if (!canInstrumentMethod(body.getMethod()))
			return;
		
		// Get a reference to the reporter method
		SootMethodRef reporterRef = Scene.v().getMethod("<de.tu_darmstadt.sse.additionalappclasses.tracing.BytecodeLogger: "
				+ "void reportTimingBomb(long,long)>").makeRef();
		
		for (Iterator<Unit> unitIt = body.getUnits().snapshotIterator(); unitIt.hasNext(); ) {
			Unit curUnit = unitIt.next();
			
			if(curUnit instanceof InvokeStmt) {
				InvokeStmt invokeStmt = (InvokeStmt) curUnit;
				InvokeExpr expr = invokeStmt.getInvokeExpr();
				String methodSig = expr.getMethod().getSignature();
				
				if(methodSig.equals("<android.app.AlarmManager: void set(int,long,android.app.PendingIntent)>"))
					prepareAlarmManagerSet(body, invokeStmt, reporterRef);
				else if(methodSig.equals("<android.os.Handler: boolean postDelayed(java.lang.Runnable,long)>"))
					prepareHandlerPostDelayed(body, invokeStmt, reporterRef);
			}
		}
		
	}
	
	private void prepareAlarmManagerSet(Body body, InvokeStmt setStmt, SootMethodRef reportRef) {
		Value oldVal = setStmt.getInvokeExpr().getArg(1);
		
		Local longLocal = UtilInstrumenter.generateFreshLocal(body, LongType.v());
		SootMethod currentTimeMillis = Scene.v().getMethod("<java.lang.System: long currentTimeMillis()>");		
		StaticInvokeExpr timeInvoke = Jimple.v().newStaticInvokeExpr(currentTimeMillis.makeRef());		
		AssignStmt timeInitalize = Jimple.v().newAssignStmt(longLocal, timeInvoke);
		
		AddExpr addTime = Jimple.v().newAddExpr(longLocal, LongConstant.v(2000L));
		AssignStmt timeAssign = Jimple.v().newAssignStmt(longLocal, addTime);
				
		
		body.getUnits().insertBefore(timeInitalize, setStmt);
		body.getUnits().insertBefore(timeAssign, setStmt);
		
		InvokeExpr expr = setStmt.getInvokeExpr();
		expr.setArg(0, IntConstant.v(0));
		expr.setArg(1, longLocal);
		
		// Report the change
		InvokeStmt reportStmt = Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(
				reportRef, oldVal, longLocal));
		reportStmt.addTag(new InstrumentedCodeTag());
		body.getUnits().insertAfter(reportStmt, setStmt);
	}
	
	private void prepareHandlerPostDelayed(Body body, Stmt invokeStmt, SootMethodRef reportRef) {
		InvokeExpr expr = invokeStmt.getInvokeExpr();
		
		Value oldValue = expr.getArg(1);
		Value newValue = LongConstant.v(2000L);
		
		expr.setArg(1, newValue);

		// Report the change
		InvokeStmt reportStmt = Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(
				reportRef, oldValue, newValue));
		reportStmt.addTag(new InstrumentedCodeTag());
		body.getUnits().insertAfter(reportStmt, invokeStmt);
	}

}
