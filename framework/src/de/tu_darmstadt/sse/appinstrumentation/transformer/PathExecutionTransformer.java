package de.tu_darmstadt.sse.appinstrumentation.transformer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.Body;
import soot.IdentityUnit;
import soot.RefType;
import soot.Unit;
import soot.Value;
import soot.jimple.DefinitionStmt;
import soot.jimple.IfStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.NullConstant;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import de.tu_darmstadt.sse.appinstrumentation.UtilInstrumenter;
import de.tu_darmstadt.sse.sharedclasses.util.Pair;


public class PathExecutionTransformer extends AbstractInstrumentationTransformer {
	
	private final Set<String> branchTargetStmt = new HashSet<String>();
	
	@Override
	protected void internalTransform(Body body, String phaseName, Map<String, String> options) {
		// Do not instrument methods in framework classes
		if (!canInstrumentMethod(body.getMethod()))
			return;
		
		instrumentInfoAboutNonAPICall(body);
		
		//important to use snapshotIterator here
		Iterator<Unit> iterator = body.getUnits().snapshotIterator();
		while(iterator.hasNext()){
			Unit unit = iterator.next();
			if(unit instanceof ReturnStmt || unit instanceof ReturnVoidStmt)
				instrumentInfoAboutReturnStmt(body, unit);
			else if(unit instanceof DefinitionStmt || unit instanceof InvokeStmt)
				instrumentInfoAboutNonApiCaller(body, unit);
			else if(unit instanceof IfStmt)
				instrumentEachBranchAccess(body, (IfStmt)unit);
		}				
	}
	
	
	private void instrumentInfoAboutNonAPICall(Body body){					
		String methodSignature =  body.getMethod().getSignature();
		Unit generatedJimpleCode = UtilInstrumenter.makeJimpleStaticCallForPathExecution("logInfoAboutNonApiMethodAccess", RefType.v("java.lang.String"), StringConstant.v(methodSignature));
		generatedJimpleCode.addTag(new InstrumentedCodeTag());
		//super-method call has to be the first statement
		if(methodSignature.contains("<init>(") || methodSignature.contains("<clinit>"))
			body.getUnits().insertAfter(generatedJimpleCode, getUnitAfterIdentities(body));
		else
			body.getUnits().insertBefore(generatedJimpleCode, getUnitAfterIdentities(body));
	}
	
	
	private void instrumentInfoAboutReturnStmt(Body body, Unit unit)
	{		
		if(unit instanceof ReturnStmt)
		{
			ReturnStmt returnStmt = (ReturnStmt)unit;
			List<Unit> generated = new ArrayList<Unit>();
			
			String methodSignature =  body.getMethod().getSignature();
			Unit generatedJimpleCode = UtilInstrumenter.makeJimpleStaticCallForPathExecution("logInfoAboutReturnStatement", 
					RefType.v("java.lang.String"), StringConstant.v(methodSignature),
					RefType.v("java.lang.Object"), UtilInstrumenter.generateCorrectObject(body, returnStmt.getOp(), generated));
			generatedJimpleCode.addTag(new InstrumentedCodeTag());
			generated.add(generatedJimpleCode);
			body.getUnits().insertBefore(generated, returnStmt);
		}
		else if(unit instanceof ReturnVoidStmt)
		{
			String methodSignature =  body.getMethod().getSignature();
			Unit generatedJimpleCode = UtilInstrumenter.makeJimpleStaticCallForPathExecution("logInfoAboutReturnStatement", RefType.v("java.lang.String"), StringConstant.v(methodSignature));
			generatedJimpleCode.addTag(new InstrumentedCodeTag());
			body.getUnits().insertBefore(generatedJimpleCode, unit);
		}			
	}
	
	
	private void instrumentInfoAboutNonApiCaller(Body body, Unit unit)
	{	
		//our instrumented code
		if(unit.hasTag(InstrumentedCodeTag.name))
			return;
		
		InvokeExpr invokeExpr = null;
		if(unit instanceof DefinitionStmt)
		{
			DefinitionStmt defStmt = (DefinitionStmt)unit;
			if(defStmt.containsInvokeExpr())
			{
				invokeExpr = defStmt.getInvokeExpr();
			}
		}					
		else if(unit instanceof InvokeStmt)
		{
			InvokeStmt invokeStmt = (InvokeStmt)unit;
			invokeExpr = invokeStmt.getInvokeExpr();
		}				
		
		if(invokeExpr != null)
		{			
			if(!UtilInstrumenter.isApiCall(invokeExpr))
			{
				String invokeExprMethodSignature = invokeExpr.getMethod().getSignature();
				
				List<Value> parameter = invokeExpr.getArgs();
				List<Unit> generated = new ArrayList<Unit>();
				Pair<Value, List<Unit>> arrayRefAndInstrumentation = UtilInstrumenter.generateParameterArray(parameter, body);
				
				List<Unit> generatedArrayInstrumentation = arrayRefAndInstrumentation.getSecond();
				Value arrayRef = arrayRefAndInstrumentation.getFirst();
				
				Unit generatedInvokeStmt = UtilInstrumenter.makeJimpleStaticCallForPathExecution("logInfoAboutNonApiMethodCaller", 	
						RefType.v("java.lang.String"), StringConstant.v(body.getMethod().getSignature()),
						RefType.v("java.lang.String"), StringConstant.v(invokeExprMethodSignature),
						UtilInstrumenter.getParameterArrayType(), (parameter.isEmpty())? NullConstant.v() : arrayRef);
				generatedInvokeStmt.addTag(new InstrumentedCodeTag());
				generated.addAll(generatedArrayInstrumentation);
				generated.add(generatedInvokeStmt);

				body.getUnits().insertBefore(generated, unit);
			}
		}		
	}
	
	
	private void instrumentEachBranchAccess(Body body, IfStmt ifStmt){		
		String methodSignature =  body.getMethod().getSignature();
		String condition = ifStmt.getCondition().toString();		
		Unit generatedJimpleCodeForBranch = UtilInstrumenter.makeJimpleStaticCallForPathExecution("logInfoAboutBranchAccess", 
				RefType.v("java.lang.String"), StringConstant.v(methodSignature),
				RefType.v("java.lang.String"), StringConstant.v(condition),
				RefType.v("java.lang.String"), NullConstant.v()
				);
		generatedJimpleCodeForBranch.addTag(new InstrumentedCodeTag());
		
		Unit generatedJimpleCodeThenBranch = UtilInstrumenter.makeJimpleStaticCallForPathExecution("logInfoAboutBranchAccess", 
				RefType.v("java.lang.String"), StringConstant.v(methodSignature),
				RefType.v("java.lang.String"), NullConstant.v(),
				RefType.v("java.lang.String"), StringConstant.v("then branch")
				);
		generatedJimpleCodeThenBranch.addTag(new InstrumentedCodeTag());
		
		Unit generatedJimpleCodeElseBranch = UtilInstrumenter.makeJimpleStaticCallForPathExecution("logInfoAboutBranchAccess", 
				RefType.v("java.lang.String"), StringConstant.v(methodSignature),
				RefType.v("java.lang.String"), NullConstant.v(),
				RefType.v("java.lang.String"), StringConstant.v("else branch")
				);
		generatedJimpleCodeElseBranch.addTag(new InstrumentedCodeTag());
		
		body.getUnits().insertBefore(generatedJimpleCodeForBranch, ifStmt);
		
		//treatment of target statement ("true"-branch)
		Stmt targetStmt = ifStmt.getTarget();
		if(!branchTargetStmt.contains(targetStmt.toString())) {
			branchTargetStmt.add(generatedJimpleCodeThenBranch.toString());
			body.getUnits().insertBefore(generatedJimpleCodeThenBranch, targetStmt);
		}
		
		//treatment of "else"-branch
		body.getUnits().insertAfter(generatedJimpleCodeElseBranch, ifStmt);
	}
	
	
	private Unit getUnitAfterIdentities(Body body) {
		Unit u = body.getUnits().getFirst();
		while (u instanceof IdentityUnit)
			u = body.getUnits().getSuccOf(u);
		
		return u;
	}

}
