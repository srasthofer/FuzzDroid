package de.tu_darmstadt.sse.appinstrumentation.transformer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import soot.Body;
import soot.Local;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.SootMethodRef;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.VoidType;
import soot.javaToJimple.LocalGenerator;
import soot.jimple.InvokeExpr;
import soot.jimple.Jimple;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.Stmt;
import de.tu_darmstadt.sse.appinstrumentation.UtilInstrumenter;


public class GlobalInstanceTransformer extends SceneTransformer {

	@Override
	protected void internalTransform(String phaseName, Map<String, String> options) {
		// Get some system components
		SootClass scActivity = Scene.v().getSootClassUnsafe("android.app.Activity");
		SootClass scService = Scene.v().getSootClassUnsafe("android.app.Service");
		SootClass scBroadcastReceiver = Scene.v().getSootClassUnsafe("android.app.BroadcastReceiver");
		SootClass scContentProvider = Scene.v().getSootClassUnsafe("android.app.ContentProvider");
		
		// Get the registration class
		SootClass scRegistrar = Scene.v().getSootClassUnsafe("de.tu_darmstadt.sse.additionalappclasses.ComponentCallerService");
		SootMethodRef smRegistrarRef = scRegistrar.getMethodByName("registerGlobalInstance").makeRef();
		
		// Get the getClass() method
		Type classType = Scene.v().getType("java.lang.Class");
		SootMethodRef smGetClass = Scene.v().getObjectType().getSootClass().getMethod("java.lang.Class getClass()").makeRef();
		
		// Is this an Android component?
		for (SootClass sc : Scene.v().getApplicationClasses()) {
			// We only instrument user code
			if (!UtilInstrumenter.isAppDeveloperCode(sc))
				continue;
			
			// Is this class a component?
			if (Scene.v().getOrMakeFastHierarchy().canStoreType(sc.getType(), scActivity.getType())
					|| Scene.v().getOrMakeFastHierarchy().canStoreType(sc.getType(), scService.getType())
					|| Scene.v().getOrMakeFastHierarchy().canStoreType(sc.getType(), scBroadcastReceiver.getType())
					|| Scene.v().getOrMakeFastHierarchy().canStoreType(sc.getType(), scContentProvider.getType())) {
				Body b = null;
				Local locThis = null;
				Unit lastUnit = null;
				
				// Do we already have a constructor?
				SootMethod cons = sc.getMethodUnsafe("void <init>()");
				if (cons == null) {
					SootMethod smSuperClassCons = sc.getSuperclass().getMethodUnsafe("void <init>()");
					if (smSuperClassCons == null)
						continue;
					
					// Create the new constructor
					cons = new SootMethod("<init>", Collections.<Type>emptyList(), VoidType.v());
					sc.addMethod(cons);
					cons.setActiveBody(b = Jimple.v().newBody(cons));
					
					// Add a reference to the "this" object
					locThis = Jimple.v().newLocal("this", sc.getType());
					b.getLocals().add(locThis);
					b.getUnits().add(Jimple.v().newIdentityStmt(locThis, Jimple.v().newThisRef(sc.getType())));
					
					// Add a call to the superclass constructor
					b.getUnits().add(Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(locThis,
							smSuperClassCons.makeRef())));
					
					// Add a return statement
					b.getUnits().add(lastUnit = Jimple.v().newReturnVoidStmt());
				}
				else {
					b = cons.getActiveBody();
					locThis = b.getThisLocal();
					
					// Find where we can inject out code. We must have called
					// the super constructor first, or the Dalvik verifier will
					// complain that the "this" local is not yet initialized.
					for (Unit u : b.getUnits()) {
						Stmt s = (Stmt) u;
						if (s.containsInvokeExpr()) {
							InvokeExpr iexpr = s.getInvokeExpr();
							if (iexpr instanceof SpecialInvokeExpr) {
								if (iexpr.getMethod().getName().equals("<init>")
										&& ((SpecialInvokeExpr) iexpr).getBase() == locThis) {
									lastUnit = b.getUnits().getSuccOf(u);
									break;
								}
							}
						}
					}
				}
				
				// Get the class
				LocalGenerator localGen = new LocalGenerator(b);
				Local locClass = localGen.generateLocal(classType);
				Stmt stmtAssignClass = Jimple.v().newAssignStmt(locClass, Jimple.v().newVirtualInvokeExpr(
						locThis, smGetClass));
				stmtAssignClass.addTag(new InstrumentedCodeTag());
				b.getUnits().insertBefore(stmtAssignClass, lastUnit);
				
				// Register the instance
				List<Value> argList = new ArrayList<>();
				argList.add(locClass);
				argList.add(locThis);
				Stmt stmtRegister = Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(
						smRegistrarRef, argList));
				stmtRegister.addTag(new InstrumentedCodeTag());
				b.getUnits().insertBefore(stmtRegister, lastUnit);
			}
		}
	}

}
