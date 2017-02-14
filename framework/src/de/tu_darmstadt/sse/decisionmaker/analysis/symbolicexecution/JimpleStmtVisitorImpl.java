package de.tu_darmstadt.sse.decisionmaker.analysis.symbolicexecution;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.HashBiMap;
import com.google.common.collect.Table;

import soot.ArrayType;
import soot.Body;
import soot.BooleanType;
import soot.DoubleType;
import soot.IntType;
import soot.Local;
import soot.LongType;
import soot.RefType;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.BreakpointStmt;
import soot.jimple.CaughtExceptionRef;
import soot.jimple.Constant;
import soot.jimple.EnterMonitorStmt;
import soot.jimple.ExitMonitorStmt;
import soot.jimple.Expr;
import soot.jimple.FieldRef;
import soot.jimple.GotoStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.IfStmt;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.LookupSwitchStmt;
import soot.jimple.NopStmt;
import soot.jimple.ParameterRef;
import soot.jimple.RetStmt;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.Stmt;
import soot.jimple.StmtSwitch;
import soot.jimple.StringConstant;
import soot.jimple.TableSwitchStmt;
import soot.jimple.ThisRef;
import soot.jimple.ThrowStmt;
import soot.jimple.infoflow.data.AccessPath;
import soot.jimple.infoflow.results.ResultSourceInfo;
import soot.jimple.infoflow.solver.cfg.IInfoflowCFG;
import soot.jimple.infoflow.source.data.SourceSinkDefinition;
import de.tu_darmstadt.sse.appinstrumentation.UtilInstrumenter;
import de.tu_darmstadt.sse.commandlinelogger.LoggerHelper;
import de.tu_darmstadt.sse.decisionmaker.analysis.dynamicValues.DynamicValueInformation;
import de.tu_darmstadt.sse.decisionmaker.analysis.smartconstantdataextractor.NotYetSupportedException;
import de.tu_darmstadt.sse.decisionmaker.analysis.symbolicexecution.datastructure.SMTAssertStatement;
import de.tu_darmstadt.sse.decisionmaker.analysis.symbolicexecution.datastructure.SMTAssignment;
import de.tu_darmstadt.sse.decisionmaker.analysis.symbolicexecution.datastructure.SMTBinding;
import de.tu_darmstadt.sse.decisionmaker.analysis.symbolicexecution.datastructure.SMTBinding.TYPE;
import de.tu_darmstadt.sse.decisionmaker.analysis.symbolicexecution.datastructure.SMTBindingValue;
import de.tu_darmstadt.sse.decisionmaker.analysis.symbolicexecution.datastructure.SMTConstantValue;
import de.tu_darmstadt.sse.decisionmaker.analysis.symbolicexecution.datastructure.SMTProgram;
import de.tu_darmstadt.sse.decisionmaker.analysis.symbolicexecution.datastructure.SMTSimpleAssignment;
import de.tu_darmstadt.sse.decisionmaker.analysis.symbolicexecution.datastructure.SMTStatement;

public class JimpleStmtVisitorImpl implements StmtSwitch{
	final Set<SMTProgram> smtPrograms;
	SMTProgram tmpSmtProgram = new SMTProgram();
	
	SMTProgram currentSMTProgram;
	
	Map<Value, SMTBinding> globalScopeSSAFormHelper = new HashMap<Value, SMTBinding>();
	
	Map<SootField, SMTBinding> fieldSSAFormHelper = new HashMap<SootField, SMTBinding>();
	
	Map<String, SMTBinding> arrayHelper = new HashMap<String, SMTBinding>();
	
	Map<ThisRef, SMTBinding> thisRefSSAFormHelper = new HashMap<ThisRef, SMTBinding>();
	private final JimpleExprVisitorImpl exprVisitor;
	
	private final List<Stmt> jimpleDataFlowStatements;
	
	private final List<AccessPath> accessPathPath;
	
	private final Set<Unit> targetUnits;
	
	private final IInfoflowCFG cfg;
	
	private Map<SMTProgram, Set<DynamicValueInformation>> dynamicValueInfos = new HashMap<SMTProgram, Set<DynamicValueInformation>>();
	
	private Table<List<Stmt>, Stmt, List<List<String>>> splitAPIElementInfos;

	
	public boolean notSupported = false;
	
	public JimpleStmtVisitorImpl(Set<SourceSinkDefinition> sources,
			List<Stmt> jimpleDataFlowStatements, List<AccessPath> accessPathPath, 
			Set<Unit> targetUnits, IInfoflowCFG cfg, Table<List<Stmt>, Stmt, List<List<String>>> splitAPIElementInfos) {
		this.exprVisitor = new JimpleExprVisitorImpl(sources, this);
		this.jimpleDataFlowStatements = jimpleDataFlowStatements;
		this.accessPathPath = accessPathPath;
		this.targetUnits = targetUnits;
		this.cfg = cfg;
		this.splitAPIElementInfos = splitAPIElementInfos;
		this.smtPrograms = new HashSet<SMTProgram>();
		//initial adding of a single SMTProgram
		currentSMTProgram = new SMTProgram();
		smtPrograms.add(currentSMTProgram);
	}
	
	public Set<SMTProgram> getSMTPrograms() {
		return this.smtPrograms;
	}		

	public Table<List<Stmt>, Stmt, List<List<String>>> getSplitInfos() {
		return splitAPIElementInfos;
	}

	
	public void addValueBindingToVariableDeclaration(Value value, SMTBinding binding) {
		//update current program
		tmpSmtProgram.addVariableDeclaration(binding);
		
		//update all smt-programs
		for(SMTProgram smtProgram : smtPrograms) 
			smtProgram.addVariableDeclaration(binding);
		//global scope is the same for all smt-programs
		this.globalScopeSSAFormHelper.put(value, binding);
	}
	
	
	public void addFieldBindingToVariableDeclaration(SootField field, SMTBinding binding) {
		//update current program
		tmpSmtProgram.addVariableDeclaration(binding);
		
		//update all smt-programs
		for(SMTProgram smtProgram : smtPrograms)
			smtProgram.addVariableDeclaration(binding);
		//field scope is the same for all smt-programs
		this.fieldSSAFormHelper.put(field, binding);
	}
	
	
	public SMTBinding getLatestBindingForValue(Value value) {
		if(hasBindingForValue(value))
			return this.globalScopeSSAFormHelper.get(value);
		else 
			return null;
	}
	
	
	public SMTBinding getLatestBindingForField(SootField field) {
		if(hasBindingForField(field))
			return this.fieldSSAFormHelper.get(field);
		else
			return null;
	}
	
	public boolean hasBindingForValue(Value value) {
		return this.globalScopeSSAFormHelper.containsKey(value);
	}
	
	public boolean hasBindingForField(SootField field) {
		return this.fieldSSAFormHelper.containsKey(field);
	}
	
	public SMTBinding getLatestBindingForThisRef(ThisRef thisRef) {
		if(hasBindingForThisRef(thisRef))
			return thisRefSSAFormHelper.get(thisRef);
		else
			return null;
	}
	
	public boolean hasBindingForThisRef(ThisRef thisRef) {
		return this.thisRefSSAFormHelper.containsKey(thisRef);
	}
	
	
	public SMTBinding createNewBindingForValue(Value value) {
		SMTBinding binding = null;
		if(hasBindingForValue(value)) {
			SMTBinding oldBinding = getLatestBindingForValue(value);
			int ssaVersionOldBinding = oldBinding.getVersion();
			//increment version
			ssaVersionOldBinding += 1;
			binding = new SMTBinding(oldBinding.getVariableName(), oldBinding.getType(), ssaVersionOldBinding);	
			return binding;
		}
		else{
			if(value instanceof Local) {
				Local local = (Local) value;
				SMTBinding.TYPE bindingType = createBindingType(local.getType());
				String localName = local.getName();
				//check if such a local name is already taken
				int countOccurance = 0;
				for(Map.Entry<Value, SMTBinding> entry : globalScopeSSAFormHelper.entrySet()) {
					if(entry.getKey().toString().equals(localName))
						countOccurance += 1;
				}
				if(countOccurance > 0) {
					String tmp = new String(localName);
					for(int i = 0; i < countOccurance; i++)
						localName += tmp;
				}
				binding = new SMTBinding(localName, bindingType, 0);
			}
			else if(value instanceof StringConstant) {
				StringConstant constantString = (StringConstant) value;
				String constantStringValue = constantString.value;
				binding = new SMTBinding(constantStringValue, SMTBinding.TYPE.String, 0);
			}
			
			return binding;
		}
	}
	
	public SMTBinding createNewBindingForThisRef(ThisRef thisRef) {
		SMTBinding binding = null;
		if(hasBindingForThisRef(thisRef)) {
			SMTBinding oldBinding = getLatestBindingForThisRef(thisRef);
			int ssaVersionOldBinding = oldBinding.getVersion();
			//increment version
			ssaVersionOldBinding += 1;
			binding = new SMTBinding(oldBinding.getVariableName(), oldBinding.getType(), ssaVersionOldBinding);	
			return binding;
		}
		else {
			return new SMTBinding(thisRef.getType().toString(), TYPE.String);
		}
	}
	
	
	public void addAssertStmtToAllPrograms(SMTAssertStatement assertStmt) {
		//update current program
		tmpSmtProgram.addAssertStatement(assertStmt);
		
		for(SMTProgram smtProgram : smtPrograms) 
			smtProgram.addAssertStatement(assertStmt);
	}
	
	
	public void addAssertStmtToSingleProgram(SMTProgram smtProgram, SMTAssertStatement assertStmt) {
		smtProgram.addAssertStatement(assertStmt);
	}
	
	
	public void doubleSMTPrograms() {
		Set<SMTProgram> newSMTPrograms = new HashSet<SMTProgram>();
		for(SMTProgram smtProgram : smtPrograms)
			newSMTPrograms.add(smtProgram.clone());
		smtPrograms.addAll(newSMTPrograms);
	}
	
	
	public void addingTrueAndFalseConditionalAssertions(SMTBinding lhs) {
		int i = 0;
		for(SMTProgram smtProgram : smtPrograms) {
			if(i%2 == 0) {				
				SMTSimpleAssignment assignment = new SMTSimpleAssignment(lhs, new SMTConstantValue<Boolean>(false));
				SMTAssertStatement assignAssert = new SMTAssertStatement(assignment);
				addAssertStmtToSingleProgram(smtProgram, assignAssert);
				
				//CAUTION: update current program; in this case, we add a wrong statement
				tmpSmtProgram.addAssertStatement(assignAssert);
			}
			else {
				SMTSimpleAssignment assignment = new SMTSimpleAssignment(lhs, new SMTConstantValue<Boolean>(true));
				SMTAssertStatement assignAssert = new SMTAssertStatement(assignment);
				addAssertStmtToSingleProgram(smtProgram, assignAssert);
				
				//CAUTION: update current program; in this case, we add a wrong statement
				tmpSmtProgram.addAssertStatement(assignAssert);
			}
			i += 1;
		}				
	}
	
	
	public SMTBinding createNewBindingForField(SootField field) {
		SMTBinding binding = null;
		if(hasBindingForField(field)) {
			SMTBinding oldBinding = getLatestBindingForField(field);
			int ssaVersionOldBinding = oldBinding.getVersion();
			//increment version
			ssaVersionOldBinding += 1;
			binding = new SMTBinding(oldBinding.getVariableName(), oldBinding.getType(), ssaVersionOldBinding);	
			return binding;
		}
		else{
			SMTBinding.TYPE bindingType = createBindingType(field.getType());
			String fieldName = field.toString().replace(" ", "_");
			fieldName = fieldName.replace(".", "_");
			fieldName = fieldName.replace(":", "");
			binding = new SMTBinding("FIELD_" + fieldName, bindingType );			
		}
		return binding;
	}
	
	public SMTBinding createTemporalBinding(SMTBinding.TYPE type) {
		String tmpName = null;
		switch(type) {
		case String : tmpName = "StringTMP"; break;
		case Int : tmpName = "IntTMP"; break;
		case Bool : tmpName = "BoolTMP"; break;
		case Real:
			break;
		default:
			break;
		}
		StringConstant tmpValue = StringConstant.v(tmpName);
		
		SMTBinding binding = null;
		if(hasBindingForValue(tmpValue)) {
			SMTBinding oldBinding = getLatestBindingForValue(tmpValue);
			int ssaVersionOldBinding = oldBinding.getVersion();
			//increment version
			ssaVersionOldBinding += 1;
			binding = new SMTBinding(oldBinding.getVariableName(), oldBinding.getType(), ssaVersionOldBinding);	
		}
		else {
			binding = new SMTBinding(tmpName, type, 0);
		}
		addValueBindingToVariableDeclaration(tmpValue, binding);
		return binding;
	}
	
	public List<Stmt> getJimpleDataFlowStatements() {
		return jimpleDataFlowStatements;
	}

	@Override
	public void caseBreakpointStmt(BreakpointStmt stmt) {
		throw new RuntimeException("todo");
		
	}

	@Override
	public void caseInvokeStmt(InvokeStmt stmt) {
		InvokeExpr invokeExpr = stmt.getInvokeExpr();
		SootClass declaringClass = invokeExpr.getMethod().getDeclaringClass();
		if(exprVisitor.isExpressionThatNeedsToBeConvertedToSMT(invokeExpr))
			exprVisitor.convertSpecialExpressionsToSMT(invokeExpr, stmt);
		else if(UtilInstrumenter.isAppDeveloperCode(declaringClass)) {
			SootMethod method = invokeExpr.getMethod();
			Body body = method.retrieveActiveBody();
			
			SMTBinding newRhs = getBindingForTaintedValue(stmt);
			//if there is no taint-tracking involved (newRhs == null), we do not have to do anything here
			if(newRhs == null)
				return;
			
			int indexOfInterest = -1;
			for(int i = 0; i < invokeExpr.getArgCount(); i++) {
				if(newRhs.getVariableName().equals(invokeExpr.getArg(i).toString())) {
					indexOfInterest = i;
					break;
				}
			}
			
			if(indexOfInterest == -1)
				return;
			
			
			for(Unit unit : body.getUnits()) {
				if(unit instanceof IdentityStmt) {
					IdentityStmt identity = (IdentityStmt)unit;
					Value rhs = identity.getRightOp();
					if(rhs instanceof ParameterRef) {
						ParameterRef param = (ParameterRef)rhs;
						if(param.getIndex() == indexOfInterest) {
							Value lhs = identity.getLeftOp();
							SMTBinding newLhs = createNewBindingForValue(lhs);
							addValueBindingToVariableDeclaration(lhs, newLhs);
							SMTSimpleAssignment simpleAssignment = new SMTSimpleAssignment(newLhs, new SMTBindingValue(newRhs));
							SMTAssertStatement assignmentAssert = new SMTAssertStatement(simpleAssignment);
							addAssertStmtToAllPrograms(assignmentAssert);
						}
					}					
				}
			}
		}		
		else {
			System.err.println(String.format("Double-Check if the following method contains useful information which can be extracted: \n%s", stmt));
		}
		
	}
	

	@Override
	public void caseAssignStmt(AssignStmt stmt) {
		Value leftOp = stmt.getLeftOp();
		Value rightOp = stmt.getRightOp();
		SMTBinding rhs = null;
		
//		handling of boolean-assignments $a = 1 (aka $a = true)
//		Since Jimple's representation of boolean assignments is actually an integer assignment of 
//		either 0 or 1, we have to treat this assignment in a specific way.
//		Assumption: If the previous statement of the current statement is a boolean assignment and 
//		the current statement is an integer-assignment, the integer-assignment is a boolean assignment
//		since we manually insert the boolean (integer) assignment, this is a valuable assumption.
//		boolean assignmentProcessed = false;
//		int currentIndex = jimpleDataFlowStatements.indexOf(stmt);
//		if(currentIndex > 0) {
//			Stmt predecessor = jimpleDataFlowStatements.get(currentIndex-1);
//			
//			if(predecessor instanceof AssignStmt) {
//				AssignStmt predAssign = (AssignStmt)predecessor;
//				if(predAssign.getLeftOp().getType() instanceof BooleanType) {
//					assignmentProcessed = true;
//					
//					SMTConstantValue boolValue;
//					if(rightOp instanceof IntConstant) {
//						IntConstant intValue = (IntConstant)rightOp;
//						if(intValue.value == 0)
//							boolValue = new SMTConstantValue<Boolean>(false);
//						else if(intValue.value == 1) 
//							boolValue = new SMTConstantValue<Boolean>(true);
//						else
//							throw new RuntimeException("This should not happen");
//					}
//					else
//						throw new RuntimeException("This should not happen");
//					
//					//since we manually added (lhs = 0 or lhs = 1), there has to be a lhs!
//					SMTBinding lhs = getLatestBindingForValue(leftOp);
//
//					SMTSimpleAssignment simpleAss = new SMTSimpleAssignment(lhs, boolValue);
//					SMTAssertStatement assertStmt = new SMTAssertStatement(simpleAss);
//					addAssertStmtToAllPrograms(assertStmt);						
//				}
//			}			
//		}
//		
//		if(assignmentProcessed == false) {
			//treatment of right side
			if(rightOp instanceof Expr) {
				Expr rightOpExpr = (Expr)rightOp;
				exprVisitor.setCurrentStatement(stmt);
				rightOpExpr.apply(exprVisitor);
				rhs = exprVisitor.result;
			}
			else if(rightOp instanceof Local) {
				if(hasBindingForValue(rightOp))
					rhs = getLatestBindingForValue(rightOp);
				else {
					System.err.println("###### Doulbe-Check this here... #######");
					rhs = createNewBindingForValue(rightOp);
				}
			}
			else if(rightOp instanceof ArrayRef) {
				ArrayRef arrayRHS = (ArrayRef)rightOp;
				SMTBinding arrayBinding = getCorrectBindingForArrayRef(arrayRHS);
				if(arrayBinding != null)
					rhs = arrayBinding;
				else {
					SMTBinding.TYPE bindingType = createBindingType(arrayRHS.getType());
					rhs = createTemporalBinding(bindingType);				
				}
			}
			else if(rightOp instanceof FieldRef) {
				FieldRef field = (FieldRef)rightOp;
				if(hasBindingForField(field.getField())) {
					rhs = getLatestBindingForField(field.getField());
				}
				else {
					//base is tainted
					//=> just propagate the taint value of previous statement
					Stmt prevStmt = getPreviousDataFlowPathElement(stmt);
					if(prevStmt == null)
						throw new RuntimeException("there is no previous statement");
					else {			
						rhs = getBindingForTaintedValue(prevStmt);
						if(rhs == null)
							throw new RuntimeException("double check this here");
					}
				}				
			}
			else if(rightOp instanceof Constant) {
				if(rightOp instanceof IntConstant) {
					//special treatment if int constant is a placeholder for a boolean true or false:
					if(leftOp.getType() instanceof BooleanType) {
						SMTConstantValue boolValue;

						IntConstant intValue = (IntConstant)rightOp;
						if(intValue.value == 0)
							boolValue = new SMTConstantValue<Boolean>(false);
						else if(intValue.value == 1) 
							boolValue = new SMTConstantValue<Boolean>(true);
						else
							throw new RuntimeException("This should not happen");
																								
						SMTBinding lhs = getLatestBindingForValue(leftOp);
	
						SMTSimpleAssignment simpleAss = new SMTSimpleAssignment(lhs, boolValue);
						SMTAssertStatement assertStmt = new SMTAssertStatement(simpleAss);
						addAssertStmtToAllPrograms(assertStmt);
						
						//we already treated the lhs => we can return here
						return;
					}
				}
			}
			else
				throw new RuntimeException("todo");
	
			
			//treatement of left side
			//condition treatment
			if(rhs != null && rhs.getType() == SMTBinding.TYPE.Bool) {					
				SMTBinding lhs = createNewBindingForValue(leftOp);
				addValueBindingToVariableDeclaration(leftOp, lhs);

				SMTSimpleAssignment simpleAss = new SMTSimpleAssignment(lhs, new SMTBindingValue(rhs));
				SMTAssertStatement assertStmt = new SMTAssertStatement(simpleAss);
				addAssertStmtToAllPrograms(assertStmt);					
				
				
	//			List<Stmt> dataFlow = this.jimpleDataFlowStatements;
	//			int currentPosInDataFlow = dataFlow.indexOf(stmt);			
	//			Set<Boolean> possibleConditions = null;
	//			//in case we do have a conditional statement on the dataflow path, but is is not the last one,
	//			//we have to check whether this conditional statement has to be set to true or false in order 
	//			//to reach the next statement after the conditonal statement.
	//			if(currentPosInDataFlow < dataFlow.size()-1) {
	//				Set<Unit> nextDataFlowStmt = new HashSet<Unit>();
	//				nextDataFlowStmt.add(dataFlow.get(currentPosInDataFlow+1));
	//				possibleConditions = UtilSMT.extractConditionForReachingAUnit(cfg, stmt, nextDataFlowStmt);
	//			}
	//			//if the conditional statement is the last statement on the dataflow path, we have to get the 
	//			//condition how one reaches the target statement
	//			else
	//				possibleConditions = UtilSMT.extractConditionForReachingAUnit(cfg, stmt, targetUnits);
	//			
	//			if(possibleConditions.size() == 0) {
	//				System.err.println("### Double-check this here... ###");
	//			}
	//			//there is only one possible condition for reaching the logging point
	//			else if(possibleConditions.size() == 1) {
	//				boolean concreteCondition = possibleConditions.iterator().next();
	//				SMTSimpleAssignment assignment = null;
	//				if(concreteCondition == true)
	//					//rhs is now lhs
	//					assignment = new SMTSimpleAssignment(rhs, new SMTConstantValue<Boolean>(true));
	//				else
	//					//rhs is now lhs
	//					assignment = new SMTSimpleAssignment(rhs, new SMTConstantValue<Boolean>(false));
	//				SMTAssertStatement assignAssert = new SMTAssertStatement(assignment);
	//				addAssertStmtToAllPrograms(assignAssert);
	//			}
	//			//logging point can be reached either on the this-branch or the else-branch
	//			else {
	//				doubleSMTPrograms();
	//				addingTrueAndFalseConditionalAssertions(rhs);
	//			}
			}
			//e.g. String[] a = getArray()
			else if(leftOp.getType() instanceof ArrayType) {
				if(rhs == null) {
					//there is nothing to track
					return;
				}
				else {
					LoggerHelper.logWarning("Arrays are not supported yet");
				}				
			}
			//e.g. a[0] = "aa"
			else if(leftOp instanceof ArrayRef) {
				throw new RuntimeException("SMTConverter: ArrayRef not implemented yet");
			}
			else if(leftOp instanceof FieldRef) {
				FieldRef field = (FieldRef)leftOp;
				SMTBinding fieldBinding = createNewBindingForField(field.getField());
				addFieldBindingToVariableDeclaration(field.getField(), fieldBinding);
				
				SMTSimpleAssignment simpleAss = new SMTSimpleAssignment(fieldBinding, new SMTBindingValue(rhs));
				SMTAssertStatement assertStmt = new SMTAssertStatement(simpleAss);
				addAssertStmtToAllPrograms(assertStmt);
			}
			else{					
				SMTBinding lhs = createNewBindingForValue(leftOp);
				addValueBindingToVariableDeclaration(leftOp, lhs);
				
				SMTSimpleAssignment simpleAss = new SMTSimpleAssignment(lhs, new SMTBindingValue(rhs));
				SMTAssertStatement assertStmt = new SMTAssertStatement(simpleAss);
				addAssertStmtToAllPrograms(assertStmt);	
			}
//		}
		
		
	}
	
	
	public SMTBinding getCorrectBindingForArrayRef(ArrayRef arrayRef) {
		for(Map.Entry<String, SMTBinding> entry : arrayHelper.entrySet()) {
			if(entry.getKey().equals(arrayRef.toString()))
				return entry.getValue();
		}
		
		return null;
	}
	
	
	
	public SMTBinding.TYPE createBindingType(Type type) {
		if(type instanceof RefType) {
			RefType refType = (RefType) type;
			if(refType.getClassName().equals("java.lang.String")
				|| refType.getClassName().equals("java.lang.StringBuilder"))
				return SMTBinding.TYPE.String;			
			else
				return findProperTypeFor(type);				
		}
		else if(type instanceof ArrayType) {
			ArrayType arrayType = (ArrayType)type;
			if(arrayType.baseType instanceof RefType) {
				RefType baseTypeRef = (RefType)arrayType.baseType;
				if(baseTypeRef.getClassName().equals("java.lang.String"))
					return SMTBinding.TYPE.String;
				else
					throw new RuntimeException("todo");
			}
			else
				throw new RuntimeException("todo");
		}
		else if(type instanceof IntType) {
			return SMTBinding.TYPE.Int;
		}
		else if(type instanceof BooleanType)
			return SMTBinding.TYPE.Bool;
		else if(type instanceof DoubleType)
			return SMTBinding.TYPE.Real;
		else if(type instanceof LongType)
			return SMTBinding.TYPE.Int;
		else
			throw new RuntimeException("todo");		
	}
	
	
	private SMTBinding.TYPE findProperTypeFor(Type type) {
		if(this.tmpSmtProgram.getAssertStatements() != null &&
				this.tmpSmtProgram.getAssertStatements().size()>0) {
			int lastIndex = this.tmpSmtProgram.getAssertStatements().size()-1;
			SMTStatement smtStmt = this.tmpSmtProgram.getAssertStatements().get(lastIndex).getStatement();
			if(smtStmt instanceof SMTAssignment) {
				SMTAssignment smtAss = (SMTAssignment)smtStmt;
				return smtAss.getLhs().getType();
			}
			else
				throw new NotYetSupportedException("type " + type.toString() + " not supported yet");
		}
		else
			throw new NotYetSupportedException("type " + type.toString() + " not supported yet");
	}
	
	
	public SMTBinding getLHSOfLastAssertStmt() {
		if(this.tmpSmtProgram.getAssertStatements() != null &&
				this.tmpSmtProgram.getAssertStatements().size()>0) {
			int lastIndex = this.tmpSmtProgram.getAssertStatements().size()-1;
			SMTStatement smtStmt = this.tmpSmtProgram.getAssertStatements().get(lastIndex).getStatement();
			if(smtStmt instanceof SMTAssignment) {
				SMTAssignment smtAss = (SMTAssignment)smtStmt;
				return smtAss.getLhs();
			}
		}
		throw new RuntimeException("wrong assumption");
	}
	
	
	public SMTBinding.TYPE getTaintedValueTypeAtSink() {
		Stmt sinkStmt = jimpleDataFlowStatements.get(jimpleDataFlowStatements.size()-1);
		SMTBinding taintBinding = getBindingForTaintedValue(sinkStmt);
		if(taintBinding == null)
			throw new RuntimeException("double check this");
		
		return taintBinding.getType();
	}
	
	
	public SMTBinding getBindingForTaintedValue(Stmt stmt) {
		if(stmt.containsInvokeExpr()) {
			InvokeExpr invokeExpr = stmt.getInvokeExpr();
			SootMethod sootMethod = invokeExpr.getMethod();
			//special handling of non-api calls:
			//the tainted value provided by flowdroid is actually the identity statement in the body and not the 
			//argument, but we need the argument in this case. Therefore, we have to identify the correct tainted argument
			if(UtilInstrumenter.isAppDeveloperCode(sootMethod.getDeclaringClass())) {
				AccessPath accessPath = getCorrectAccessPathForStmt(stmt);
				Local identityStmtTaintValue = accessPath.getPlainValue();
				
				if(identityStmtTaintValue != null) {				
					Body body = sootMethod.retrieveActiveBody();	
					for(Unit unit : body.getUnits()) {
						if(unit instanceof IdentityStmt) {
							IdentityStmt identity = (IdentityStmt)unit;						
							Value rhs = identity.getRightOp();
							if(rhs instanceof ParameterRef) {
								if(identity.getLeftOp() == identityStmtTaintValue){
									ParameterRef param = (ParameterRef)rhs;
									int index = param.getIndex();
									Value argument = invokeExpr.getArg(index);
									if(hasBindingForValue(argument))
										return getLatestBindingForValue(argument);
									else
										return createNewBindingForValue(argument);								
								}
							}
							else if(rhs instanceof ThisRef) {
								if(identity.getLeftOp() == identityStmtTaintValue){								
									if(invokeExpr instanceof InstanceInvokeExpr) {
										InstanceInvokeExpr instanceInvoke = (InstanceInvokeExpr)invokeExpr;
										Value base = instanceInvoke.getBase();
										if(hasBindingForValue(base))
											return getLatestBindingForValue(base);
										else
											return createNewBindingForValue(base);
									}
									else
										throw new RuntimeException("this should not happen...");								
								}
							}
						}					
					}				
					throw new RuntimeException("There should be an identity statement!");
				}
				else{
					return null;
				}
			}
			else{			
				AccessPath accessPath = getCorrectAccessPathForStmt(stmt);
				Local taintValue = accessPath.getPlainValue();
				if(hasBindingForValue(taintValue))
					return getLatestBindingForValue(taintValue);
				else{
					return createNewBindingForValue(taintValue);
				}
			}
		}
		else {
			AccessPath accessPath = getCorrectAccessPathForStmt(stmt);
			Local taintValue = accessPath.getPlainValue();
			if(hasBindingForValue(taintValue))
				return getLatestBindingForValue(taintValue);
			else{
				return createNewBindingForValue(taintValue);
			}
		} 
	}
	
	
	public AccessPath getCorrectAccessPathForStmt(Stmt stmt) {
		for (int i = 0; i < this.jimpleDataFlowStatements.size(); i++) {
			if (this.jimpleDataFlowStatements.get(i) == stmt)
				return this.accessPathPath.get(i);
		}
		throw new RuntimeException("There should be a statement in the data flow path");
	}
	
	
	public void addArrayRef(String arrayWithIndex, SMTBinding value) {
		this.arrayHelper.put(arrayWithIndex, value);
	}
	
	
	public Stmt getPreviousDataFlowPathElement(Stmt currentStmt) {
		if(jimpleDataFlowStatements.contains(currentStmt)) {
			int index = jimpleDataFlowStatements.indexOf(currentStmt);
			if(index == 0)
				return null;
			else
				return jimpleDataFlowStatements.get(index-1);
		}
		else{
			return null;
		}
	}

	@Override
	public void caseIdentityStmt(IdentityStmt stmt) {
		Value leftOp = stmt.getLeftOp();
		Value rightOp = stmt.getRightOp();
		SMTBinding rhs = null;
		
		
		if(rightOp instanceof Local) {
			if(hasBindingForValue(rightOp))
				rhs = getLatestBindingForValue(rightOp);
			else {
				rhs = createNewBindingForValue(rightOp);
				addValueBindingToVariableDeclaration(rightOp, rhs);
			}
		}
		else if(rightOp instanceof ArrayRef) {
			ArrayRef arrayRHS = (ArrayRef)rightOp;
			SMTBinding arrayBinding = getCorrectBindingForArrayRef(arrayRHS);
			if(arrayBinding != null)
				rhs = arrayBinding;
			else {
				SMTBinding.TYPE bindingType = createBindingType(arrayRHS.getType());
				rhs = createTemporalBinding(bindingType);				
			}
		}
		else if(rightOp instanceof FieldRef) {
			FieldRef field = (FieldRef)rightOp;
			if(hasBindingForField(field.getField())) {
				rhs = getLatestBindingForField(field.getField());
			}
			else {
				//base is tainted
				//=> just propagate the taint value of previous statement
				Stmt prevStmt = getPreviousDataFlowPathElement(stmt);
				if(prevStmt == null)
					throw new RuntimeException("there is no previous statement");
				else {			
					rhs = getBindingForTaintedValue(prevStmt);
					if(rhs == null)
						throw new RuntimeException("double check this here");
				}
			}				
		}
		else if(rightOp instanceof Constant) {
			if(rightOp instanceof IntConstant) {
				//special treatment if int constant is a placeholder for a boolean true or false:
				if(leftOp.getType() instanceof BooleanType) {
					SMTConstantValue boolValue;

					IntConstant intValue = (IntConstant)rightOp;
					if(intValue.value == 0)
						boolValue = new SMTConstantValue<Boolean>(false);
					else if(intValue.value == 1) 
						boolValue = new SMTConstantValue<Boolean>(true);
					else
						throw new RuntimeException("This should not happen");
																							
					SMTBinding lhs = getLatestBindingForValue(leftOp);

					SMTSimpleAssignment simpleAss = new SMTSimpleAssignment(lhs, boolValue);
					SMTAssertStatement assertStmt = new SMTAssertStatement(simpleAss);
					addAssertStmtToAllPrograms(assertStmt);
					
					//we already treated the lhs => we can return here
					return;
				}
			}
		}
		else if(rightOp instanceof ThisRef) {
			ThisRef thisref = (ThisRef)rightOp;
			rhs = createNewBindingForThisRef(thisref);
		}
		else if(rightOp instanceof CaughtExceptionRef) {
			rhs = createTemporalBinding(TYPE.String);
		}
		//e.g. @parameter0: com.a.a.AR
		else if(rightOp instanceof ParameterRef) {					
			//base is tainted
			//=> just propagate the taint value of previous statement
			Stmt prevStmt = getPreviousDataFlowPathElement(stmt);
			if(prevStmt == null)
				throw new RuntimeException("there is no previous statement");
			else {			
				rhs = getBindingForTaintedValue(prevStmt);
				if(rhs == null)
					throw new RuntimeException("double check this here");
			}				
		}
		else
			throw new RuntimeException("todo");
		
		SMTBinding lhs = createNewBindingForValue(leftOp);
		addValueBindingToVariableDeclaration(leftOp, lhs);
		
		SMTSimpleAssignment simpleAss = new SMTSimpleAssignment(lhs, new SMTBindingValue(rhs));
		SMTAssertStatement assertStmt = new SMTAssertStatement(simpleAss);
		addAssertStmtToAllPrograms(assertStmt);	
	}

	@Override
	public void caseEnterMonitorStmt(EnterMonitorStmt stmt) {
		throw new RuntimeException("todo");
		
	}

	@Override
	public void caseExitMonitorStmt(ExitMonitorStmt stmt) {
		throw new RuntimeException("todo");
		
	}

	@Override
	public void caseGotoStmt(GotoStmt stmt) {
		throw new RuntimeException("todo");
		
	}

	@Override
	public void caseIfStmt(IfStmt stmt) {
		throw new RuntimeException("todo");
		
	}

	@Override
	public void caseLookupSwitchStmt(LookupSwitchStmt stmt) {
		throw new RuntimeException("todo");
		
	}

	@Override
	public void caseNopStmt(NopStmt stmt) {
		throw new RuntimeException("todo");
		
	}

	@Override
	public void caseRetStmt(RetStmt stmt) {
		throw new RuntimeException("todo");
		
	}

	@Override
	public void caseReturnStmt(ReturnStmt stmt) {
		//in case of return CONSTANT, we do nothing; unfortunately, this is part of FlowDroid's path
		if(stmt.getOp() instanceof Constant)
			return;
		int index = jimpleDataFlowStatements.indexOf(stmt);
		AccessPath ap = accessPathPath.get(index);
		Local local = ap.getPlainValue();
				
		SMTBinding lhs = createNewBindingForValue(local);
		addValueBindingToVariableDeclaration(local, lhs);
		
		if(!hasBindingForValue(stmt.getOp()))
			throw new RuntimeException("There has to be a tainted value");
		SMTBinding rhs = getLatestBindingForValue(stmt.getOp());
		
		SMTSimpleAssignment simpleAss = new SMTSimpleAssignment(lhs, new SMTBindingValue(rhs));
		SMTAssertStatement assertStmt = new SMTAssertStatement(simpleAss);
		addAssertStmtToAllPrograms(assertStmt);	
	}

	@Override
	public void caseReturnVoidStmt(ReturnVoidStmt stmt) {
		//do nothing
		return;
	}

	@Override
	public void caseTableSwitchStmt(TableSwitchStmt stmt) {
		throw new RuntimeException("todo");
		
	}

	@Override
	public void caseThrowStmt(ThrowStmt stmt) {
		throw new RuntimeException("todo");
		
	}

	@Override
	public void defaultCase(Object obj) {
		throw new RuntimeException("todo");		
	}		
	
	public Map<SMTProgram, Set<DynamicValueInformation>> getDynamicValueInfos() {
		return dynamicValueInfos;
	}

	
	public void addNewDynamicValueForBaseObjectToMap(Stmt stmt, SMTBinding binding) {
		if(!dynamicValueInfos.keySet().contains(currentSMTProgram))
			dynamicValueInfos.put(currentSMTProgram, new HashSet<DynamicValueInformation>());
		
		DynamicValueInformation dynValueInfo = new DynamicValueInformation(stmt, binding);
		dynValueInfo.setBaseObject(true);
		
		dynamicValueInfos.get(currentSMTProgram).add(dynValueInfo);
	}
	
	
	public void addNewDynamicValueForArgumentToMap(Stmt stmt, SMTBinding binding, int argPos) {
		if(!dynamicValueInfos.keySet().contains(currentSMTProgram))
			dynamicValueInfos.put(currentSMTProgram, new HashSet<DynamicValueInformation>());
		
		DynamicValueInformation dynValueInfo = new DynamicValueInformation(stmt, binding);
		dynValueInfo.setBaseObject(false);
		dynValueInfo.setArgPos(argPos);
		
		dynamicValueInfos.get(currentSMTProgram).add(dynValueInfo);
	}

}
