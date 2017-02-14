package de.tu_darmstadt.sse.additionalappclasses.hooking;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.tu_darmstadt.sse.additionalappclasses.util.UtilHook;
import de.tu_darmstadt.sse.sharedclasses.util.Pair;


public class MethodHookInfo implements HookInfo{	
	private final String className;
	private final String methodName;
	private final Class<?>[] params;
	
	private List<AbstractMethodHookBefore> beforeHook = null;
	private List<AbstractMethodHookAfter> afterHook = null;
	
	private final String methodSignature;
	
	public MethodHookInfo(String methodSignature) {
		this.methodSignature = methodSignature;
		this.className = getClassName(methodSignature);
		this.methodName = getMethodName(methodSignature);
		this.params = getParams(methodSignature);
	}	

	private String getClassName(String methodSignature) {
	      String pattern = "<(.*):.*>";
	      Pattern r = Pattern.compile(pattern);

	      Matcher m = r.matcher(methodSignature);
	      if (m.find()) {
	         return m.group(1);
	      } else {
	         throw new RuntimeException("wrong format for className");
	      }
	}
	
	private String getMethodName(String methodSignature) {
		String pattern = "<.*\\s(.*)\\(.*>";
		Pattern r = Pattern.compile(pattern);

		Matcher m = r.matcher(methodSignature);
		if (m.find()) {
			return m.group(1);
		} else {
			throw new RuntimeException("wrong format for className");
		}
	}
	
	private Class<?>[] getParams(String methodSignature) {
		String pattern = "<.*\\((.*)\\)>";
		Pattern r = Pattern.compile(pattern);

		Matcher m = r.matcher(methodSignature);
		if (m.find()) {
			String allParamTypes = m.group(1);
			//no params
			if(allParamTypes.equals(""))
				return null;
			else {
				String[] classTypes = allParamTypes.split(",");
				return UtilHook.getClassTypes(classTypes);
			}
				
		} else {
			throw new RuntimeException("wrong format for param-type");
		}
	}
	
	public void persistentHookBefore(Set<Pair<Integer, Object>> pairs) {
		if(this.beforeHook == null)
			this.beforeHook = new ArrayList<AbstractMethodHookBefore>();
		this.beforeHook.add(new PersistentMethodHookBefore(pairs));
	}
	
	public void analysisDependentHookBefore() {
		if(this.beforeHook == null)
			this.beforeHook = new ArrayList<AbstractMethodHookBefore>();
		this.beforeHook.add(new AnalysisDependentMethodHookBefore(this.methodSignature));
	}
	
	public void conditionDependentHookBefore(Set<ParameterConditionValueInfo> paramConditions) {
		if(this.beforeHook == null)
			this.beforeHook = new ArrayList<AbstractMethodHookBefore>();
		this.beforeHook.add(new ConditionalMethodHookBefore(paramConditions));
	}
	
	public void persistentHookAfter(Object returnValue) {
		if(this.afterHook == null)
			this.afterHook = new ArrayList<AbstractMethodHookAfter>();
		this.afterHook.add(new PersistentMethodHookAfter(returnValue));
	}
	
	public void analysisDependentHookAfter() {
		if(this.afterHook == null)
			this.afterHook = new ArrayList<AbstractMethodHookAfter>();
		this.afterHook.add(new AnalysisDependentMethodHookAfter(this.methodSignature));
	}	
	
	public void dexFileExtractorHookBefore(int argumentPosition) {
		if(this.beforeHook == null)
			this.beforeHook = new ArrayList<AbstractMethodHookBefore>();
		this.beforeHook.add(new DexFileExtractorHookBefore(this.methodSignature, argumentPosition));
	}
	
	public void simpleBooleanHookAfter() {
		if(this.afterHook == null)
			this.afterHook = new ArrayList<AbstractMethodHookAfter>();
		this.afterHook.add(new SimpleBooleanHookAfter(this.methodSignature));
	}
	
	public void conditionDependentHookAfter(Condition condition, Object returnValue) {
		if(this.afterHook == null)
			this.afterHook = new ArrayList<AbstractMethodHookAfter>();
		this.afterHook.add(new ConditionalMethodHookAfter(condition, returnValue));
	}
	
	public boolean hasHookBefore() {
		return beforeHook != null;
	}
	
	public boolean hasHookAfter() {
		return afterHook != null;
	}

	public List<AbstractMethodHookBefore> getBeforeHooks() {
		return beforeHook;
	}

	public List<AbstractMethodHookAfter> getAfterHooks() {
		return afterHook;
	}

	public String getClassName() {
		return className;
	}

	public String getMethodName() {
		return methodName;
	}

	public Class<?>[] getParams() {
		return params;
	}
}
