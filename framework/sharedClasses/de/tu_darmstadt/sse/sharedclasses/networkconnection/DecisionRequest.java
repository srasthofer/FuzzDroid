package de.tu_darmstadt.sse.sharedclasses.networkconnection;

import java.util.Arrays;



public class DecisionRequest implements IClientRequest, Cloneable {
	private static final long serialVersionUID = 5206760883356756113L;
	
	
	private final int codePosition;
	
	
	private final String loggingPointSignature;
	
	
	private final boolean hookAfter;
	
	
	private Object runtimeValueOfReturn;
	
	
	private Object[] runtimeValuesOfParams;
	
	public DecisionRequest(int codePosition, String loggingPointSignature, boolean hookAfter) {
		this.codePosition = codePosition;
		this.loggingPointSignature = loggingPointSignature;
		this.hookAfter = hookAfter;
	}
	
	
	private DecisionRequest(DecisionRequest original) {
		this.codePosition = original.codePosition;
		this.loggingPointSignature = original.loggingPointSignature;
		this.hookAfter = original.hookAfter;
		this.runtimeValueOfReturn = original.runtimeValueOfReturn;
		this.runtimeValuesOfParams = original.runtimeValuesOfParams;
	}
			
	public boolean isHookAfter() {
		return hookAfter;
	}

	public String getLoggingPointSignature() {
		return loggingPointSignature;
	}

	public Object getRuntimeValueOfReturn() {
		return runtimeValueOfReturn;
	}

	public void setRuntimeValueOfReturn(Object runtimeValueOfReturn) {
		this.runtimeValueOfReturn = runtimeValueOfReturn;
	}		

	public Object[] getRuntimeValuesOfParams() {
		return runtimeValuesOfParams;
	}

	public void setRuntimeValuesOfParams(Object[] runtimeValuesOfParams) {
		this.runtimeValuesOfParams = runtimeValuesOfParams;
	}

	public int getCodePosition() {
		return codePosition;
	}		
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("[HOOK]%s\n", this.loggingPointSignature));
		sb.append(String.format("\t codeposition=%d\n", this.codePosition));
		sb.append(String.format("\t hookAfter=%s\n", (this.hookAfter == true)? "true" : "false"));
		if(hookAfter)
			sb.append(String.format("\t runtime value of return value: %s\n", this.runtimeValueOfReturn));
		else {
			String params = "[";
			for(int i = 0; i < this.runtimeValuesOfParams.length; i++) {
				if(i != this.runtimeValuesOfParams.length - 1)
					params += this.runtimeValuesOfParams[i] + ", ";
				else
					params += this.runtimeValuesOfParams[i];
			}
			params += "]";
			sb.append(String.format("\t runtime values of params: %s\n", params));
		}
		return sb.toString();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DecisionRequest other = (DecisionRequest) obj;
		if (codePosition != other.codePosition)
			return false;
		if (hookAfter != other.hookAfter)
			return false;
		if (loggingPointSignature == null) {
			if (other.loggingPointSignature != null)
				return false;
		} else if (!loggingPointSignature.equals(other.loggingPointSignature))
			return false;
		if (runtimeValueOfReturn == null) {
			if (other.runtimeValueOfReturn != null)
				return false;
		} else if (!runtimeValueOfReturn.equals(other.runtimeValueOfReturn))
			return false;
		if (!Arrays.equals(runtimeValuesOfParams, other.runtimeValuesOfParams))
			return false;
		return true;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + codePosition;
		result = prime * result + (hookAfter ? 1231 : 1237);
		result = prime
				* result
				+ ((loggingPointSignature == null) ? 0 : loggingPointSignature
						.hashCode());
		result = prime
				* result
				+ ((runtimeValueOfReturn == null) ? 0 : runtimeValueOfReturn
						.hashCode());
		result = prime * result + Arrays.hashCode(runtimeValuesOfParams);
		return result;
	}
	
	@Override
	public DecisionRequest clone() {
		return new DecisionRequest(this);
	}
	
}
