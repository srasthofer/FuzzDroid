package de.tu_darmstadt.sse.decisionmaker.analysis.dynamicValues;



public class DynamicStringValue extends DynamicValue{
	private final String stringValue;
	private final int codePosition;
	private final int paramIdx;
	
	public DynamicStringValue(int codePosition, int paramIdx, String stringValue) {
		super(codePosition, paramIdx);
		this.stringValue = stringValue;
		this.codePosition = codePosition;
		this.paramIdx = paramIdx;
	}

	
	public String getStringValue() {
		return this.stringValue;
	}
	
	public String toString() {
		return stringValue;
	}
	
	@Override
	public int hashCode() {
		int code = 31;
		code += codePosition;
		code += paramIdx;
		code += stringValue.hashCode();
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
		DynamicStringValue other = (DynamicStringValue) obj;
		if(other.codePosition != codePosition)
			return false;
		if(other.paramIdx != paramIdx)
			return false;
		if(!other.stringValue.equals(stringValue))
			return false;
		return true;
	}
}
