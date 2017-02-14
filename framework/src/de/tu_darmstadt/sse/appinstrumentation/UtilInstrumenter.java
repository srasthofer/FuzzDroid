package de.tu_darmstadt.sse.appinstrumentation;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import soot.ArrayType;
import soot.Body;
import soot.BooleanType;
import soot.ByteType;
import soot.CharType;
import soot.DoubleType;
import soot.FloatType;
import soot.IntType;
import soot.Local;
import soot.LongType;
import soot.PrimType;
import soot.RefType;
import soot.Scene;
import soot.ShortType;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.javaToJimple.LocalGenerator;
import soot.jimple.ArrayRef;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.Jimple;
import soot.jimple.NewArrayExpr;
import soot.jimple.StaticInvokeExpr;
import de.tu_darmstadt.sse.FrameworkOptions;
import de.tu_darmstadt.sse.appinstrumentation.transformer.InstrumentedCodeTag;
import de.tu_darmstadt.sse.sharedclasses.util.Pair;


public class UtilInstrumenter {
	
	public final static String JAVA_CLASS_FOR_PATH_INSTRUMENTATION = "de.tu_darmstadt.sse.additionalappclasses.tracing.BytecodeLogger";
	public final static String JAVA_CLASS_FOR_CODE_POSITIONS = "de.tu_darmstadt.sse.additionalappclasses.tracing.BytecodeLogger";
	public final static String JAVA_CLASS_FOR_CRASH_REPORTING = "de.tu_darmstadt.sse.additionalappclasses.crashreporter.CrashReporter";
	public final static String JAVA_CLASS_FOR_PATH_EXECUTION = "de.tu_darmstadt.sse.additionalappclasses.pathexecution.PathExecutor";
	
	public final static String ROOT_PACKAGE_OF_INSTRUMENTED_CODE  = "de.tu_darmstadt.sse.";
	public final static String PACKAGE_FOR_HOOKER = "com.morgoo.hook.";
	
	public final static String HELPER_APPLICATION_FOR_HOOKING = "de.tu_darmstadt.sse.additionalappclasses.HookingHelperApplication";
	public final static String HELPER_SERVICE_FOR_PATH_TRACKING = "de.tu_darmstadt.sse.additionalappclasses.tracing.TracingService";
	public final static String COMPONENT_CALLER_SERVICE_HELPER = "de.tu_darmstadt.sse.additionalappclasses.ComponentCallerService";
	public final static String HOOKER_CLASS = "de.tu_darmstadt.sse.additionalappclasses.hooking.Hooker";
	
	public final static String ADDITIONAL_APP_CLASSES_BIN = FrameworkOptions.frameworkDir + "/additionalAppClassesBin";
	public final static String SHARED_CLASSES_BIN =  FrameworkOptions.frameworkDir + "/sharedClassesBin";
	
	public final static String HOOKING_LIBRARY = FrameworkOptions.frameworkDir + "/libs/zhook.jar";
	public final static String HOOKING_LIBRARY_ARM = FrameworkOptions.frameworkDir + "/hooking/armeabi/libZHook.so";
	public final static String HOOKING_LIBRARY_X86 = FrameworkOptions.frameworkDir + "/hooking/x86/libZHook.so";
	
	public final static String SOOT_OUTPUT = "./sootOutput";
	public final static String SOOT_OUTPUT_APK = UtilInstrumenter.SOOT_OUTPUT + File.separator
			+ new File(FrameworkOptions.getAPKName()).getName() + ".apk";
	public final static String SOOT_OUTPUT_DEPLOYED_APK = UtilInstrumenter.SOOT_OUTPUT + File.separator
			+ new File(FrameworkOptions.getAPKName()).getName() + "_deployed.apk";
		
	
	public static boolean isApiCall(InvokeExpr invokeExpr){
		if(invokeExpr.getMethod().getDeclaringClass().isLibraryClass() 
				|| invokeExpr.getMethod().getDeclaringClass().isJavaLibraryClass())
			return true;
		else
			return false;
	}
	
	
	public static boolean isAppDeveloperCode(SootClass className) {
		return !(className.getPackageName().startsWith("android.") ||
				className.getPackageName().startsWith("java.") ||
				className.toString().startsWith(UtilInstrumenter.ROOT_PACKAGE_OF_INSTRUMENTED_CODE) ||
				className.toString().startsWith(UtilInstrumenter.PACKAGE_FOR_HOOKER) ||
				className.toString().contains("dummyMainClass")
				);
	}
	
	
	public static Unit makeJimpleStaticCallForPathExecution(String methodName, Object... args) {
		SootClass sootClass = Scene.v().getSootClass(UtilInstrumenter.JAVA_CLASS_FOR_PATH_EXECUTION);
		
		Unit generated = null;

		ArrayList<Type> argTypes = new ArrayList<Type>();
		ArrayList<Value> argList = new ArrayList<Value>();

		if (args != null) {
		if (args.length % 2 != 0) {
			throw new RuntimeException(
					"Mismatched argument types:values in static call to "
							+ methodName);
		} else {
			for (int i = 0; i < args.length; i++)
				if (i % 2 == 0) // First type, then argument
					argTypes.add((Type) args[i]);
				else
					argList.add((Value) args[i]);
		}
		}

		SootMethod createAndAdd = sootClass.getMethod(methodName, argTypes);
		StaticInvokeExpr sie = Jimple.v().newStaticInvokeExpr(
				createAndAdd.makeRef(), argList);

		
		generated = Jimple.v().newInvokeStmt(sie);
		
		return generated;
	}
	
	
	public static Pair<Value, List<Unit>> generateParameterArray(List<Value> parameterList, Body body){
		List<Unit> generated = new ArrayList<Unit>();
		
		NewArrayExpr arrayExpr = Jimple.v().newNewArrayExpr(RefType.v("java.lang.Object"), IntConstant.v(parameterList.size()));
		
		Value newArrayLocal = generateFreshLocal(body, getParameterArrayType());
		Unit newAssignStmt = Jimple.v().newAssignStmt(newArrayLocal, arrayExpr);
		generated.add(newAssignStmt);
		
		for(int i = 0; i < parameterList.size(); i++){
			Value index = IntConstant.v(i);
			ArrayRef leftSide = Jimple.v().newArrayRef(newArrayLocal, index);
			Value rightSide = generateCorrectObject(body, parameterList.get(i), generated);
			
			Unit parameterInArray = Jimple.v().newAssignStmt(leftSide, rightSide);
			generated.add(parameterInArray);
		}
		
		return new Pair<Value, List<Unit>>(newArrayLocal, generated);
	}
	
	
	public static Value generateCorrectObject(Body body, Value value, List<Unit> generated){
		if(value.getType() instanceof PrimType){
			//in case of a primitive type, we use boxing (I know it is not nice, but it works...) in order to use the Object type
			if(value.getType() instanceof BooleanType){
				Local booleanLocal = generateFreshLocal(body, RefType.v("java.lang.Boolean"));
				
				SootClass sootClass = Scene.v().getSootClass("java.lang.Boolean");
				SootMethod valueOfMethod = sootClass.getMethod("java.lang.Boolean valueOf(boolean)");
				StaticInvokeExpr staticInvokeExpr = Jimple.v().newStaticInvokeExpr(valueOfMethod.makeRef(), value);
				
				Unit newAssignStmt = Jimple.v().newAssignStmt(booleanLocal, staticInvokeExpr);
				newAssignStmt.addTag(new InstrumentedCodeTag());
				generated.add(newAssignStmt);
				
				return booleanLocal;
			}
			else if(value.getType() instanceof ByteType){
				Local byteLocal = generateFreshLocal(body, RefType.v("java.lang.Byte"));
				
				SootClass sootClass = Scene.v().getSootClass("java.lang.Byte");
				SootMethod valueOfMethod = sootClass.getMethod("java.lang.Byte valueOf(byte)");
				StaticInvokeExpr staticInvokeExpr = Jimple.v().newStaticInvokeExpr(valueOfMethod.makeRef(), value);
				
				Unit newAssignStmt = Jimple.v().newAssignStmt(byteLocal, staticInvokeExpr);
				newAssignStmt.addTag(new InstrumentedCodeTag());
				generated.add(newAssignStmt);
				
				return byteLocal;
			}
			else if(value.getType() instanceof CharType){
				Local characterLocal = generateFreshLocal(body, RefType.v("java.lang.Character"));
				
				SootClass sootClass = Scene.v().getSootClass("java.lang.Character");
				SootMethod valueOfMethod = sootClass.getMethod("java.lang.Character valueOf(char)");
				StaticInvokeExpr staticInvokeExpr = Jimple.v().newStaticInvokeExpr(valueOfMethod.makeRef(), value);
				
				Unit newAssignStmt = Jimple.v().newAssignStmt(characterLocal, staticInvokeExpr);
				newAssignStmt.addTag(new InstrumentedCodeTag());
				generated.add(newAssignStmt); 
				
				return characterLocal;
			}
			else if(value.getType() instanceof DoubleType){
				Local doubleLocal = generateFreshLocal(body, RefType.v("java.lang.Double"));
				
				SootClass sootClass = Scene.v().getSootClass("java.lang.Double");
				SootMethod valueOfMethod = sootClass.getMethod("java.lang.Double valueOf(double)");
																
				StaticInvokeExpr staticInvokeExpr = Jimple.v().newStaticInvokeExpr(valueOfMethod.makeRef(), value);
				
				Unit newAssignStmt = Jimple.v().newAssignStmt(doubleLocal, staticInvokeExpr);
				newAssignStmt.addTag(new InstrumentedCodeTag());
				generated.add(newAssignStmt); 
				
				return doubleLocal;
			}
			else if(value.getType() instanceof FloatType){
				Local floatLocal = generateFreshLocal(body, RefType.v("java.lang.Float"));
				
				SootClass sootClass = Scene.v().getSootClass("java.lang.Float");
				SootMethod valueOfMethod = sootClass.getMethod("java.lang.Float valueOf(float)");
				StaticInvokeExpr staticInvokeExpr = Jimple.v().newStaticInvokeExpr(valueOfMethod.makeRef(), value);
				
				Unit newAssignStmt = Jimple.v().newAssignStmt(floatLocal, staticInvokeExpr);
				newAssignStmt.addTag(new InstrumentedCodeTag());
				generated.add(newAssignStmt); 
				
				return floatLocal;
			}
			else if(value.getType() instanceof IntType){
				Local integerLocal = generateFreshLocal(body, RefType.v("java.lang.Integer"));
				
				SootClass sootClass = Scene.v().getSootClass("java.lang.Integer");
				SootMethod valueOfMethod = sootClass.getMethod("java.lang.Integer valueOf(int)");
				StaticInvokeExpr staticInvokeExpr = Jimple.v().newStaticInvokeExpr(valueOfMethod.makeRef(), value);
				
				Unit newAssignStmt = Jimple.v().newAssignStmt(integerLocal, staticInvokeExpr);
				newAssignStmt.addTag(new InstrumentedCodeTag());
				generated.add(newAssignStmt); 
				
				return integerLocal;
			}
			else if(value.getType() instanceof LongType){
				Local longLocal = generateFreshLocal(body, RefType.v("java.lang.Long"));
				
				SootClass sootClass = Scene.v().getSootClass("java.lang.Long");
				SootMethod valueOfMethod = sootClass.getMethod("java.lang.Long valueOf(long)");
				StaticInvokeExpr staticInvokeExpr = Jimple.v().newStaticInvokeExpr(valueOfMethod.makeRef(), value);
				
				Unit newAssignStmt = Jimple.v().newAssignStmt(longLocal, staticInvokeExpr);
				newAssignStmt.addTag(new InstrumentedCodeTag());
				generated.add(newAssignStmt); 
				
				return longLocal;
			}
			else if(value.getType() instanceof ShortType){
				Local shortLocal = generateFreshLocal(body, RefType.v("java.lang.Short"));
				
				SootClass sootClass = Scene.v().getSootClass("java.lang.Short");
				SootMethod valueOfMethod = sootClass.getMethod("java.lang.Short valueOf(short)");
				StaticInvokeExpr staticInvokeExpr = Jimple.v().newStaticInvokeExpr(valueOfMethod.makeRef(), value);
				
				Unit newAssignStmt = Jimple.v().newAssignStmt(shortLocal, staticInvokeExpr);
				newAssignStmt.addTag(new InstrumentedCodeTag());
				generated.add(newAssignStmt); 
				
				return shortLocal;
			}
			else
				throw new RuntimeException("Ooops, something went all wonky!");
		}
		else
			//just return the value, there is nothing to box
			return value;
	}
	
	
	public static Local generateFreshLocal(Body body, Type type){
		LocalGenerator lg = new LocalGenerator(body);
		return lg.generateLocal(type);
	}
	
	
	public static Type getParameterArrayType(){
		Type parameterArrayType = RefType.v("java.lang.Object");
		Type parameterArray = ArrayType.v(parameterArrayType, 1);
		
		return parameterArray;
	}
}
