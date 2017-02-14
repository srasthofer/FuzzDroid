package de.tu_darmstadt.sse.additionalappclasses.pathexecution;

import java.util.Arrays;

import android.util.Log;
import de.tu_darmstadt.sse.additionalappclasses.tracing.BytecodeLogger;
import de.tu_darmstadt.sse.sharedclasses.SharedClassesSettings;


public class PathExecutor {	
	
	public static void logInfoAboutNonApiMethodAccess(String methodSignature) {
		long codePosition = BytecodeLogger.getLastExecutedStatement();
		Log.i(SharedClassesSettings.TAG, String.format("%s || CodePos: %d || Method-Sign: %s || method access", SharedClassesSettings.METHOD_CALLEE_LABEL, codePosition, methodSignature));
	}
	
	
	public static void logInfoAboutReturnStatement(String methodSignature, Object returnValue ) {
		long codePosition = BytecodeLogger.getLastExecutedStatement();
		Log.i(SharedClassesSettings.TAG, String.format("%s || CodePos: %d || Method-Sign: %s || return %s", SharedClassesSettings.RETURN_LABEL, codePosition, methodSignature, concreteParameterValue(returnValue)));
	}
	
	
	public static void logInfoAboutReturnStatement(String methodSignature) {
		logInfoAboutReturnStatement(methodSignature, "");
	}
	
	
	public static void logInfoAboutNonApiMethodCaller(String methodSignature, String invokeExprMethodSignature, Object... parameter)
	{
		long codePosition = BytecodeLogger.getLastExecutedStatement();
		String invokeExprInfo = invokeExprMethodSignature + "(";
		
		if(parameter != null){
			for(int i = 0; i < parameter.length; i++){
				if(i < parameter.length-1){
					if(parameter[i] != null)
						invokeExprInfo += concreteParameterValue(parameter[i]) + ", ";
					else
						invokeExprInfo += "null, ";
				}
				else{
					if(parameter[i] != null)
						invokeExprInfo += concreteParameterValue(parameter[i]);
					else
						invokeExprInfo += "null";
				}
					
			}
		}
		invokeExprInfo += ")";
		
		Log.i(SharedClassesSettings.TAG, String.format("%s || CodePos: %d || Method-Sign: %s || InvokeExpr: %s", SharedClassesSettings.METHOD_CALLER_LABEL, codePosition, methodSignature, invokeExprInfo));
	}
	
	
	public static void logInfoAboutBranchAccess(String methodSignature, String condition, String branchInfo) {
		long codePosition = BytecodeLogger.getLastExecutedStatement();
		//before branch-condition
		if(branchInfo == null && condition != null)
			Log.i(SharedClassesSettings.TAG, String.format("[branch access condition] || CodePos: %d || Method-Sign: %s || %s", codePosition, methodSignature, condition));
		else if(condition == null && branchInfo != null)
			Log.i(SharedClassesSettings.TAG, String.format("[branch access decision] || CodePos: %d || Method-Sign: %s || %s",codePosition, methodSignature, branchInfo));
		else
			throw new RuntimeException("there is a issue with the logInfoAboutBranchAccess method");
	}
	
	
	private static String concreteParameterValue(Object object){
		if(object == null)	
			return "null";
		else if(object instanceof String[])
			return Arrays.deepToString((String[]) object);
		else if(object instanceof String)
			return object.toString();
		else if(object instanceof boolean[])
			return Arrays.toString((boolean[]) object);
		else if(object instanceof byte[]){
			String byteArray = Arrays.toString((byte[]) object);
			String byteArrayToString = null;
			try{
				byteArrayToString = new String((byte[])object);
			}catch(Exception ex){
				//do nothing
			}
			if(byteArrayToString == null)
				return byteArray;
			else
				return byteArray + "\n" + byteArrayToString;
		}
		else if(object instanceof byte[][])
			return Arrays.toString((byte[][]) object);
		else if(object instanceof short[])
			return Arrays.toString((short[]) object);
		else if(object instanceof char[])
			return Arrays.toString((char[]) object);
		else if(object instanceof int[])
			return Arrays.toString((int[]) object);
		else if(object instanceof long[])
			return Arrays.toString((long[]) object);
		else if(object instanceof float[])
			return Arrays.toString((float[]) object);
		else if(object instanceof double[])
			return Arrays.toString((double[]) object);
		else if(object instanceof Object[])
			return Arrays.deepToString((Object[]) object);
		else
			return object.toString();
	}
}
