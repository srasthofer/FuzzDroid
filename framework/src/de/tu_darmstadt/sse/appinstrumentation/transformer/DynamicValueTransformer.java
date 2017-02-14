package de.tu_darmstadt.sse.appinstrumentation.transformer;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.Body;
import soot.IntType;
import soot.Local;
import soot.RefType;
import soot.Scene;
import soot.SootMethodRef;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.VoidType;
import soot.javaToJimple.LocalGenerator;
import soot.jimple.AssignStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.Jimple;
import soot.jimple.NewExpr;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import de.tu_darmstadt.sse.appinstrumentation.UtilInstrumenter;
import de.tu_darmstadt.sse.dynamiccfg.utils.FileUtils;


public class DynamicValueTransformer extends AbstractInstrumentationTransformer {

	private boolean instrumentOnlyComparisons = true;
	private final static String DYNAMIC_VALUES_FILENAME = "." + File.separator + "files" + File.separator + "dynamicValueMethods.txt";
	private static Set<String> comparisonSignatures = FileUtils.textFileToLineSet(DYNAMIC_VALUES_FILENAME);	

	private final SootMethodRef refString;
	private final SootMethodRef refInt;
	
	
	public DynamicValueTransformer(boolean instrumentOnlyComparisons) {
		this.instrumentOnlyComparisons = instrumentOnlyComparisons;
		
		// Make references to the tracker methods
		RefType stringType = RefType.v("java.lang.String");		
		{
			List<Type> paramTypeList = new ArrayList<>();
			paramTypeList.add(stringType);
			paramTypeList.add(IntType.v());		
			refString = Scene.v().makeMethodRef(
					Scene.v().getSootClass(UtilInstrumenter.JAVA_CLASS_FOR_CODE_POSITIONS),
					"reportDynamicValue",
					paramTypeList,
					VoidType.v(),
					true);
		}
		{
			List<Type> paramTypeList = new ArrayList<>();
			paramTypeList.add(IntType.v());
			paramTypeList.add(IntType.v());		
			refInt = Scene.v().makeMethodRef(
					Scene.v().getSootClass(UtilInstrumenter.JAVA_CLASS_FOR_CODE_POSITIONS),
					"reportDynamicValue",
					paramTypeList,
					VoidType.v(),
					true);
		}
	}
	
	@Override
	protected void internalTransform(Body b, String phaseName,
			Map<String, String> options) {
		// Do not instrument methods in framework classes
		if (!canInstrumentMethod(b.getMethod()))
			return;
		
		// Iterate over all statements. For each definition statement that
		// defines a string, report the string to the server.
		for (Iterator<Unit> unitIt = b.getUnits().snapshotIterator(); unitIt.hasNext(); ) {
			Unit curUnit = unitIt.next();
			
			// If we're still inside the IdentityStmt block, there's nothing to
			// instrument
			if (curUnit instanceof IdentityStmt ||
					// If this unit was instrumented by another transformer, there's nothing to instrument
					curUnit.hasTag(InstrumentedCodeTag.name))
				continue;			
			
			if (instrumentOnlyComparisons) {
				// Is this a comparison?
				Stmt curStmt = (Stmt) curUnit;
				if (!curStmt.containsInvokeExpr())
					continue;
				InvokeExpr invExpr = curStmt.getInvokeExpr();
				if (comparisonSignatures.contains(invExpr.getMethod().getSignature())) {					
					if (invExpr instanceof InstanceInvokeExpr)
						checkAndReport(b, curStmt, ((InstanceInvokeExpr) invExpr).getBase(), -1);
					for (int i = 0; i < invExpr.getArgCount(); i++)
						checkAndReport(b, curStmt, invExpr.getArg(i), i);
				}
				
				// Do not look for anything else
				continue;
			}
			
			// We only care about statements that define strings
			if (!(curUnit instanceof AssignStmt))
				continue;
			AssignStmt assignStmt = (AssignStmt) curUnit;
			checkAndReport(b, assignStmt, assignStmt.getLeftOp(), -1);
		}

	}

	private void checkAndReport(Body b, Stmt curStmt, Value value, int paramIdx) {
		LocalGenerator localGenerator = new LocalGenerator(b);
		RefType stringType = RefType.v("java.lang.String");
		Value lhs = value;
		
		if(lhs instanceof StringConstant)
			return;
		else if(lhs instanceof IntConstant)
			return;
		
		// If this is a CharSequence, we need to convert it into a string
		if (lhs.getType() == RefType.v("java.lang.CharSequence") ||
				lhs.getType() == RefType.v("java.lang.StringBuilder") && lhs instanceof Local) {
			SootMethodRef toStringRef = Scene.v().getMethod("<java.lang.Object: "
					+ "java.lang.String toString()>").makeRef();
			Local stringLocal = localGenerator.generateLocal(stringType);
			Stmt stringAssignStmt = Jimple.v().newAssignStmt(stringLocal,
					Jimple.v().newVirtualInvokeExpr((Local) lhs, toStringRef));
			stringAssignStmt.addTag(new InstrumentedCodeTag());
			
			b.getUnits().insertBefore(stringAssignStmt, curStmt);
			lhs = stringLocal;
		}
		else if (lhs.getType() != IntType.v() && lhs.getType() != stringType)
			return;
		
		//new String() case
		if (value instanceof NewExpr)
			return;
		
		// Depending on the type of the value, we might need an intermediate local
		if (!(lhs instanceof Local)) {
			Local newLhs = localGenerator.generateLocal(lhs.getType());
			AssignStmt assignLocalStmt = Jimple.v().newAssignStmt(newLhs, lhs);
			assignLocalStmt.addTag(new InstrumentedCodeTag());
			b.getUnits().insertBefore(assignLocalStmt, curStmt);
			lhs = newLhs;
		}
		
		// Report the value
		Stmt reportValueStmt;
		if (lhs.getType() == stringType) {
			reportValueStmt = Jimple.v().newInvokeStmt(
					Jimple.v().newStaticInvokeExpr(refString, lhs, IntConstant.v(paramIdx)));
		}
		else if (lhs.getType() == IntType.v()) {
			reportValueStmt = Jimple.v().newInvokeStmt(
					Jimple.v().newStaticInvokeExpr(refInt, lhs, IntConstant.v(paramIdx)));
		}
		else
			return;
		reportValueStmt.addTag(new InstrumentedCodeTag());
		
		b.getUnits().insertBefore(reportValueStmt, curStmt);
	}

}
