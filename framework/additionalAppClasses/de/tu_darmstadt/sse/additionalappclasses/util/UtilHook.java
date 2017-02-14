package de.tu_darmstadt.sse.additionalappclasses.util;

import java.lang.reflect.Array;
import java.util.HashSet;
import java.util.Set;

import de.tu_darmstadt.sse.additionalappclasses.hookdefinitions.AnalysisDependentHookDefinitions;
import de.tu_darmstadt.sse.additionalappclasses.hookdefinitions.ConditionalHookDefinitions;
import de.tu_darmstadt.sse.additionalappclasses.hookdefinitions.DexFileExtractorHookDefinitions;
import de.tu_darmstadt.sse.additionalappclasses.hookdefinitions.PersistentHookDefinitions;
import de.tu_darmstadt.sse.additionalappclasses.hookdefinitions.SimpleBooleanReturnDefinitions;
import de.tu_darmstadt.sse.additionalappclasses.hooking.DummyValue;
import de.tu_darmstadt.sse.additionalappclasses.hooking.HookInfo;

public class UtilHook {	
	
	public static Object prepareValueForExchange(Object obj) {
		if( obj instanceof String ||
				
			//primitive types	
			obj instanceof Byte ||
			obj instanceof Short ||
			obj instanceof Integer ||	
			obj instanceof Long ||
			obj instanceof Float ||
			obj instanceof Double ||
			obj instanceof Boolean ||
			obj instanceof Character)
			return obj;
		
		else
			return new DummyValue();
	}
	
	
	private static Class getClassType(String paramType) {				
		if(paramType.equals("byte"))
			return byte.class;
		else if(paramType.equals("short"))
			return short.class;
		else if(paramType.equals("int"))
			return int.class;
		else if(paramType.equals("long"))
			return long.class;
		else if(paramType.equals("float"))
			return float.class;
		else if(paramType.equals("double"))
			return double.class;
		else if(paramType.equals("boolean"))
			return boolean.class;
		else if(paramType.equals("char"))
			return char.class;
		else if(paramType.equals("byte[]"))
			return byte[].class;
		else if(paramType.equals("short[]"))
			return short[].class;
		else if(paramType.equals("int[]"))
			return int[].class;
		else if(paramType.equals("long[]"))
			return long[].class;
		else if(paramType.equals("float[]"))
			return float[].class;
		else if(paramType.equals("double[]"))
			return double[].class;
		else if(paramType.equals("boolean[]"))
			return boolean[].class;
		else if(paramType.equals("char[]"))
			return char[].class;
		else {
			try{
				if(paramType.endsWith("[]")) {
					String tmp = paramType.substring(0, paramType.indexOf("[]"));															
					int size = countMatch(paramType, "[]") - 1;
					Class<?> tmpClass = Class.forName(tmp);
					return Array.newInstance(tmpClass, size).getClass();
				}
				return Class.forName(paramType);
			}catch(Exception ex) {				
				ex.printStackTrace();
			}
		}
		throw new RuntimeException("incorrect param-typ");
	}
	
	
	public static Class<?>[] getClassTypes(String[] paramTypes) {
		int amountOfParams = paramTypes.length;
		Class<?>[] classParamTypes = new Class[amountOfParams];
		for(int i = 0; i < amountOfParams; i++) {
			classParamTypes[i] = getClassType(paramTypes[i].trim());
		}
		return classParamTypes;
	}
	
	private static int countMatch(String string, String findStr) {
		int lastIndex = 0;
	    int count = 0;

	    while ((lastIndex = string.indexOf(findStr, lastIndex)) != -1) {
	        count++;
	        lastIndex += findStr.length() - 1;
	    }	    
	    return count;
	}
	
	
	public static Set<HookInfo> initAllHookers() {
		Set<HookInfo> allHookInfos = new HashSet<HookInfo>();
		allHookInfos.addAll(new AnalysisDependentHookDefinitions().initializeHooks());
		allHookInfos.addAll(new DexFileExtractorHookDefinitions().initializeHooks());
		allHookInfos.addAll(new SimpleBooleanReturnDefinitions().initializeHooks());
		allHookInfos.addAll(new ConditionalHookDefinitions().initializeHooks());
		allHookInfos.addAll(new PersistentHookDefinitions().initializeHooks());
		return allHookInfos;
	}
}
