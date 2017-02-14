package de.tu_darmstadt.sse.appinstrumentation.transformer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import soot.Body;
import soot.Scene;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.Jimple;
import soot.jimple.Stmt;


public class ClassLoaderTransformer extends AbstractInstrumentationTransformer {

	private final SootMethod methodDexFileLoadClass = Scene.v().getMethod(
			"<dalvik.system.DexFile: java.lang.Class loadClass(java.lang.String,java.lang.ClassLoader)>");
	private final SootMethod methodOwnLoader = Scene.v().getMethod(
			"<de.tu_darmstadt.sse.additionalappclasses.classloading.InterceptingClassLoader: "
					+ "java.lang.Class loadClass(dalvik.system.DexFile,java.lang.String,java.lang.ClassLoader)>");
	
	@Override
	protected void internalTransform(Body b, String phaseName,
			Map<String, String> options) {
		// Do not instrument methods in framework classes
		if (!canInstrumentMethod(b.getMethod()))
			return;
		
		// Check for calls to DexFile.loadClass
		for (Iterator<Unit> unitIt = b.getUnits().snapshotIterator(); unitIt.hasNext(); ) {
			Stmt stmt = (Stmt) unitIt.next();
			if (stmt.hasTag(InstrumentedCodeTag.name))
				continue;
			if (!(stmt instanceof AssignStmt))
				continue;
			AssignStmt assignStmt = (AssignStmt) stmt;
			
			if (stmt.containsInvokeExpr()) {
				InvokeExpr iexpr = stmt.getInvokeExpr();
				if (iexpr.getMethod()  == methodDexFileLoadClass) {
					List<Value> args = new ArrayList<>();
					args.add(((InstanceInvokeExpr) iexpr).getBase());
					args.addAll(iexpr.getArgs());
					InvokeExpr newLoadExpr = Jimple.v().newStaticInvokeExpr(methodOwnLoader.makeRef(), args);
					b.getUnits().swapWith(stmt, Jimple.v().newAssignStmt(assignStmt.getLeftOp(), newLoadExpr));
				}
			}
		}
	}

}
