package de.tu_darmstadt.sse.decisionmaker.analysis.symbolicexecution;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import de.tu_darmstadt.sse.decisionmaker.analysis.smartconstantdataextractor.NotYetSupportedException;
import de.tu_darmstadt.sse.decisionmaker.analysis.symbolicexecution.datastructure.SMTAssertStatement;
import de.tu_darmstadt.sse.decisionmaker.analysis.symbolicexecution.datastructure.SMTBinding;
import de.tu_darmstadt.sse.decisionmaker.analysis.symbolicexecution.datastructure.SMTBindingValue;
import de.tu_darmstadt.sse.decisionmaker.analysis.symbolicexecution.datastructure.SMTBooleanEqualsAssignment;
import de.tu_darmstadt.sse.decisionmaker.analysis.symbolicexecution.datastructure.SMTComplexBinaryOperation;
import de.tu_darmstadt.sse.decisionmaker.analysis.symbolicexecution.datastructure.SMTComplexBinaryOperation.SMTComplexBinaryOperator;
import de.tu_darmstadt.sse.decisionmaker.analysis.symbolicexecution.datastructure.SMTConcatMethodCall;
import de.tu_darmstadt.sse.decisionmaker.analysis.symbolicexecution.datastructure.SMTConstantValue;
import de.tu_darmstadt.sse.decisionmaker.analysis.symbolicexecution.datastructure.SMTContainsMethodCall;
import de.tu_darmstadt.sse.decisionmaker.analysis.symbolicexecution.datastructure.SMTIndexOfMethodCall;
import de.tu_darmstadt.sse.decisionmaker.analysis.symbolicexecution.datastructure.SMTLengthMethodCall;
import de.tu_darmstadt.sse.decisionmaker.analysis.symbolicexecution.datastructure.SMTMethodAssignment;
import de.tu_darmstadt.sse.decisionmaker.analysis.symbolicexecution.datastructure.SMTRegexDigitOperation;
import de.tu_darmstadt.sse.decisionmaker.analysis.symbolicexecution.datastructure.SMTSimpleAssignment;
import de.tu_darmstadt.sse.decisionmaker.analysis.symbolicexecution.datastructure.SMTSimpleBinaryOperation;
import de.tu_darmstadt.sse.decisionmaker.analysis.symbolicexecution.datastructure.SMTStartsWithMethodCall;
import de.tu_darmstadt.sse.decisionmaker.analysis.symbolicexecution.datastructure.SMTStatement;
import de.tu_darmstadt.sse.decisionmaker.analysis.symbolicexecution.datastructure.SMTSubstringMethodCall;
import de.tu_darmstadt.sse.decisionmaker.analysis.symbolicexecution.datastructure.SMTUnaryOperation;
import de.tu_darmstadt.sse.decisionmaker.analysis.symbolicexecution.datastructure.SMTUnaryOperation.SMTUnaryOperator;
import de.tu_darmstadt.sse.decisionmaker.analysis.symbolicexecution.datastructure.SMTUnaryOperationAssignment;
import de.tu_darmstadt.sse.decisionmaker.analysis.symbolicexecution.datastructure.SMTValue;
import soot.Local;
import soot.Type;
import soot.Value;
import soot.jimple.AddExpr;
import soot.jimple.AndExpr;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.CastExpr;
import soot.jimple.CmpExpr;
import soot.jimple.CmpgExpr;
import soot.jimple.CmplExpr;
import soot.jimple.DivExpr;
import soot.jimple.DynamicInvokeExpr;
import soot.jimple.EqExpr;
import soot.jimple.ExprSwitch;
import soot.jimple.GeExpr;
import soot.jimple.GtExpr;
import soot.jimple.InstanceOfExpr;
import soot.jimple.IntConstant;
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.LeExpr;
import soot.jimple.LengthExpr;
import soot.jimple.LtExpr;
import soot.jimple.MulExpr;
import soot.jimple.NeExpr;
import soot.jimple.NegExpr;
import soot.jimple.NewArrayExpr;
import soot.jimple.NewExpr;
import soot.jimple.NewMultiArrayExpr;
import soot.jimple.OrExpr;
import soot.jimple.RemExpr;
import soot.jimple.ShlExpr;
import soot.jimple.ShrExpr;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.jimple.SubExpr;
import soot.jimple.UshrExpr;
import soot.jimple.VirtualInvokeExpr;
import soot.jimple.XorExpr;
import soot.jimple.infoflow.data.AccessPath;
import soot.jimple.infoflow.source.data.SourceSinkDefinition;

class JimpleExprVisitorImpl implements ExprSwitch{
	//all source definitions
	private final Set<SourceSinkDefinition> sources;
	private final JimpleStmtVisitorImpl stmtVisitor;
	public SMTBinding result;
	private Stmt currentStatement = null;
	
	public JimpleExprVisitorImpl(Set<SourceSinkDefinition> sources, JimpleStmtVisitorImpl stmtVisitor) {
		this.sources = sources;
		this.stmtVisitor = stmtVisitor;
	}
	

	@Override
	public void caseAddExpr(AddExpr v) {
		Value op1 = v.getOp1();
		Value op2 = v.getOp2();
		
		if(stmtVisitor.hasBindingForValue(op1)) {
			this.result = stmtVisitor.getLatestBindingForValue(op1);
		}else if(stmtVisitor.hasBindingForValue(op2)) {
			this.result = stmtVisitor.getLatestBindingForValue(op2);
		}else
			throw new RuntimeException("This should not happen...");
		
	}

	@Override
	public void caseAndExpr(AndExpr v) {
		throw new RuntimeException("todo");
		
	}

	@Override
	public void caseCmpExpr(CmpExpr v) {
		throw new RuntimeException("todo");
		
	}

	@Override
	public void caseCmpgExpr(CmpgExpr v) {
		throw new RuntimeException("todo");
		
	}

	@Override
	public void caseCmplExpr(CmplExpr v) {
		throw new RuntimeException("todo");
		
	}

	@Override
	public void caseDivExpr(DivExpr v) {
		throw new RuntimeException("todo");
		
	}

	@Override
	public void caseEqExpr(EqExpr v) {
		throw new RuntimeException("todo");
		
	}

	@Override
	public void caseNeExpr(NeExpr v) {
		throw new RuntimeException("todo");
		
	}

	@Override
	public void caseGeExpr(GeExpr v) {
		throw new RuntimeException("todo");
		
	}

	@Override
	public void caseGtExpr(GtExpr v) {
		throw new RuntimeException("todo");
		
	}

	@Override
	public void caseLeExpr(LeExpr v) {
		throw new RuntimeException("todo");
		
	}

	@Override
	public void caseLtExpr(LtExpr v) {
		throw new RuntimeException("todo");
		
	}

	@Override
	public void caseMulExpr(MulExpr v) {
		throw new RuntimeException("todo");
		
	}

	@Override
	public void caseOrExpr(OrExpr v) {
		throw new RuntimeException("todo");
		
	}

	@Override
	public void caseRemExpr(RemExpr v) {
		throw new RuntimeException("todo");
		
	}

	@Override
	public void caseShlExpr(ShlExpr v) {
		throw new RuntimeException("todo");
		
	}

	@Override
	public void caseShrExpr(ShrExpr v) {
		throw new RuntimeException("todo");
		
	}

	@Override
	public void caseUshrExpr(UshrExpr v) {
		throw new RuntimeException("todo");
		
	}

	@Override
	public void caseSubExpr(SubExpr v) {
		throw new RuntimeException("todo");
		
	}

	@Override
	public void caseXorExpr(XorExpr v) {
		throw new RuntimeException("todo");
		
	}

	@Override
	public void caseInterfaceInvokeExpr(InterfaceInvokeExpr v) {
		if(isSourceMethod(v)) {
			StringConstant newSourceValue = StringConstant.v("loggingPoint");
			SMTBinding binding = stmtVisitor.createNewBindingForValue(newSourceValue);
			stmtVisitor.addValueBindingToVariableDeclaration(newSourceValue, binding);				
			//no smt-statement required, just return the binding
			this.result = binding;
			
			// Additionally check whether the source method need special treatment
			if(isExpressionThatNeedsToBeConvertedToSMT(v)) {
				convertSpecialExpressionsToSMT(v, currentStatement);
			}
		}
		else if(isExpressionThatNeedsToBeConvertedToSMT(v)){
			convertSpecialExpressionsToSMT(v, currentStatement);
		}else{
			//just propagate the taint value of previous statement
			Stmt prevStmt = stmtVisitor.getPreviousDataFlowPathElement(currentStatement);
			if(prevStmt == null) 
				throw new RuntimeException("there is no previous statement");
			else{			
				this.result = stmtVisitor.getBindingForTaintedValue(prevStmt);
				if(this.result == null)
					throw new RuntimeException("double check this here");
			}
		}
	}

	@Override
	public void caseSpecialInvokeExpr(SpecialInvokeExpr v) {
		//is the invokeExpr a source method?
		if(isSourceMethod(v)) {
			StringConstant newSourceValue = StringConstant.v("loggingPoint");
			SMTBinding binding = stmtVisitor.createNewBindingForValue(newSourceValue);
			stmtVisitor.addValueBindingToVariableDeclaration(newSourceValue, binding);				
			//no smt-statement required, just return the binding
			this.result = binding;
			
			// Additionally check whether the source method need special treatment
			if(isExpressionThatNeedsToBeConvertedToSMT(v)) {
				convertSpecialExpressionsToSMT(v, currentStatement);
			}
			
		} else {
			if(isStringOperationSupportedBySMT(v))
				convertStringOperationToSMT(v, v.getBase());
			else if(isExpressionThatNeedsToBeConvertedToSMT(v))
				convertSpecialExpressionsToSMT(v, currentStatement);
			else
				convertAPIMethodToSMT(v);
		}
	}

	@Override
	public void caseStaticInvokeExpr(StaticInvokeExpr v) {		
		//just propagate the taint value of previous statement
		Stmt prevStmt = stmtVisitor.getPreviousDataFlowPathElement(currentStatement);		
		if(prevStmt == null) 
			throw new RuntimeException("there is no previous statement");
		else {	
			//create an assignment between the incoming taint-value and the
			//assigned taint-value inside the method
			
			SMTBinding bindingPrevStmt = stmtVisitor.getBindingForTaintedValue(prevStmt);
			//if there is no taint-tracking involved, we do not have to create an SMT formula
			if(bindingPrevStmt == null)
				return;
			
			AccessPath accessPath = stmtVisitor.getCorrectAccessPathForStmt(currentStatement);
			Local identityStmtTaintValue = accessPath.getPlainValue();
			
			SMTBinding bindingCurentStmt = stmtVisitor.getLatestBindingForValue(identityStmtTaintValue);
			if(bindingCurentStmt == null) {
				bindingCurentStmt = stmtVisitor.createNewBindingForValue(identityStmtTaintValue);
				stmtVisitor.addValueBindingToVariableDeclaration(identityStmtTaintValue, bindingCurentStmt);	
			}
			
			if(bindingCurentStmt != bindingPrevStmt) {
				SMTSimpleAssignment simpleAssignForTaintProp = new SMTSimpleAssignment(bindingCurentStmt, new SMTBindingValue(bindingPrevStmt));
				SMTAssertStatement simpleAssignAssert = new SMTAssertStatement(simpleAssignForTaintProp);
				stmtVisitor.addAssertStmtToAllPrograms(simpleAssignAssert);
			}
			
			this.result = bindingCurentStmt;
		}
	}

	@Override
	public void caseDynamicInvokeExpr(DynamicInvokeExpr v) {
//		caseInvokeExpr(v, null);
		throw new RuntimeException("todo");
	}
	
	@Override
	public void caseCastExpr(CastExpr v) {
		//just propagate the taint value of previous statement
		Stmt prevStmt = stmtVisitor.getPreviousDataFlowPathElement(currentStatement);
		if(prevStmt == null)
			throw new RuntimeException("there is no previous statement");
		else{			
			this.result = stmtVisitor.getBindingForTaintedValue(prevStmt);
			if(this.result == null)
				throw new RuntimeException("double check this here");
		}
	}

	@Override
	public void caseInstanceOfExpr(InstanceOfExpr v) {
		throw new RuntimeException("todo");
		
	}

	@Override
	public void caseNewArrayExpr(NewArrayExpr v) {
		throw new RuntimeException("todo");
		
	}

	@Override
	public void caseNewMultiArrayExpr(NewMultiArrayExpr v) {
		throw new RuntimeException("todo");
		
	}

	@Override
	public void caseNewExpr(NewExpr v) {
		throw new RuntimeException("todo");
		
	}

	@Override
	public void caseLengthExpr(LengthExpr v) {
		throw new RuntimeException("todo");
		
	}

	@Override
	public void caseNegExpr(NegExpr v) {
		throw new RuntimeException("todo");
		
	}

	@Override
	public void defaultCase(Object obj) {
		throw new RuntimeException("todo");
		
	}
	
	@Override
	public void caseVirtualInvokeExpr(VirtualInvokeExpr virtualInvokeExpr) {
		//is the invokeExpr a source method?
		if(isSourceMethod(virtualInvokeExpr)) {
			StringConstant newSourceValue = StringConstant.v("loggingPoint");
			SMTBinding binding = stmtVisitor.createNewBindingForValue(newSourceValue);
			stmtVisitor.addValueBindingToVariableDeclaration(newSourceValue, binding);				
			//no smt-statement required, just return the binding
			this.result = binding;
			
			// Additionally check whether the source method need special treatment
			if(isExpressionThatNeedsToBeConvertedToSMT(virtualInvokeExpr)) {
				convertSpecialExpressionsToSMT(virtualInvokeExpr, currentStatement);
			}
			
		} else {
			if(isStringOperationSupportedBySMT(virtualInvokeExpr))
				convertStringOperationToSMT(virtualInvokeExpr, virtualInvokeExpr.getBase());
			else if(isExpressionThatNeedsToBeConvertedToSMT(virtualInvokeExpr))
				convertSpecialExpressionsToSMT(virtualInvokeExpr, currentStatement);
			else
				convertAPIMethodToSMT(virtualInvokeExpr);
		}
	}
		
	
	private void convertStringOperationToSMT(InvokeExpr invokeExpr, Value base) {
		String methodSignature = invokeExpr.getMethod().getSignature();
		
		if(methodSignature.equals("<java.lang.String: java.lang.String substring(int,int)>")
				|| methodSignature.equals("<java.lang.String: java.lang.String substring(int)>")) 
			generateSMTSubstringStmt(invokeExpr, base);
		
		else if(methodSignature.equals("<java.lang.String: boolean equals(java.lang.Object)>")
				|| methodSignature.equals("<java.lang.String: boolean equalsIgnoreCase(java.lang.String)>")
				|| methodSignature.equals("<java.lang.String: boolean matches(java.lang.String)>")) 
			generateSMTEqualStmt(invokeExpr, base);
		
		else if(methodSignature.equals("<java.lang.String: int indexOf(java.lang.String)>")) 
			generateSMTIndexOfStmt(invokeExpr, base);
		
		else if(methodSignature.equals("<java.lang.String: int indexOf(int,int)>"))
			throw new NotYetSupportedException("indexOf(int,int) not supported yet");
		
		else if(methodSignature.equals("<java.lang.String: boolean startsWith(java.lang.String)>")) 
			generateSMTStartsWithStmt(invokeExpr, base);
		
		else if(methodSignature.equals("<java.lang.String: boolean contains(java.lang.CharSequence)>")
				|| methodSignature.equals("<java.lang.String: java.lang.String replaceAll(java.lang.String,java.lang.String)>")) 
			generateSMTContainsStmt(invokeExpr, base);
		
		else if(methodSignature.equals("<java.lang.String: java.lang.String[] split(java.lang.String)>")) 
			generateSMTSplitStmt(invokeExpr, base);
//			stmtVisitor.notSupported = true;
		else if(methodSignature.equals("<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>"))
			generateSMTAppendStmt(invokeExpr, base);
		else {
			throw new RuntimeException("todo");
		}
	}
	
	
	private void generateSMTSubstringStmt(InvokeExpr invokeExpr, Value base) {
		//############## a.substring(b,c) and a.substring(b) treatment ##############
		//(= t (substring a b (Length a)) )
				
		//treatment of lhs
		SMTBinding lhs = stmtVisitor.createTemporalBinding(SMTBinding.TYPE.String);			
		
		//base treatment
		SMTBinding baseBinding = null;
		if(stmtVisitor.hasBindingForValue(base))
			baseBinding = stmtVisitor.getLatestBindingForValue(base);
		else {
			baseBinding = stmtVisitor.createNewBindingForValue(base);
			stmtVisitor.addValueBindingToVariableDeclaration(base, baseBinding);
		}
		SMTBindingValue stringValue = new SMTBindingValue(baseBinding);
					
		//treatement of rhs		
		//treatment of first argument		
		Value fromIndex = invokeExpr.getArg(0);
		SMTValue fromValue = null;
		if(fromIndex instanceof IntConstant) {
			IntConstant constant = (IntConstant)fromIndex;				
			fromValue = new SMTConstantValue<Integer>(constant.value);												
		}
		else if(stmtVisitor.hasBindingForValue(fromIndex)){
			SMTBinding from = stmtVisitor.getLatestBindingForValue(fromIndex);
			fromValue = new SMTBindingValue(from);
			
		} else{
			System.err.println("###### Doulbe-Check this here... #######");
			SMTBinding tmpBinding = stmtVisitor.createNewBindingForValue(fromIndex);
			stmtVisitor.addValueBindingToVariableDeclaration(fromIndex, tmpBinding);
			fromValue = new SMTBindingValue(tmpBinding);
		}
		
		SMTValue toValue = null;		
		//in case we do only have a startsAt argument and the endAt is the length of the string: 
		if(invokeExpr.getArgCount() == 1) {
			//to treatment
			//first: (assert (= length (Length a) ))
			SMTLengthMethodCall length = new SMTLengthMethodCall(new SMTBindingValue(baseBinding));
			SMTBinding tmpBinding = stmtVisitor.createTemporalBinding(SMTBinding.TYPE.Int);
			SMTMethodAssignment lengthMethodAssignment = new SMTMethodAssignment(tmpBinding, length);
			//second: (assert (= sublength (- length b )  ) )
			SMTUnaryOperation unaryOperation = new SMTUnaryOperation(SMTUnaryOperator.Minus, new SMTBindingValue(tmpBinding), fromValue);
			SMTBinding lengthBindingInt = stmtVisitor.createTemporalBinding(SMTBinding.TYPE.Int);
			SMTUnaryOperationAssignment unaryAssignment = new SMTUnaryOperationAssignment(lengthBindingInt, unaryOperation);
			SMTAssertStatement unaryOperationAssert = new SMTAssertStatement(unaryAssignment);
			
			SMTAssertStatement lengthAssert = new SMTAssertStatement(lengthMethodAssignment);
			stmtVisitor.addAssertStmtToAllPrograms(lengthAssert);			
			stmtVisitor.addAssertStmtToAllPrograms(unaryOperationAssert);
			
			toValue = new SMTBindingValue(lengthBindingInt);
		}
		//in case we do have a startsAt argument AND the endAt argument:
		else if(invokeExpr.getArgCount() == 2) {
			Value toIndex = invokeExpr.getArg(1);			
			if(toIndex instanceof IntConstant) {
				IntConstant toConstant = (IntConstant)toIndex;	
				//SMT solver has length as second argument not to index
				if(fromIndex instanceof IntConstant) {
					IntConstant tmp = (IntConstant)fromIndex;	
					int fromConstant = tmp.value;
					int substrLength = toConstant.value - fromConstant;
					toValue = new SMTConstantValue<Integer>(substrLength);
				}
				else {
					SMTBinding to = stmtVisitor.getLatestBindingForValue(toIndex);
					toValue = new SMTBindingValue(to);	
				}	
			}
			else if(stmtVisitor.hasBindingForValue(toIndex)){
				SMTBinding to = stmtVisitor.getLatestBindingForValue(toIndex);
				toValue = new SMTBindingValue(to);				
			}
			else {
				//to treatment
				//first: (assert (= length (Length a) ))
				SMTLengthMethodCall length = new SMTLengthMethodCall(new SMTBindingValue(baseBinding));
				SMTBinding tmpBinding = stmtVisitor.createTemporalBinding(SMTBinding.TYPE.Int);
				SMTMethodAssignment lengthMethodAssignment = new SMTMethodAssignment(tmpBinding, length);
				//second: (assert (= sublength (- length b )  ) )
				SMTUnaryOperation unaryOperation = new SMTUnaryOperation(SMTUnaryOperator.Minus, new SMTBindingValue(tmpBinding), fromValue);
				SMTBinding lengthBindingInt = stmtVisitor.createTemporalBinding(SMTBinding.TYPE.Int);
				SMTUnaryOperationAssignment unaryAssignment = new SMTUnaryOperationAssignment(lengthBindingInt, unaryOperation);
				SMTAssertStatement unaryOperationAssert = new SMTAssertStatement(unaryAssignment);
				
				SMTAssertStatement lengthAssert = new SMTAssertStatement(lengthMethodAssignment);
				stmtVisitor.addAssertStmtToAllPrograms(lengthAssert);
				stmtVisitor.addAssertStmtToAllPrograms(unaryOperationAssert);
				
				toValue = new SMTBindingValue(lengthBindingInt);
				
			}
		}
		
		
		SMTSubstringMethodCall substring = new SMTSubstringMethodCall(stringValue, fromValue, toValue);
		SMTMethodAssignment methodAss = new SMTMethodAssignment(lhs, substring);
		SMTAssertStatement substringAssert = new SMTAssertStatement(methodAss);
		
		stmtVisitor.addAssertStmtToAllPrograms(substringAssert);
		
		this.result = lhs;				
	}
	
	
	private void generateSMTEqualStmt(InvokeExpr invokeExpr, Value base) {
		//############## a.equals(b), a.equalsIgnoreCase(b) and a.matches(b) treatment ##############
		//(= a b)
				
		//treatment of lhs
		SMTBinding lhs = null;		
		if(stmtVisitor.hasBindingForValue(base))
			lhs = stmtVisitor.getLatestBindingForValue(base);
		else {			
			lhs = stmtVisitor.createNewBindingForValue(base);
			//created a new binding => dynamic values are necessary here for improving the result
			if(lhs.getVersion() == 0) {
				stmtVisitor.addNewDynamicValueForBaseObjectToMap(currentStatement, lhs);
			}	
			stmtVisitor.addValueBindingToVariableDeclaration(base, lhs);			
		}
		
		//treatment of rhs
		Value equalsCheck = invokeExpr.getArg(0);
		SMTValue smtArgumentValue = null;
		if(equalsCheck instanceof StringConstant)
			smtArgumentValue = new SMTConstantValue<String>(((StringConstant) equalsCheck).value);
		else {
			//no constant string available; there is maybe a need for dynamic information to improve the result
			SMTBinding tmpBinding = null;
			if(stmtVisitor.hasBindingForValue(equalsCheck))
				tmpBinding = stmtVisitor.getLatestBindingForValue(equalsCheck);
			else {
				tmpBinding = stmtVisitor.createNewBindingForValue(equalsCheck);
				stmtVisitor.addValueBindingToVariableDeclaration(equalsCheck, tmpBinding);
				//created a new binding => dynamic values are necessary here for improving the result
				stmtVisitor.addNewDynamicValueForArgumentToMap(currentStatement, tmpBinding, 0);
			}				
						
			smtArgumentValue = new SMTBindingValue(tmpBinding);
		}
		
		SMTBinding outerLHS = stmtVisitor.createTemporalBinding(SMTBinding.TYPE.Bool);
		SMTBooleanEqualsAssignment booleanEqualsAssignment = new SMTBooleanEqualsAssignment(outerLHS, new SMTBindingValue(lhs), smtArgumentValue);
		SMTAssertStatement booleanEqualsnAssert = new SMTAssertStatement(booleanEqualsAssignment);
		stmtVisitor.addAssertStmtToAllPrograms(booleanEqualsnAssert);
		
		// result is treated in JimpleStmtVisitor
		this.result = outerLHS;						
	}
	
	
	private void generateSMTIndexOfStmt(InvokeExpr invokeExpr, Value base) {
		//############## a.indexOf(b) treatment ##############
		//(= t (Indexof a b)
				
		//lhs treatment
		SMTBinding lhs = stmtVisitor.createTemporalBinding(SMTBinding.TYPE.Int);
		
		//rhs treatment
		Value indexOf = invokeExpr.getArg(0);
		SMTValue argumentValue = null;
		if(indexOf instanceof StringConstant) {
			argumentValue = new SMTConstantValue<String>(((StringConstant)indexOf).value); 
		}
		else {			
			SMTBinding tmpBinding = null;
			if(stmtVisitor.hasBindingForValue(indexOf))
				tmpBinding = stmtVisitor.getLatestBindingForValue(indexOf);
			else {
				tmpBinding = stmtVisitor.createNewBindingForValue(indexOf);
				stmtVisitor.addValueBindingToVariableDeclaration(indexOf, tmpBinding);
				stmtVisitor.addNewDynamicValueForArgumentToMap(currentStatement, tmpBinding, 0);
			}
									
			argumentValue = new SMTBindingValue(tmpBinding);
		}
		
		//base treatment
		SMTBinding baseBinding = null;
		if(stmtVisitor.hasBindingForValue(base))
			baseBinding = stmtVisitor.getLatestBindingForValue(base);
		else {			
			baseBinding = stmtVisitor.createNewBindingForValue(base);
			stmtVisitor.addValueBindingToVariableDeclaration(base, baseBinding);
			stmtVisitor.addNewDynamicValueForBaseObjectToMap(currentStatement, baseBinding);
		}
		
		SMTIndexOfMethodCall indexOfMethod = new SMTIndexOfMethodCall(new SMTBindingValue(baseBinding), argumentValue);
		SMTMethodAssignment methodAssignment = new SMTMethodAssignment(lhs, indexOfMethod);
		SMTAssertStatement assertStmt = new SMTAssertStatement(methodAssignment);
		
		stmtVisitor.addAssertStmtToAllPrograms(assertStmt);
		
		this.result = lhs;				
	}
	
	
	private void generateSMTStartsWithStmt(InvokeExpr invokeExpr, Value base) {
		//############## a.startsWith(b) treatment ##############
		//(= t (StartsWith a b)
				
		//lhs treatment
		SMTBinding lhs = stmtVisitor.createTemporalBinding(SMTBinding.TYPE.Bool);
		
		//rhs treatment
		Value argumentValue = invokeExpr.getArg(0);
		SMTValue argumentSMTForm = null;
		if(argumentValue instanceof StringConstant) {
			argumentSMTForm = new SMTConstantValue<String>(((StringConstant) argumentValue).value); 
		}
		else {			
			SMTBinding tmpBinding = null;
			if(stmtVisitor.hasBindingForValue(argumentValue))
				tmpBinding = stmtVisitor.getLatestBindingForValue(argumentValue);
			else {
				tmpBinding = stmtVisitor.createNewBindingForValue(argumentValue);
				stmtVisitor.addValueBindingToVariableDeclaration(argumentValue, tmpBinding);
				stmtVisitor.addNewDynamicValueForArgumentToMap(currentStatement, tmpBinding, 0);
			}
						
			argumentSMTForm = new SMTBindingValue(tmpBinding);
		}
		
		//base treatment
		SMTBinding baseBinding = null;
		if(stmtVisitor.hasBindingForValue(base))
			baseBinding = stmtVisitor.getLatestBindingForValue(base);
		else {
			baseBinding = stmtVisitor.createNewBindingForValue(base);
			stmtVisitor.addValueBindingToVariableDeclaration(base, baseBinding);
			stmtVisitor.addNewDynamicValueForBaseObjectToMap(currentStatement, baseBinding);			
		}
		
		SMTStartsWithMethodCall startsWithMethod = new SMTStartsWithMethodCall(new SMTBindingValue(baseBinding), argumentSMTForm);
		SMTMethodAssignment methodAss = new SMTMethodAssignment(lhs, startsWithMethod);
		SMTAssertStatement assertStmt = new SMTAssertStatement(methodAss);
		
		stmtVisitor.addAssertStmtToAllPrograms(assertStmt);
		this.result = lhs;				
	}
	
	
	private void generateSMTContainsStmt(InvokeExpr invokeExpr, Value base) {
		//############## a.contains(b), a.replaceAll(b, c) treatment ##############
		//(= t (Contains a b) )				
		SMTBinding lhs = stmtVisitor.createTemporalBinding(SMTBinding.TYPE.Bool);
		
		//rhs treatment
		Value argumentValue = invokeExpr.getArg(0);
		SMTValue argumentSMTForm = null;
		if(argumentValue instanceof StringConstant) {
			argumentSMTForm = new SMTConstantValue<String>(((StringConstant) argumentValue).value);
		}
		else {			
			SMTBinding tmpBinding = null;
			if(stmtVisitor.hasBindingForValue(argumentValue))
				tmpBinding = stmtVisitor.getLatestBindingForValue(argumentValue);
			else {
				tmpBinding = 
				stmtVisitor.createNewBindingForValue(argumentValue);
				stmtVisitor.addValueBindingToVariableDeclaration(argumentValue, tmpBinding);
				stmtVisitor.addNewDynamicValueForArgumentToMap(currentStatement, tmpBinding, 0);
			}
			argumentSMTForm = new SMTBindingValue(tmpBinding);
		}
		
		//base treatment
		SMTBinding baseBinding = null;
		if(stmtVisitor.hasBindingForValue(base))
			baseBinding = stmtVisitor.getLatestBindingForValue(base);
		else {			
			baseBinding = stmtVisitor.createNewBindingForValue(base);
			stmtVisitor.addValueBindingToVariableDeclaration(base, baseBinding);
			stmtVisitor.addNewDynamicValueForBaseObjectToMap(currentStatement, baseBinding);
		}
		
		SMTContainsMethodCall containsMethod = new SMTContainsMethodCall(new SMTBindingValue(baseBinding), argumentSMTForm);
		SMTMethodAssignment methodAss = new SMTMethodAssignment(lhs, containsMethod);
		SMTAssertStatement assertStmt = new SMTAssertStatement(methodAss);
		
		stmtVisitor.addAssertStmtToAllPrograms(assertStmt);			
		this.result = lhs;
	}
	
	
	private void generateSMTAppendStmt(InvokeExpr invokeExpr, Value base) {
		//############## a.append(b) treatment ##############
		//(= t (Concat a b) )
		
		//treatment of lhs
		SMTBinding lhs = stmtVisitor.createTemporalBinding(SMTBinding.TYPE.String);
		
		//base treatment
		SMTBinding baseBinding = null;
		if(stmtVisitor.hasBindingForValue(base))
			baseBinding = stmtVisitor.getLatestBindingForValue(base);
		else {
			baseBinding = stmtVisitor.createNewBindingForValue(base);
			stmtVisitor.addValueBindingToVariableDeclaration(base, baseBinding);
			stmtVisitor.addNewDynamicValueForBaseObjectToMap(currentStatement, baseBinding);
		}
		
		//rhs treatment
		Value argumentValue = invokeExpr.getArg(0);
		SMTValue argumentSMTForm = null;
		if(argumentValue instanceof StringConstant) {
			argumentSMTForm = new SMTConstantValue<String>(((StringConstant) argumentValue).value);
		}
		else {			
			SMTBinding tmpBinding = null;
			if(stmtVisitor.hasBindingForValue(argumentValue))
				tmpBinding = stmtVisitor.getLatestBindingForValue(argumentValue);
			else {
				tmpBinding = stmtVisitor.createNewBindingForValue(argumentValue);
				stmtVisitor.addValueBindingToVariableDeclaration(argumentValue, tmpBinding);
				stmtVisitor.addNewDynamicValueForBaseObjectToMap(currentStatement, tmpBinding);
			}
			argumentSMTForm = new SMTBindingValue(tmpBinding);
		}

		SMTConcatMethodCall concat = new SMTConcatMethodCall(new SMTBindingValue(baseBinding), argumentSMTForm);
		SMTMethodAssignment conacatAss = new SMTMethodAssignment(lhs, concat);
		SMTAssertStatement assertStmt = new SMTAssertStatement(conacatAss);
		stmtVisitor.addAssertStmtToAllPrograms(assertStmt);
		
		this.result = lhs;
	}
	
	
	private void generateSMTSplitStmt(InvokeExpr invokeExpr, Value base) {
		//############## a.split(b) treatment ##############					
		
		//split-element
		String splitter = null;
		if(invokeExpr.getArg(0) instanceof StringConstant) {
			splitter = ((StringConstant)invokeExpr.getArg(0)).value;
		}
		else {
			System.err.println("###### Doulbe-Check this here... #######");			
		}
		
		List<Stmt> currentDataFlow = this.stmtVisitor.getJimpleDataFlowStatements();
		Stmt currentSplitAPIStatement = currentStatement;
		
		//check if we have concrete information for the split API call
		if(this.stmtVisitor.getSplitInfos().contains(currentDataFlow, currentSplitAPIStatement)) {
			List<List<String>> allSplitInfos = this.stmtVisitor.getSplitInfos().get(currentDataFlow, currentSplitAPIStatement);
			//get first element
			Iterator<List<String>> iterator = allSplitInfos.iterator(); 
			if(iterator.hasNext()) {
				List<String> splitElements = iterator.next();
				
				String splitValue = "";
				for(int i = 0; i < splitElements.size(); i++) {
					String splitElem = splitElements.get(i);
					splitValue += splitElem;
					
					if(i < splitElements.size()-1)
						splitValue += splitter;
				}
				
				iterator.remove();
				
				SMTBinding taintedValue = stmtVisitor.getBindingForTaintedValue(currentStatement);
				
				SMTConstantValue splitValueSMT = new SMTConstantValue<String>(splitValue);
				SMTSimpleAssignment simpleAss = new SMTSimpleAssignment(taintedValue, splitValueSMT);
				SMTAssertStatement assertion = new SMTAssertStatement(simpleAss);
				stmtVisitor.addAssertStmtToAllPrograms(assertion);
			}
		}
		else{
			
		}
			
		
//		//base treatment
//		SMTBinding baseBinding = null;
//		if(stmtVisitor.hasBindingForValue(base))
//			baseBinding = stmtVisitor.getLatestBindingForValue(base);
//		else {
//			System.err.println("###### Doulbe-Check this here... #######");
//			baseBinding = stmtVisitor.createNewBindingForValue(base);
//		}
//		
//		Value argumentValue = invokeExpr.getArg(0);
//		SMTValue argumentSMTForm = null;
//		if(argumentValue instanceof StringConstant) {
//			argumentSMTForm = new SMTConstantValue<String>(((StringConstant) argumentValue).value);
//		}
//		else {
//			System.err.println("###### Doulbe-Check this here... #######");
//			SMTBinding tmpBinding = stmtVisitor.createNewBindingForValue(argumentValue);
//			stmtVisitor.addValueBindingToVariableDeclaration(argumentValue, tmpBinding);
//			argumentSMTForm = new SMTBindingValue(tmpBinding);
//		}
//		
//		Value LHS = null;
//		if(currentStatement instanceof AssignStmt){
//			AssignStmt assignment = (AssignStmt) currentStatement;
//			LHS = assignment.getLeftOp();
//		}
//		else
//			throw new RuntimeException("LHS should not be null");
//		
//		SMTBindingValue baseBindingValue = new SMTBindingValue(baseBinding);
//		//FIRST: 	(assert (= tmpString0 (Substring a 0 i0 )) )
//		SMTBinding tmpLength0 = stmtVisitor.createTemporalBinding(SMTBinding.TYPE.Int);
//		SMTBindingValue tmpLengthValue0 = new SMTBindingValue(tmpLength0);
//		SMTSubstringMethodCall sub0 = new SMTSubstringMethodCall(baseBindingValue, new SMTConstantValue<Integer>(0), tmpLengthValue0);
//		SMTBinding subString0 = stmtVisitor.createTemporalBinding(SMTBinding.TYPE.String);
//		//add subString0 to map
//		String arrayElement0 = String.format("%s[0]", LHS.toString());
//		stmtVisitor.addArrayRef(arrayElement0, subString0);
//		
//		SMTMethodAssignment subAssign0 = new SMTMethodAssignment(subString0, sub0);
//		SMTAssertStatement sub0Assert = new SMTAssertStatement(subAssign0);
//		stmtVisitor.addAssertStmtToAllPrograms(sub0Assert);
//		//SECOND: 	(= tmpString2 (CharAt s i1) )
//		//			(assert (= tmpString2 ":" ) )			
//		SMTCharAtMethodCall charAt0 = new SMTCharAtMethodCall(baseBindingValue, tmpLengthValue0);
//		SMTBinding tmpCharAt = stmtVisitor.createTemporalBinding(SMTBinding.TYPE.String);
//		SMTMethodAssignment charAtMethod0 = new SMTMethodAssignment(tmpCharAt, charAt0);
//		SMTAssertStatement charAtAssert0 = new SMTAssertStatement(charAtMethod0);
//		stmtVisitor.addAssertStmtToAllPrograms(charAtAssert0);
//		SMTSimpleAssignment simpleAssignForCharAt = new SMTSimpleAssignment(tmpCharAt, argumentSMTForm);
//		SMTAssertStatement simpleAssignAssert = new SMTAssertStatement(simpleAssignForCharAt);
//		stmtVisitor.addAssertStmtToAllPrograms(simpleAssignAssert);
//		
//		//THIRD: 	(assert (= tmpString1 (Substring a i0 i1 )) )
//		SMTBinding tmpLength1 = stmtVisitor.createTemporalBinding(SMTBinding.TYPE.Int);
//		SMTBindingValue tmpLengthValue1 = new SMTBindingValue(tmpLength1);
//		SMTSubstringMethodCall sub1 = new SMTSubstringMethodCall(baseBindingValue, tmpLengthValue0, tmpLengthValue1);
//		SMTBinding subString1 = stmtVisitor.createTemporalBinding(SMTBinding.TYPE.String);
//		//add subString1 to map
//		String arrayElement1 = String.format("%s[1]", LHS.toString());
//		stmtVisitor.addArrayRef(arrayElement1, subString1);
//		
//		SMTMethodAssignment subAssign1 = new SMTMethodAssignment(subString1, sub1);
//		SMTAssertStatement sub1Assert = new SMTAssertStatement(subAssign1);
//		stmtVisitor.addAssertStmtToAllPrograms(sub1Assert);
//		
//		//FOURTH: (assert (> i1 i0 ) )
//		SMTSimpleBinaryOperation gt = new SMTSimpleBinaryOperation(SMTSimpleBinaryOperator.GT, tmpLengthValue1, tmpLengthValue0);
//		SMTAssertStatement gtAssert = new SMTAssertStatement(gt);
//		stmtVisitor.addAssertStmtToAllPrograms(gtAssert);
//		
//		int maxIndex = findMaxIndexOfArray(invokeExpr);
//		SMTBindingValue tmpLengthValueAtN = tmpLengthValue1;
//		for(int i = 2; i <= maxIndex; i++) {
//			
//			//FIFTH: 	(assert (= tmpStringn (Substring a iN iN+1 )) )
//			SMTBinding tmpLengthNPLUS1 = stmtVisitor.createTemporalBinding(SMTBinding.TYPE.Int);
//			SMTBindingValue tmpLengthValueNPLUS1 = new SMTBindingValue(tmpLengthNPLUS1);
//			SMTSubstringMethodCall subN = new SMTSubstringMethodCall(baseBindingValue, tmpLengthValueAtN, tmpLengthValueNPLUS1);
//			SMTBinding subStringN = stmtVisitor.createTemporalBinding(SMTBinding.TYPE.String);
//			
//			//add subStringN to map
//			String arrayElementN = String.format("%s[%d]", LHS.toString(), i);
//			stmtVisitor.addArrayRef(arrayElementN, subStringN);
//			
//			SMTMethodAssignment subAssignN = new SMTMethodAssignment(subStringN, subN);
//			SMTAssertStatement subNAssert = new SMTAssertStatement(subAssignN);
//			stmtVisitor.addAssertStmtToAllPrograms(subNAssert);
//			tmpLengthValueAtN = tmpLengthValueNPLUS1;
//			
//			//SIXTH: 	(= tmpStringn (CharAt s i) )
//			//			(assert (= tmpStringn ":" ) )					
//			SMTBinding tmpLengthI = stmtVisitor.createTemporalBinding(SMTBinding.TYPE.Int);
//			SMTBindingValue tmpLengthValueI = new SMTBindingValue(tmpLengthI);				
//			SMTCharAtMethodCall charAtI = new SMTCharAtMethodCall(baseBindingValue, tmpLengthValueI);
//			SMTBinding tmpStringn = stmtVisitor.createTemporalBinding(SMTBinding.TYPE.String);
//			SMTMethodAssignment charAtMethodI = new SMTMethodAssignment(tmpStringn, charAtI);
//			SMTAssertStatement charAtAssertI = new SMTAssertStatement(charAtMethodI);
//			stmtVisitor.addAssertStmtToAllPrograms(charAtAssertI);
//			
//			SMTSimpleAssignment simpleAssignForCharAtI = new SMTSimpleAssignment(tmpStringn, argumentSMTForm);
//			SMTAssertStatement simpleAssignAssertI = new SMTAssertStatement(simpleAssig	nForCharAtI);
//			stmtVisitor.addAssertStmtToAllPrograms(simpleAssignAssertI);					
//		}
		
		this.result = null;
	}
		
	
	
	private int findMaxIndexOfArray(InvokeExpr invokeExpr) {
		Value array = null;
		int maxIndex = -1;
		for(Stmt stmt : stmtVisitor.getJimpleDataFlowStatements()) {
			if(stmt instanceof AssignStmt) {
				AssignStmt assign = (AssignStmt)stmt;
				if(array == null) {
					if(assign.getRightOp().equals(invokeExpr)) {
						array = assign.getLeftOp();
					}
				}
				else{
					Value rhs = assign.getRightOp();
					if(rhs instanceof ArrayRef) {
						ArrayRef arrayRef = (ArrayRef)rhs;
						if(arrayRef.getBase().equals(array)) {
							Value index = arrayRef.getIndex();
							if(index instanceof IntConstant) {
								IntConstant constant = (IntConstant)index;
								maxIndex = constant.value;
							}
						}
					}
				}
			}
		}
		return maxIndex;
	}
	
	private void convertAPIMethodToSMT(InvokeExpr invokeExpr) {
		//lhs treatment
		Type returnType = invokeExpr.getMethod().getReturnType();
		SMTBinding.TYPE bindingType = stmtVisitor.createBindingType(returnType);
		SMTBinding lhs = stmtVisitor.createTemporalBinding(bindingType);
		
		//rhs treatment		
		//just propagate the taint value of previous statement
		Stmt prevStmt = stmtVisitor.getPreviousDataFlowPathElement(currentStatement);
		if(prevStmt == null)
			throw new RuntimeException("there is no previous statement");
				
//		SMTBinding bindingPreviousStmt = stmtVisitor.getBindingForTaintedValue(prevStmt);
		
		//it can be the case that the binding for the 
		
		SMTBinding bindingPreviousStmt = stmtVisitor.getLHSOfLastAssertStmt();
		
		
		
		if(bindingPreviousStmt.getType() != lhs.getType()) {
			SMTBinding tmpBinding = stmtVisitor.createTemporalBinding(lhs.getType());
			SMTSimpleAssignment simpleAss = new SMTSimpleAssignment(lhs, new SMTBindingValue(tmpBinding));
			SMTAssertStatement assertStmt = new SMTAssertStatement(simpleAss);
			stmtVisitor.addAssertStmtToAllPrograms(assertStmt);
			
			this.result = lhs;
		}
		else if(bindingPreviousStmt != null) {		
			SMTSimpleAssignment simpleAss = new SMTSimpleAssignment(lhs, new SMTBindingValue(bindingPreviousStmt));
			SMTAssertStatement assertStmt = new SMTAssertStatement(simpleAss);
			stmtVisitor.addAssertStmtToAllPrograms(assertStmt);
			
			this.result = lhs;
		}
		else
			throw new RuntimeException("previous stmt should be an AssignStmt!");		
	}
	
	private boolean isSourceMethod(InvokeExpr invoke) {
		for(SourceSinkDefinition source : sources) {
			String sourceMethodSign = source.getMethod().getSignature();
			if(invoke.getMethod().getSignature().equals(sourceMethodSign))
				return true;
		}
		return false;
	}
	
	private boolean isStringOperationSupportedBySMT(InvokeExpr invokeExpr) {
		String methodSignature = invokeExpr.getMethod().getSignature();
		if(methodSignature.equals("<java.lang.String: java.lang.String substring(int,int)>")
			|| methodSignature.equals("<java.lang.String: java.lang.String substring(int)>")	
			|| methodSignature.equals("<java.lang.String: boolean equals(java.lang.Object)>")	
			|| methodSignature.equals("<java.lang.String: boolean equalsIgnoreCase(java.lang.String)>")	
			|| methodSignature.equals("<java.lang.String: int indexOf(java.lang.String)>")	
			|| methodSignature.equals("<java.lang.String: int indexOf(int,int)>")	
			|| methodSignature.equals("<java.lang.String: boolean startsWith(java.lang.String)>")	
			|| methodSignature.equals("<java.lang.String: boolean matches(java.lang.String)>")	
			|| methodSignature.equals("<java.lang.String: java.lang.String replaceAll(java.lang.String,java.lang.String)>")	
			|| methodSignature.equals("<java.lang.String: boolean contains(java.lang.CharSequence)>")	
			|| methodSignature.equals("<java.lang.String: java.lang.String[] split(java.lang.String)>")	
			|| methodSignature.equals("<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>")	
				)
			return true;
		return false;
	}


	public void setCurrentStatement(Stmt currentStatement) {
		this.currentStatement = currentStatement;
	}	
	
	
	public boolean isExpressionThatNeedsToBeConvertedToSMT(InvokeExpr invokeExpr) {
		String methodSignature = invokeExpr.getMethod().getSignature();
		if(methodSignature.equals("<java.lang.Integer: int parseInt(java.lang.String)>")
				|| methodSignature.equals("<org.apache.http.client.methods.HttpGet: void <init>(java.lang.String)>")
				|| methodSignature.equals("<java.net.URL: void <init>(java.lang.String)>")
				|| methodSignature.equals("<android.telephony.SmsManager: void sendTextMessage(java.lang.String,java.lang.String,java.lang.String,android.app.PendingIntent,android.app.PendingIntent)>")
				|| methodSignature.equals("<android.telephony.gsm.SmsManager: void sendTextMessage(java.lang.String,java.lang.String,java.lang.String,android.app.PendingIntent,android.app.PendingIntent)>")
				|| methodSignature.equals("<android.telephony.SmsMessage: java.lang.String getDisplayOriginatingAddress()>")
				|| methodSignature.equals("<java.util.Map: java.lang.Object put(java.lang.Object,java.lang.Object)>")
				|| methodSignature.equals("<java.util.Map: java.lang.Object get(java.lang.Object)>")
				|| methodSignature.equals("<android.telephony.TelephonyManager: java.lang.String getNetworkOperator()>")
				|| methodSignature.equals("<android.telephony.TelephonyManager: java.lang.String getSimOperator()>")
			)
			return true;
		return false;
	}
	
	public void convertSpecialExpressionsToSMT(InvokeExpr invokeExpr, Stmt currentStmt) {
		String methodSignature = invokeExpr.getMethod().getSignature();
		
		//###### sendTextMessage ####
		if(methodSignature.equals("<android.telephony.SmsManager: void sendTextMessage(java.lang.String,java.lang.String,java.lang.String,android.app.PendingIntent,android.app.PendingIntent)>")
				|| methodSignature.equals("<android.telephony.gsm.SmsManager: void sendTextMessage(java.lang.String,java.lang.String,java.lang.String,android.app.PendingIntent,android.app.PendingIntent)>")
				){
			SMTBinding taintBinding = stmtVisitor.getBindingForTaintedValue(currentStmt);			
			convertSendTextMessage(taintBinding, invokeExpr);
		}	
		//###### Util.maps #######
		else if(methodSignature.startsWith("<java.util.Map:")) {
			convertUtilMaps(methodSignature, currentStmt);
		}
		//###### getDisplayOriginatingAddress() #######
		else if(methodSignature.equals("<android.telephony.SmsMessage: java.lang.String getDisplayOriginatingAddress()>")) {
			convertSMSSenderNumber(currentStmt);
		}	
		else if(methodSignature.equals("<android.telephony.TelephonyManager: java.lang.String getNetworkOperator()>")) {
			SMTBinding taintBinding = stmtVisitor.getBindingForTaintedValue(currentStmt);
			convertNetworkOperator(taintBinding, currentStmt);
		}
		else if(methodSignature.equals("<android.telephony.TelephonyManager: java.lang.String getSimOperator()>")) {
			SMTBinding taintBinding = stmtVisitor.getBindingForTaintedValue(currentStmt);
			convertSimOperator(taintBinding, currentStmt);
		}
		else
			throw new RuntimeException("todo");
	}
	
	
	private void convertUtilMaps(String methodSignature, Stmt currentStmt) {
		if(methodSignature.equals("<java.util.Map: java.lang.Object put(java.lang.Object,java.lang.Object)>")) {
			Stmt prevStmt = stmtVisitor.getPreviousDataFlowPathElement(currentStmt);	
			SMTBinding rhs = stmtVisitor.getBindingForTaintedValue(prevStmt);
			AccessPath apCurrent = stmtVisitor.getCorrectAccessPathForStmt(currentStmt);
			
			//we do not support Maps right now. Therefore, we take the type of the incoming taint (previous statement)
			SMTBinding.TYPE bindingType = rhs.getType();
			String localName = apCurrent.getPlainValue().getName();
			SMTBinding lhs = new SMTBinding(localName, bindingType, 0);
			stmtVisitor.addValueBindingToVariableDeclaration(apCurrent.getPlainValue(), lhs);
			
			
			SMTSimpleAssignment simpleAss = new SMTSimpleAssignment(lhs, new SMTBindingValue(rhs));
			SMTAssertStatement assertStmt = new SMTAssertStatement(simpleAss);
			stmtVisitor.addAssertStmtToAllPrograms(assertStmt);	
			
			this.result = lhs;
		}
		//we take the previous taint-value
		else if(methodSignature.equals("<java.util.Map: java.lang.Object get(java.lang.Object)>")) {
			Stmt prevStmt = stmtVisitor.getPreviousDataFlowPathElement(currentStmt);	
			SMTBinding rhs = stmtVisitor.getBindingForTaintedValue(prevStmt);
			this.result = rhs;
		}
	}
	
	
	private void convertSendTextMessage(SMTBinding taintBinding, InvokeExpr invokeExpr) {
		if(taintBinding != null){				
			//sms number: we know that the length should be at least 4 and the characters are digits
			Value smsNr = invokeExpr.getArg(0);
			if(smsNr.toString().equals(taintBinding.getVariableName())) {					
				SMTLengthMethodCall length = new SMTLengthMethodCall(new SMTBindingValue(taintBinding));
				SMTBinding tmpBinding = stmtVisitor.createTemporalBinding(SMTBinding.TYPE.Int);
				SMTMethodAssignment lengthMethodAssignment = new SMTMethodAssignment(tmpBinding, length);
				SMTAssertStatement lengthMethodAssert = new SMTAssertStatement(lengthMethodAssignment);
				stmtVisitor.addAssertStmtToAllPrograms(lengthMethodAssert);
				// (assert (> int 4 ) )
				SMTValue valueThreeBinding = new SMTConstantValue<Integer>(4);
				SMTSimpleBinaryOperation gtBinaryOperation = new SMTSimpleBinaryOperation(SMTSimpleBinaryOperation.SMTSimpleBinaryOperator.GT, new SMTBindingValue(tmpBinding), valueThreeBinding);
				SMTAssertStatement gtBinaryAssertion = new SMTAssertStatement(gtBinaryOperation);
				stmtVisitor.addAssertStmtToAllPrograms(gtBinaryAssertion);
				
				//second: (assert (RegexIn a (RegexStar (RegexDigit "") ) ) )
				SMTRegexDigitOperation isDigitOperation = new SMTRegexDigitOperation(taintBinding);
				SMTAssertStatement isDigitAssert = new SMTAssertStatement(isDigitOperation);
				//Todo: temporarily disabled this one due to performance reasons; please enable it!!
				stmtVisitor.addAssertStmtToAllPrograms(isDigitAssert);
			}
			//there is no return value
			this.result = null;
		}
		else
			throw new RuntimeException("it should be an assignment!");
	}
	
	private void convertSMSSenderNumber(Stmt currentStmt) {
		//Returns the originating address, or email from address if this message was from an email gateway. Returns null if originating address unavailable.
		SMTBinding taintedValue = stmtVisitor.getBindingForTaintedValue(currentStmt);
		//FIRST: (assert (= s0 "+" ) )
		SMTBinding s0 = stmtVisitor.createTemporalBinding(SMTBinding.TYPE.String);
		SMTSimpleAssignment plusAssign = new SMTSimpleAssignment(s0, new SMTConstantValue<String>("+"));
		SMTAssertStatement plusAssertion = new SMTAssertStatement(plusAssign);
		stmtVisitor.addAssertStmtToAllPrograms(plusAssertion);
		
		//SECOND: (assert ( RegexIn s1 ( RegexStar ( RegexDigit "" ) ) ))
		SMTBinding s1 = stmtVisitor.createTemporalBinding(SMTBinding.TYPE.String);
		SMTRegexDigitOperation digitOp = new SMTRegexDigitOperation(s1);
		SMTAssertStatement digitAssert = new SMTAssertStatement(digitOp);
		stmtVisitor.addAssertStmtToAllPrograms(digitAssert);
		
		//THIRD: (assert (or (= s2 (Concat s0 s1) ) (= s2 s1)) )
		SMTConcatMethodCall concat = new SMTConcatMethodCall(new SMTBindingValue(s0), new SMTBindingValue(s1));
		SMTMethodAssignment conacatAss = new SMTMethodAssignment(taintedValue, concat);
		SMTSimpleAssignment simpleAss = new SMTSimpleAssignment(taintedValue, new SMTBindingValue(s1));
		Set<SMTStatement> allOrStmts = new HashSet<SMTStatement>();
		allOrStmts.add(conacatAss);
		allOrStmts.add(simpleAss);
		SMTComplexBinaryOperation orBinaryOp = new SMTComplexBinaryOperation(SMTComplexBinaryOperator.OR, allOrStmts);
		SMTAssertStatement orAssertion = new SMTAssertStatement(orBinaryOp);
		stmtVisitor.addAssertStmtToAllPrograms(orAssertion);
		
		//ToDo: email-address			
	}
	
	
	private void convertNetworkOperator(SMTBinding taintBinding, Stmt currentStmt) {
		if(taintBinding != null){								
			SMTLengthMethodCall length = new SMTLengthMethodCall(new SMTBindingValue(taintBinding));
			SMTBinding tmpBinding = stmtVisitor.createTemporalBinding(SMTBinding.TYPE.Int);
			SMTMethodAssignment lengthMethodAssignment = new SMTMethodAssignment(tmpBinding, length);
			SMTAssertStatement lengthMethodAssert = new SMTAssertStatement(lengthMethodAssignment);
			stmtVisitor.addAssertStmtToAllPrograms(lengthMethodAssert);
			
//			// (assert (or (= int 5 ) (= int 6))
//			SMTValue valueBinding5 = new SMTConstantValue<Integer>(5);
//			SMTSimpleAssignment simpleAss5 = new SMTSimpleAssignment(tmpBinding, valueBinding5);
//			SMTValue valueBinding6 = new SMTConstantValue<Integer>(6);
//			SMTSimpleAssignment simpleAss6 = new SMTSimpleAssignment(tmpBinding, valueBinding6);
//			Set<SMTStatement> allOrStmts = new HashSet<SMTStatement>();
//			allOrStmts.add(simpleAss5);
//			allOrStmts.add(simpleAss6);
//			SMTComplexBinaryOperation orBinaryOp = new SMTComplexBinaryOperation(SMTComplexBinaryOperator.OR, allOrStmts);
//			SMTAssertStatement orAssertion = new SMTAssertStatement(orBinaryOp);
//			stmtVisitor.addAssertStmtToAllPrograms(orAssertion);
			
			SMTValue valueBinding5 = new SMTConstantValue<Integer>(5);
			SMTSimpleAssignment simpleAss5 = new SMTSimpleAssignment(tmpBinding, valueBinding5);
			SMTAssertStatement assertStmt = new SMTAssertStatement(simpleAss5);
			stmtVisitor.addAssertStmtToAllPrograms(assertStmt);
		}
		else
			throw new RuntimeException("it should be an assignment!");
	}
	
	private void convertSimOperator(SMTBinding taintBinding, Stmt currentStmt) {
		if(taintBinding != null){								
			SMTLengthMethodCall length = new SMTLengthMethodCall(new SMTBindingValue(taintBinding));
			SMTBinding tmpBinding = stmtVisitor.createTemporalBinding(SMTBinding.TYPE.Int);
			SMTMethodAssignment lengthMethodAssignment = new SMTMethodAssignment(tmpBinding, length);
			SMTAssertStatement lengthMethodAssert = new SMTAssertStatement(lengthMethodAssignment);
			stmtVisitor.addAssertStmtToAllPrograms(lengthMethodAssert);
			
//			// (assert (or (= int 5 ) (= int 6))
//			SMTValue valueBinding5 = new SMTConstantValue<Integer>(5);
//			SMTSimpleAssignment simpleAss5 = new SMTSimpleAssignment(tmpBinding, valueBinding5);
//			SMTValue valueBinding6 = new SMTConstantValue<Integer>(6);
//			SMTSimpleAssignment simpleAss6 = new SMTSimpleAssignment(tmpBinding, valueBinding6);
//			Set<SMTStatement> allOrStmts = new HashSet<SMTStatement>();
//			allOrStmts.add(simpleAss5);
//			allOrStmts.add(simpleAss6);
//			SMTComplexBinaryOperation orBinaryOp = new SMTComplexBinaryOperation(SMTComplexBinaryOperator.OR, allOrStmts);
//			SMTAssertStatement orAssertion = new SMTAssertStatement(orBinaryOp);
//			stmtVisitor.addAssertStmtToAllPrograms(orAssertion);
			
			SMTValue valueBinding5 = new SMTConstantValue<Integer>(5);
			SMTSimpleAssignment simpleAss5 = new SMTSimpleAssignment(tmpBinding, valueBinding5);
			SMTAssertStatement assertStmt = new SMTAssertStatement(simpleAss5);
			stmtVisitor.addAssertStmtToAllPrograms(assertStmt);					
		}
		else
			throw new RuntimeException("it should be an assignment!");
	}
}
