package de.tu_darmstadt.sse.sharedclasses.networkconnection;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import de.tu_darmstadt.sse.sharedclasses.util.Pair;


public class ServerResponse implements Serializable, Cloneable {	
	private static final long serialVersionUID = 5488569934511264853L;
	
	private boolean responseExist;
	
	private Object returnValue;
	
	private Set<Pair<Integer, Object>>  paramValues;
	
	private String analysisName;
	
	
	public ServerResponse() {
		
	}
	
	
	private ServerResponse(ServerResponse original) {
		this.responseExist = original.responseExist;
		this.returnValue = original.returnValue;
		this.paramValues = original.paramValues == null ? null : new HashSet<>(original.paramValues);
	}
	
	public void setResponseExist(boolean responseExist) {
		this.responseExist = responseExist;
	}

	public Object getReturnValue() {
		return returnValue;
	}

	public void setReturnValue(Object returnValue) {
		this.returnValue = returnValue;
	}

	public Set<Pair<Integer, Object>>  getParamValues() {
		return paramValues;
	}

	public void setParamValues(Set<Pair<Integer, Object>>  paramValues) {
		this.paramValues = paramValues;
	}

	public boolean doesResponseExist() {
		return responseExist;
	}		
	
	public String getAnalysisName() {
		return analysisName;
	}

	public void setAnalysisName(String analysisName) {
		this.analysisName = analysisName;
	}

	@Override
	public String toString() {
		if(!responseExist)
			return "[NO VALUE AVAILABLE]";
		else {
			String response = "[NEW VALUE] ";
			if(returnValue != null)
				response += returnValue;
			else {
				if(paramValues != null) {
					for(Pair<Integer, Object> pair : paramValues) 
						response += String.format("\n\t param %d = %s", pair.getFirst(), pair.getSecond());
				}
				else
					throw new RuntimeException("the response has to contain either a value for the parameter or return value");
			}
			response += String.format(" [ANALYSIS_NAME] %s", this.getAnalysisName());
			return response;
		}
	}
	
	
	public static ServerResponse getEmptyResponse() {
		ServerResponse response = new ServerResponse();
		response.responseExist = false;
		return response;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((paramValues == null) ? 0 : paramValues.hashCode());
		result = prime * result + (responseExist ? 1231 : 1237);
		result = prime * result
				+ ((returnValue == null) ? 0 : returnValue.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ServerResponse other = (ServerResponse) obj;
		if (paramValues == null) {
			if (other.paramValues != null)
				return false;
		} else if (!paramValues.equals(other.paramValues))
			return false;
		if (responseExist != other.responseExist)
			return false;
		if (returnValue == null) {
			if (other.returnValue != null)
				return false;
		} else if (!returnValue.equals(other.returnValue))
			return false;
		return true;
	}
	
	@Override
	public ServerResponse clone() {
		return new ServerResponse(this);
	}
	
}
