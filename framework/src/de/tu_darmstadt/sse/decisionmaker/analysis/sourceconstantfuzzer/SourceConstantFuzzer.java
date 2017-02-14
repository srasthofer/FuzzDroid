
package de.tu_darmstadt.sse.decisionmaker.analysis.sourceconstantfuzzer;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sun.tools.javac.comp.Todo;

import de.tu_darmstadt.sse.apkspecific.CodeModel.CodePositionManager;
import de.tu_darmstadt.sse.appinstrumentation.UtilInstrumenter;
import de.tu_darmstadt.sse.appinstrumentation.transformer.InstrumentedCodeTag;
import de.tu_darmstadt.sse.decisionmaker.DeterministicRandom;
import de.tu_darmstadt.sse.decisionmaker.analysis.AnalysisDecision;
import de.tu_darmstadt.sse.decisionmaker.analysis.FuzzyAnalysis;
import de.tu_darmstadt.sse.decisionmaker.analysis.randomFuzzer.RandomPrimitives;
import de.tu_darmstadt.sse.decisionmaker.server.ThreadTraceManager;
import de.tu_darmstadt.sse.decisionmaker.server.TraceManager;
import de.tu_darmstadt.sse.decisionmaker.server.history.ClientHistory;
import de.tu_darmstadt.sse.sharedclasses.networkconnection.DecisionRequest;
import de.tu_darmstadt.sse.sharedclasses.networkconnection.ServerResponse;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.DoubleConstant;
import soot.jimple.FloatConstant;
import soot.jimple.IntConstant;
import soot.jimple.LongConstant;
import soot.jimple.StringConstant;
import soot.jimple.infoflow.solver.cfg.InfoflowCFG;


public class SourceConstantFuzzer extends FuzzyAnalysis {

	private final boolean RETURN_DUPLICATES = false;

	private InfoflowCFG ifCFG = null;

	private ConstantContainer stringContainer = new ConstantContainer();
	private ConstantContainer intContainer = new ConstantContainer();
	private ConstantContainer longContainer = new ConstantContainer();
	private ConstantContainer doubleContainer = new ConstantContainer();
	private ConstantContainer floatContainer = new ConstantContainer();
	
	private ConstantContainer booleanContainer = new ConstantContainer();
	private ConstantContainer charContainer = new ConstantContainer();
	private ConstantContainer shortContainer = new ConstantContainer();
	private ConstantContainer byteContainer = new ConstantContainer();

	// once we improved the metric we stick to our "good" response
	private Map<DecisionRequest, ServerResponse> pinnedResponses = new HashMap<>();

	private Map<Integer, HashSet<Object>> oldValues = new HashMap<Integer, HashSet<Object>>();
	
	private RandomPrimitives randPrimitives = new RandomPrimitives();

	@Override
	public void doPreAnalysis(Set<Unit> targetUnits, TraceManager traceManager) {

		ifCFG = new InfoflowCFG();

		for (SootClass sc : Scene.v().getClasses()) {

			if (!UtilInstrumenter.isAppDeveloperCode(sc)) {
				continue;
			}

			for (SootMethod m : sc.getMethods()) {
				if (m.hasActiveBody()) {
					for (Unit u : m.getActiveBody().getUnits()) {
						if (!u.hasTag(InstrumentedCodeTag.name))
							getConstants(u, sc);
					}
				}
			}

		}
		
		fillWithDummyValues();

		convertSets2Arrays();

		// debug output to show found strings
//		for (String className : doubleContainer.getArrayMap().keySet()) {
//			System.out.println("SourceConstantFuzzer: Classname: " + className);
//			Object[] valuesString = stringContainer.getArrayMap().get(className);
//			
//			Object[] valuesInt = intContainer.getArrayMap().get(className);
//			Object[] valuesLong = longContainer.getArrayMap().get(className);
//			Object[] valuesDouble = doubleContainer.getArrayMap().get(className);
//			Object[] valuesFloat = floatContainer.getArrayMap().get(className);
//			Object[] valuesBoolean = stringContainer.getArrayMap().get(className); //TODO!
//			
//			if(null != valuesString)
//			{
//				for (Object o : valuesString) {
//					System.out.println("\tFound String: " + o);
//				}
//			}
//			else
//			{
//				System.out.println("###No Strings in this class###");
//			}			
//		}
		
		System.out.println("#Constants in App including dummy values");
		System.out.println("#Strings in App: " + stringContainer.getAllValues().length);
		System.out.println("#Integers in App: " + intContainer.getAllValues().length);
		System.out.println("#Longs in App: " + longContainer.getAllValues().length);
		System.out.println("#Doubles in App: " + doubleContainer.getAllValues().length);
		System.out.println("#Floats in App: " + floatContainer.getAllValues().length);
		System.out.println("#Booleans in App: " + booleanContainer.getAllValues().length);
		System.out.println("#Chars in App: " + charContainer.getAllValues().length);
		System.out.println("#Shorts in App: " + shortContainer.getAllValues().length);
		System.out.println("#bytes in App: " + byteContainer.getAllValues().length);
		
	}
	
	private void fillWithDummyValues()
	{
		if (stringContainer.isEmpty())
		{
			System.out.println("*** No Strings in App ***");
			
			generateDummy("String", 5);
		}
		
		if (intContainer.isEmpty())
		{
			System.out.println("*** No Integers in App ***");
			
			generateDummy("int", 5);
		}
		
		if (longContainer.isEmpty())
		{
			System.out.println("*** No Longs in App ***");
			generateDummy("long", 5);
		}
		
		if (doubleContainer.isEmpty())
		{
			System.out.println("*** No Doubles in App ***");
			generateDummy("double", 5);
		}
		
		if (floatContainer.isEmpty())
		{
			System.out.println("*** No Floats in App ***");
			generateDummy("float", 5);
		}
		
		//always do dummys for boolean
		generateDummy("boolean", 2);
		
		if(byteContainer.isEmpty())
		{
			generateDummy("byte", 5);
		}
		
		if(shortContainer.isEmpty())
		{
			generateDummy("short", 5);
		}
		
		if(charContainer.isEmpty())
		{
			generateDummy("char", 5);
		}
		
	}

	private void generateDummy(String type, int count) {				
		
		if("String".equals(type))
		{
			for (int i=0; i<count; i++)
			{
				Object value = randPrimitives.next("String");				
				stringContainer.insertTmpValue("DummyClass", value);
			}
		}
		else if("int".equals(type))
		{
			for (int i=0; i<count; i++)
			{
				Object value = randPrimitives.next("int");				
				intContainer.insertTmpValue("DummyClass", value);
			}
		}
		else if("long".equals(type))
		{
			for (int i=0; i<count; i++)
			{
				Object value = randPrimitives.next("long");				
				longContainer.insertTmpValue("DummyClass", value);
			}
		}
		else if("double".equals(type))
		{
			for (int i=0; i<count; i++)
			{
				Object value = randPrimitives.next("double");				
				doubleContainer.insertTmpValue("DummyClass", value);
			}
		}
		else if("float".equals(type))
		{
			for (int i=0; i<count; i++)
			{
				Object value = randPrimitives.next("float");				
				floatContainer.insertTmpValue("DummyClass", value);
			}
		}
		else if("boolean".equals(type))
		{
			booleanContainer.insertTmpValue("DummyClass", (Object) true);
			booleanContainer.insertTmpValue("DummyClass", (Object) false);
		}
		else if("char".equals(type))
		{
			for (int i=0; i<count; i++)
			{
				Object value = randPrimitives.next("char");				
				charContainer.insertTmpValue("DummyClass", value);
			}
		}
		else if("short".equals(type))
		{
			for (int i=0; i<count; i++)
			{
				Object value = randPrimitives.next("short");				
				shortContainer.insertTmpValue("DummyClass", value);
			}
		}
		else if("byte".equals(type))
		{
			for (int i=0; i<count; i++)
			{
				Object value = randPrimitives.next("byte");				
				byteContainer.insertTmpValue("DummyClass", value);
			}
		}
		
	}

	@Override
	public List<AnalysisDecision> resolveRequest(DecisionRequest clientRequest, ThreadTraceManager completeHistory) {

		// We only model "after"-style hooks
		if (!clientRequest.isHookAfter()) {
			return null;
		}

		// We only model method return values
		String hookSignature = clientRequest.getLoggingPointSignature();

		String returnType = extractReturnType(hookSignature);
		if (returnType.equals("void")) {
			return null;
		}
		
		//store codeposition
		int codePosition = clientRequest.getCodePosition();

		// by default we return the original value for types we do not
		// handle
		Object newReturnValue = clientRequest.getRuntimeValueOfReturn();
		ServerResponse response = new ServerResponse();
		response.setAnalysisName(getAnalysisName());

		// from the 3rd attempt compare metrics of last and lastlast value
		if (completeHistory.getHistories().size() > 1) {
			int bdLast = completeHistory.getNewestClientHistory().getProgressValue("ApproachLevel");
			int bdLastLast = completeHistory.getLastClientHistory().getProgressValue("ApproachLevel");
			boolean improvedOverLastRun = (bdLast < bdLastLast);

			// look at history to not return same value again, only
			// do this if metric improves

			// If we already have a good value for this place, let's keep it
			ServerResponse pinnedResponse = pinnedResponses.get(clientRequest);
			if (pinnedResponse != null) {
				pinnedResponse.setAnalysisName(getAnalysisName());
				AnalysisDecision finalDecision = new AnalysisDecision();
				finalDecision.setDecisionWeight(3);
				finalDecision.setAnalysisName(getAnalysisName());
				finalDecision.setServerResponse(pinnedResponse);
				return Collections.singletonList(finalDecision);
			} else if (improvedOverLastRun) {
				ClientHistory lastHistory = completeHistory.getLastClientHistory();
				if (lastHistory != null) {
					// return the last value and stick to it for the future
					AnalysisDecision decision = lastHistory.getResponseForRequest(
							clientRequest);
					if (decision != null) {
						ServerResponse lastReturnValue = decision.getServerResponse();
						lastReturnValue.setAnalysisName(getAnalysisName());
						pinnedResponses.put(clientRequest, lastReturnValue);
						AnalysisDecision finalDecision = new AnalysisDecision();
						finalDecision.setDecisionWeight(3);
						finalDecision.setAnalysisName(getAnalysisName());
						finalDecision.setServerResponse(lastReturnValue);
						return Collections.singletonList(finalDecision);
					}
				}
			}
			// if not improvedOverLastRun -> pick new value, see below
		}

		// pick a new return value:

		CodePositionManager codePosMgr = CodePositionManager.getCodePositionManagerInstance();
		Unit codeUnit = codePosMgr.getUnitForCodePosition(codePosition);

		SootMethod originMethod = ifCFG.getMethodOf(codeUnit);
		if(originMethod == null)
			return null;
		SootClass originClass = originMethod.getDeclaringClass();
		String className = originClass.getName();

		HashSet<Object> lastReturnValuesForCodePos = oldValues.get(codePosition);

		// System.out.println("SourceConstantFuzzer: Lookup value in class: " +
		// className);

		switch (returnType) {
		case "java.lang.String":
			newReturnValue = chooseNewValue(lastReturnValuesForCodePos, stringContainer.getArrayMap().get(className));
			if (null == newReturnValue) // we did not find anything in current
										// class, try ALL string constants
			{
				newReturnValue = chooseNewValue(lastReturnValuesForCodePos, stringContainer.getAllValues());
			}
			response.setResponseExist(true);

			break;
		case "int":
			newReturnValue = chooseNewValue(lastReturnValuesForCodePos, intContainer.getArrayMap().get(className));
			if (null == newReturnValue) // we did not find anything in current
										// class, try ALL int constants
			{
				newReturnValue = chooseNewValue(lastReturnValuesForCodePos, intContainer.getAllValues());
			}
			response.setResponseExist(true);
			break;
		case "long":
			newReturnValue = chooseNewValue(lastReturnValuesForCodePos, longContainer.getArrayMap().get(className));
			if (null == newReturnValue) // we did not find anything in current
										// class, try ALL long constants
			{
				newReturnValue = chooseNewValue(lastReturnValuesForCodePos, longContainer.getAllValues());
			}
			response.setResponseExist(true);
			break;
		case "float":
			newReturnValue = chooseNewValue(lastReturnValuesForCodePos, floatContainer.getArrayMap().get(className));
			if (null == newReturnValue) // we did not find anything in current
										// class, try ALL float constants
			{
				newReturnValue = chooseNewValue(lastReturnValuesForCodePos, floatContainer.getAllValues());
			}
			response.setResponseExist(true);
			break;
		case "double":
			newReturnValue = chooseNewValue(lastReturnValuesForCodePos, doubleContainer.getArrayMap().get(className));
			if (null == newReturnValue) // we did not find anything in current
										// class, try ALL double constants
			{
				newReturnValue = chooseNewValue(lastReturnValuesForCodePos, doubleContainer.getAllValues());
			}
			response.setResponseExist(true);
			break;
		case "boolean":
			newReturnValue = chooseNewValue(lastReturnValuesForCodePos, booleanContainer.getArrayMap().get(className));
			if (null == newReturnValue) // we did not find anything in current
										// class, try ALL boolean constants
			{
				newReturnValue = chooseNewValue(lastReturnValuesForCodePos, booleanContainer.getAllValues());
			}
			response.setResponseExist(true);
			break;
		case "char":
			newReturnValue = chooseNewValue(lastReturnValuesForCodePos, charContainer.getArrayMap().get(className));
			if (null == newReturnValue) // we did not find anything in current
										// class, try ALL boolean constants
			{
				newReturnValue = chooseNewValue(lastReturnValuesForCodePos, charContainer.getAllValues());
			}
			response.setResponseExist(true);
			break;
		case "short":
			newReturnValue = chooseNewValue(lastReturnValuesForCodePos, shortContainer.getArrayMap().get(className));
			if (null == newReturnValue) // we did not find anything in current
										// class, try ALL boolean constants
			{
				newReturnValue = chooseNewValue(lastReturnValuesForCodePos, shortContainer.getAllValues());
			}
			response.setResponseExist(true);
			break;
		case "byte":
			newReturnValue = chooseNewValue(lastReturnValuesForCodePos, byteContainer.getArrayMap().get(className));
			if (null == newReturnValue) // we did not find anything in current
										// class, try ALL boolean constants
			{
				newReturnValue = chooseNewValue(lastReturnValuesForCodePos, byteContainer.getAllValues());
			}
			response.setResponseExist(true);
			break;
		default:
			break;
		}

		if (null == newReturnValue) {
			System.err.println(
					"SourceConstantFuzzer: ALL Values have been tried (out of options)! -> returning NULL now");

			// if we ran out of options, we can return null here
			return null;
		}

		// remember old return values
		HashSet<Object> oldSet = oldValues.get(codePosition);
		if (null != oldSet) {
			oldSet.add(newReturnValue);
		} else {
			HashSet<Object> tmpSet = new HashSet<Object>();
			tmpSet.add(newReturnValue);
			oldValues.put(codePosition, tmpSet);
		}

		// prepare new return value response
		response.setReturnValue(newReturnValue);
		AnalysisDecision finalDecision = new AnalysisDecision();
		finalDecision.setDecisionWeight(3);
		finalDecision.setAnalysisName(getAnalysisName());
		finalDecision.setServerResponse(response);
		return Collections.singletonList(finalDecision);
	}

	@Override
	public void reset() {
	}

	private void getConstants(Unit u, SootClass sootClass) {

		for (ValueBox vb : u.getUseBoxes()) {

			Value val = vb.getValue();

			if (val instanceof StringConstant) {
				stringContainer.insertTmpValue(sootClass.getName(), ((StringConstant) val).value);
			} else if (val instanceof IntConstant) {
				int value = ((IntConstant) val).value;
				intContainer.insertTmpValue(sootClass.getName(), value);
								
				//test size of int, whether it might also be a char, byte or short
				
				//char (or 65,535 inclusive)
				if(value >= 0 && value <= 65536)
				{
					charContainer.insertTmpValue(sootClass.getName(), (char) value);
				}
				//short -32,768 and a maximum value of 32,767 (inclusive)
				if(value >= -32768 && value <= 32767)
				{
					shortContainer.insertTmpValue(sootClass.getName(), (short) value);
				}
				//byte: -128 and a maximum value of 127 (inclusive)
				if(value > -128 && value <= 127)
				{
					byteContainer.insertTmpValue(sootClass.getName(), (byte) value);
				}				
				
			} else if (val instanceof LongConstant) {
				longContainer.insertTmpValue(sootClass.getName(), ((LongConstant) val).value);
			} else if (val instanceof DoubleConstant) {
				doubleContainer.insertTmpValue(sootClass.getName(), ((DoubleConstant) val).value);
			} else if (val instanceof FloatConstant) {
				floatContainer.insertTmpValue(sootClass.getName(), ((FloatConstant) val).value);
			}
		}
	}

	private void convertSets2Arrays() {
		stringContainer.convertSetsToArrays();
		intContainer.convertSetsToArrays();
		floatContainer.convertSetsToArrays();
		longContainer.convertSetsToArrays();
		doubleContainer.convertSetsToArrays();
		booleanContainer.convertSetsToArrays();
		charContainer.convertSetsToArrays();
		shortContainer.convertSetsToArrays();
		byteContainer.convertSetsToArrays();
	}

	private Object chooseNewValue(HashSet<Object> usedValues, Object[] allValues) {

		Object newValue = null;

		// if we do not have any constant values for the requested type, we
		// return null
		if (null == allValues || allValues.length == 0) {
			return null;
		}

		// if we havent used any values, just pick one
		if (null == usedValues || usedValues.size() == 0) {
			newValue = allValues[DeterministicRandom.theRandom.nextInt(allValues.length)];
		} else {
			if (!RETURN_DUPLICATES) {
				// if all values have been tried -> return null == out of values
				if (usedValues.size() == allValues.length) {
					return null;
				}

				do {
					newValue = allValues[DeterministicRandom.theRandom.nextInt(allValues.length)];
				} while (usedValues.contains(newValue));
			} else {
				// just choose one and dont care about duplicates
				newValue = allValues[DeterministicRandom.theRandom.nextInt(allValues.length)];
			}

		}

		return newValue;
	}

	private String extractReturnType(String methodSignature) {
		return methodSignature.split(": ")[1].split(" ")[0];
	}

	@Override
	public String getAnalysisName() {
		return "SourceConstantFuzzer";
	}

}
