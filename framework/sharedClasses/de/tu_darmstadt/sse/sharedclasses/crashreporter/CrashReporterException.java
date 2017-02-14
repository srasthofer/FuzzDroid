package de.tu_darmstadt.sse.sharedclasses.crashreporter;


public class CrashReporterException extends Exception {
	
	
	private static final long serialVersionUID = 1047994013094918155L;
	
	private final String originalClassName;
	
	
	public CrashReporterException(Throwable originalException) {
		super(originalException.getMessage());
		this.originalClassName = originalException.getClass().getName();
		setStackTrace(originalException.getStackTrace());
	}
	
	
	CrashReporterException(String message, String className,
			StackTraceElement[] stackTrace) {
		super(message);
		this.originalClassName = className;
		setStackTrace(stackTrace);
	}
	
	public String getOriginalClassName() {
		return this.originalClassName;
	}

}
