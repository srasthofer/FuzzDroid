package de.tu_darmstadt.sse.decisionmaker.analysis.symbolicexecution.datastructure;


public class SMTBinding{
	private final String variableName;
	private final TYPE type;
	private int version;	
	
	public enum TYPE {String, Int, Bool, Real};
	
	public SMTBinding(String variableName, SMTBinding.TYPE type, int version) {
		this.variableName = variableName;
		this.type = type;
		this.version = version;
	}
	
	public SMTBinding(String variableName, SMTBinding.TYPE type) {
		this.variableName = variableName;
		this.type = type;
		this.version = 0;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public String getVariableName() {
		return variableName;
	}

	public int getVersion() {
		return version;
	}
	
	public TYPE getType() {
		return type;
	}
	
	public String getBinding() {
		return String.format("%s_%d", variableName, version);
	}

	public String toString() {
		return String.format("%s %s_%d", type.name(), variableName, version);
	}
}
