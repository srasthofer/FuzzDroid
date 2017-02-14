package de.tu_darmstadt.sse.decisionmaker.analysis.dynamicValues;


public class DynamicIntValue extends DynamicValue{
	private final int intValue;
	private final int codePosition;
	private final int paramIdx;
	
	public DynamicIntValue(int codePosition, int paramIdx, int intValue) {
		super(codePosition, paramIdx);
		this.intValue = intValue;
		this.codePosition = codePosition;
		this.paramIdx = paramIdx;
	}

	
	public int getIntValue() {
		return this.intValue;
	}
	
	public String toString() {
		return intValue+"";
	}
	
	@Override
	public int hashCode() {
		int code = 31;
		code += codePosition;
		code += paramIdx;
		code += intValue;
		return code;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DynamicIntValue other = (DynamicIntValue) obj;
		if(other.codePosition != codePosition)
			return false;
		if(other.paramIdx != paramIdx)
			return false;
		if(other.intValue != intValue)
			return false;
		return true;
	}
}
