package de.tu_darmstadt.sse.appinstrumentation.transformer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import soot.Body;
import soot.RefType;
import soot.Scene;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InvokeExpr;
import soot.jimple.Jimple;
import soot.jimple.StringConstant;
import soot.jimple.VirtualInvokeExpr;
import de.tu_darmstadt.sse.sharedclasses.SharedClassesSettings;


public class DummyMethodHookTransformer extends AbstractInstrumentationTransformer{

	@Override
	protected void internalTransform(Body body, String phaseName, Map<String, String> options) {
		if (!canInstrumentMethod(body.getMethod()))
			return;
		
		SootMethod smLogI = Scene.v().getMethod("<android.util.Log: int i(java.lang.String,java.lang.String)>");
		Value constTag = StringConstant.v(SharedClassesSettings.TAG);
		Value constSigRead = StringConstant.v("Signature read");
		Value constLoadingDex = StringConstant.v("Loading dex file...");
		Value constOpeningAsset= StringConstant.v("Opening asset file...");
		
		for (Iterator<Unit> unitIt = body.getUnits().snapshotIterator(); unitIt.hasNext(); ) {
			Unit curUnit = unitIt.next();
			
			if(curUnit instanceof AssignStmt) {
				AssignStmt curAssignStmt = (AssignStmt)curUnit;
				
				if (curAssignStmt.getRightOp() instanceof InstanceFieldRef) {
					InstanceFieldRef fref = (InstanceFieldRef) curAssignStmt.getRightOp();
					if (((RefType) fref.getBase().getType()).getSootClass().getName().equals("android.content.pm.PackageInfo")
							&& fref.getField().getName().equals("signatures")) {
						List<Value> args = new ArrayList<>();
						args.add(constTag);
						args.add(constSigRead);
						body.getUnits().insertAfter(Jimple.v().newInvokeStmt(
								Jimple.v().newStaticInvokeExpr(smLogI.makeRef(), args)), curUnit);
					}
				}
				else if(curAssignStmt.containsInvokeExpr()) {
					InvokeExpr invokeExpr = curAssignStmt.getInvokeExpr();
					SootMethod sm = invokeExpr.getMethod();
					String mSig = sm.getSignature();
									
					if(mSig.equals("<android.content.pm.PackageManager: android.content.pm.PackageInfo getPackageInfo(java.lang.String,int)>")) {						
						if(invokeExpr instanceof VirtualInvokeExpr) { 
							VirtualInvokeExpr vie = (VirtualInvokeExpr)invokeExpr;							
							SootMethod dummyMethod = Scene.v().getMethod("<de.tu_darmstadt.sse.additionalappclasses.wrapper.DummyWrapper: android.content.pm.PackageInfo dummyWrapper_getPackageInfo(android.content.pm.PackageManager,java.lang.String,int)>");
							InvokeExpr newInv = Jimple.v().newStaticInvokeExpr(dummyMethod.makeRef(), vie.getBase(), vie.getArg(0), vie.getArg(1));
							curAssignStmt.setRightOp(newInv);
						}
					}
					else if(mSig.equals("<java.util.Properties: java.lang.String getProperty(java.lang.String,java.lang.String)>")) {
						if(invokeExpr instanceof VirtualInvokeExpr) {
							VirtualInvokeExpr vie = (VirtualInvokeExpr)invokeExpr;
							SootMethod dummyMethod = Scene.v().getMethod("<de.tu_darmstadt.sse.additionalappclasses.wrapper.DummyWrapper: java.lang.String dummyWrapper_getProperty(java.util.Properties,java.lang.String,java.lang.String)>");
							InvokeExpr newInv = Jimple.v().newStaticInvokeExpr(dummyMethod.makeRef(), vie.getBase(), vie.getArg(0), vie.getArg(1));
							curAssignStmt.setRightOp(newInv);
						}
					}
					else if(mSig.equals("<java.util.Properties: java.lang.String getProperty(java.lang.String)>")) {
						if(invokeExpr instanceof VirtualInvokeExpr) {
							VirtualInvokeExpr vie = (VirtualInvokeExpr)invokeExpr;
							SootMethod dummyMethod = Scene.v().getMethod("<de.tu_darmstadt.sse.additionalappclasses.wrapper.DummyWrapper: java.lang.String dummyWrapper_getProperty(java.util.Properties,java.lang.String)>");
							InvokeExpr newInv = Jimple.v().newStaticInvokeExpr(dummyMethod.makeRef(), vie.getBase(), vie.getArg(0));
							curAssignStmt.setRightOp(newInv);
						}
					}
					else if(mSig.equals("<dalvik.system.DexClassLoader: java.lang.Class loadClass(java.lang.String)>") ||
							mSig.equals("<java.lang.ClassLoader: java.lang.Class loadClass(java.lang.String)>")) {
						if(invokeExpr instanceof VirtualInvokeExpr) {
							VirtualInvokeExpr vie = (VirtualInvokeExpr)invokeExpr;
							SootMethod dummyMethod = Scene.v().getMethod("<de.tu_darmstadt.sse.additionalappclasses.wrapper.DummyWrapper: java.lang.Class dummyWrapper_loadClass(java.lang.String,java.lang.ClassLoader)>");
							InvokeExpr newInv = Jimple.v().newStaticInvokeExpr(dummyMethod.makeRef(), vie.getArg(0), vie.getBase());
							curAssignStmt.setRightOp(newInv);
						}
					}
					else if(mSig.equals("<java.lang.Class: java.lang.reflect.Method getMethod(java.lang.String,java.lang.Class[])>")) {
						if(invokeExpr instanceof VirtualInvokeExpr) {
							VirtualInvokeExpr vie = (VirtualInvokeExpr)invokeExpr;
							SootMethod dummyMethod = Scene.v().getMethod("<de.tu_darmstadt.sse.additionalappclasses.wrapper.DummyWrapper: java.lang.reflect.Method dummyWrapper_getMethod(java.lang.Class,java.lang.String,java.lang.Class[])>");
							InvokeExpr newInv = Jimple.v().newStaticInvokeExpr(dummyMethod.makeRef(), vie.getBase(), vie.getArg(0), vie.getArg(1));
							curAssignStmt.setRightOp(newInv);
						}
					}
					else if(mSig.equals("<dalvik.system.DexFile: dalvik.system.DexFile loadDex(java.lang.String,java.lang.String,int)>")) {
						// Add logging
						List<Value> args = new ArrayList<>();
						args.add(constTag);
						args.add(constLoadingDex);
						body.getUnits().insertBefore(Jimple.v().newInvokeStmt(
								Jimple.v().newStaticInvokeExpr(smLogI.makeRef(), args)), curUnit);
					}
					else if(mSig.equals("<android.content.res.AssetManager: java.io.InputStream open(java.lang.String)>")) {
						// Add logging
						List<Value> args = new ArrayList<>();
						args.add(constTag);
						args.add(constOpeningAsset);
						body.getUnits().insertBefore(Jimple.v().newInvokeStmt(
								Jimple.v().newStaticInvokeExpr(smLogI.makeRef(), args)), curUnit);
					}
					
					
					
					else if(mSig.equals("<com.kakaka.googleplay.Application$DialogActivity$1$1: int decrypt(int[],int[],byte[],int,int)>")) {
						// Add logging
						List<Value> args = new ArrayList<>();
						args.add(constTag);
						args.add(StringConstant.v("Before decrypt"));
						body.getUnits().insertBefore(Jimple.v().newInvokeStmt(
								Jimple.v().newStaticInvokeExpr(smLogI.makeRef(), args)), curUnit);
						
						args = new ArrayList<>();
						args.add(constTag);
						args.add(StringConstant.v("After decrypt"));
						body.getUnits().insertAfter(Jimple.v().newInvokeStmt(
								Jimple.v().newStaticInvokeExpr(smLogI.makeRef(), args)), curUnit);
					}
				}
			}
		}
		
	}

}
