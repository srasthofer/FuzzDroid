package de.tu_darmstadt.sse.appinstrumentation.transformer;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import soot.Body;
import soot.BooleanType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.jimple.GotoStmt;
import soot.jimple.IfStmt;
import soot.jimple.IntConstant;
import soot.jimple.Jimple;
import soot.jimple.NopStmt;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.Stmt;
import de.tu_darmstadt.sse.appinstrumentation.UtilInstrumenter;


public class ConditionTracking extends AbstractInstrumentationTransformer {
	
	private final Set<String> branchTargetStmt = new HashSet<String>();
	
	@Override
	protected void internalTransform(Body body, String phaseName, Map<String, String> options) {
		// Do not instrument methods in framework classes
		if (!canInstrumentMethod(body.getMethod()))
			return;
		
		//important to use snapshotIterator here
		Iterator<Unit> iterator = body.getUnits().snapshotIterator();
		
		while(iterator.hasNext()){
			Unit unit = iterator.next();
			
			if(unit instanceof IfStmt
					&& !unit.hasTag(InstrumentedCodeTag.name)) {
				instrumentEachBranchAccess(body, unit);
			}
		}
		
	}
	
	
	private void instrumentEachBranchAccess(Body body, Unit unit){
		SootClass sootClass = Scene.v().getSootClass(
				UtilInstrumenter.JAVA_CLASS_FOR_PATH_INSTRUMENTATION);
		
		// Create the method invocation
		SootMethod createAndAdd = sootClass.getMethod("reportConditionOutcomeSynchronous",
				Collections.<Type>singletonList(BooleanType.v()));
		StaticInvokeExpr sieThen = Jimple.v().newStaticInvokeExpr(
				createAndAdd.makeRef(), IntConstant.v(1));
		StaticInvokeExpr sieElse = Jimple.v().newStaticInvokeExpr(
				createAndAdd.makeRef(), IntConstant.v(0));
		Unit sieThenUnit = Jimple.v().newInvokeStmt(sieThen);
		sieThenUnit.addTag(new InstrumentedCodeTag());
		Unit sieElseUnit = Jimple.v().newInvokeStmt(sieElse);
		sieElseUnit.addTag(new InstrumentedCodeTag());
		
		//treatment of target statement ("true"-branch)
		IfStmt ifStmt = (IfStmt)unit;
		Stmt targetStmt = ifStmt.getTarget();
		if(!branchTargetStmt.contains(targetStmt.toString())) {
			branchTargetStmt.add(sieThenUnit.toString());
			body.getUnits().insertBefore(sieThenUnit, targetStmt);
			
			NopStmt nop = Jimple.v().newNopStmt();
			GotoStmt gotoNop = Jimple.v().newGotoStmt(nop);
			body.getUnits().insertBeforeNoRedirect(nop, targetStmt);
			body.getUnits().insertBeforeNoRedirect(gotoNop, sieThenUnit);
		}
		
		
		//treatment of "else"-branch
		body.getUnits().insertAfter(sieElseUnit, unit);
	}
	
}
