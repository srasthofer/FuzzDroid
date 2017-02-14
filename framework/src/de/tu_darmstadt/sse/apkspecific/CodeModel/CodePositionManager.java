package de.tu_darmstadt.sse.apkspecific.CodeModel;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import soot.Unit;


public class CodePositionManager {
	
	private Map<String, Integer> methodToLastCodePosition = new HashMap<>();
	private Map<Unit, CodePosition> unitToCodePosition = new HashMap<>();
	private Map<CodePosition, Unit> codePositionToUnit = new HashMap<>();
	private Map<Integer, CodePosition> idToCodePosition = new HashMap<>();
	
	private final static int methodOffsetMultiplier = 1000;
	private Map<String, Integer> methodToOffset = new HashMap<>();
	private int lastOffset = 1;
	private static CodePositionManager codePositionManager = null;
		
	private CodePositionManager() {
		//
	}
	
	public static CodePositionManager getCodePositionManagerInstance() {
		if(codePositionManager == null)
			codePositionManager = new CodePositionManager();
		return codePositionManager;
	}
	
	
	public CodePosition getCodePositionForUnit(Unit u) {
		return unitToCodePosition.get(u);
	}

	
	public CodePosition getCodePositionForUnit(Unit u, String methodSignature,
			int lineNumber, int sourceLineNumber) {
		CodePosition codePos = unitToCodePosition.get(u);
		if (codePos == null) {
			int offset = getMethodOffset(methodSignature);
			int lastCodePos = getAndIncrementLastCodePosition(methodSignature);
			
			codePos = new CodePosition(offset + lastCodePos, methodSignature,
					lineNumber, sourceLineNumber);
			unitToCodePosition.put(u, codePos);
			codePositionToUnit.put(codePos, u);
			idToCodePosition.put(codePos.getID(), codePos);
		}
		return codePos;
	}
	
	
	private int getAndIncrementLastCodePosition(String methodSignature) {
		Integer lastPos = this.methodToLastCodePosition.get(methodSignature);
		if (lastPos == null)
			lastPos = 0;
		else
			lastPos++;
		this.methodToLastCodePosition.put(methodSignature, lastPos);
		return lastPos;
	}
	
	
	public int getMethodOffset(String methodSignature) {
		Integer offset = this.methodToOffset.get(methodSignature);
		if (offset == null) {
			offset = lastOffset++ * methodOffsetMultiplier;
			this.methodToOffset.put(methodSignature, offset);
		}
		return offset;
	}
	
	
	public Set<String> getMethodsWithCodePositions() {
		return methodToOffset.keySet();
	}

	
	public Unit getUnitForCodePosition(CodePosition cp) {
		return codePositionToUnit.get(cp);
	}
	
	
	public Unit getUnitForCodePosition(int id) {
		CodePosition cp = idToCodePosition.get(id);
		if (cp == null)
			return null;
		return codePositionToUnit.get(cp);
	}
	
	
	public CodePosition getCodePositionByID(int id) {
		return idToCodePosition.get(id);
	}

}
