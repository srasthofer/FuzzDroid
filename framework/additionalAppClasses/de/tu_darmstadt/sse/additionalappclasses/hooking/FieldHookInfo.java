package de.tu_darmstadt.sse.additionalappclasses.hooking;

import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class FieldHookInfo implements HookInfo{
	private final String fieldSignature;
	private final String className;
	private final String fieldName;
	
	private AbstractFieldHookAfter afterHook = null;
	
	public FieldHookInfo(String fieldSignature) {
		this.fieldSignature = fieldSignature;
		this.className = getClassNameFromSignature();
		this.fieldName = getFieldNameFromSignature();
	}
	
	private String getClassNameFromSignature() {
		String pattern = "<(.*):.*>";
	      Pattern r = Pattern.compile(pattern);

	      Matcher m = r.matcher(fieldSignature);
	      if (m.find()) {
	         return m.group(1);
	      } else {
	         throw new RuntimeException("wrong format for className");
	      }
	}
	
	private String getFieldNameFromSignature() {
		String pattern = "<.*\\s(.*)>";
		Pattern r = Pattern.compile(pattern);

		Matcher m = r.matcher(fieldSignature);
		if (m.find()) {
			return m.group(1);
		} else {
			throw new RuntimeException("wrong format for className");
		}
	}
	
	public void persistentHookAfter(Object fieldValue) {
		this.afterHook = new PersistentFieldHookAfter(fieldValue);
	}
	
	public String getClassName() {
		return className;
	}

	public String getFieldName() {
		return fieldName;
	}

	public AbstractFieldHookAfter getAfterHook() {
		return afterHook;
	}		
}
