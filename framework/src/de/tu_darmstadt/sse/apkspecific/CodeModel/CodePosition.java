package de.tu_darmstadt.sse.apkspecific.CodeModel;


public class CodePosition {
	
	private int id = -1;
	private String enclosingMethod = null;
	private int lineNumber = -1;
	private int sourceLineNumber = -1;
	
	public CodePosition(int id, String enclosingMethod, int lineNumber,
			int sourceLineNumber) {
		this.id = id;
		this.enclosingMethod = enclosingMethod;
		this.lineNumber = lineNumber;
		this.sourceLineNumber = sourceLineNumber;
	}
	
	public int getID() {
		return this.id;
	}
	
	public String getEnclosingMethod() {
		return this.enclosingMethod;
	}
	
	public int getLineNumber() {
		return this.lineNumber;
	}
	
	public int getSourceLineNumber() {
		return this.sourceLineNumber;
	}

}
