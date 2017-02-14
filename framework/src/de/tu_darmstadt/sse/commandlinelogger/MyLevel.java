package de.tu_darmstadt.sse.commandlinelogger;

import java.util.logging.Level;

public class MyLevel extends Level{
	public static final Level RUNTIME = new MyLevel("RUNTIME", Level.SEVERE.intValue() + 1);
	public static final Level ANALYSIS = new MyLevel("ANALYSIS", Level.SEVERE.intValue() + 1);
	public static final Level EXCEPTION_RUNTIME = new MyLevel("EXCEPTION_RUNTIME", Level.SEVERE.intValue() + 1);
	public static final Level EXCEPTION_ANALYSIS = new MyLevel("EXCEPTION_ANALYSIS", Level.SEVERE.intValue() + 1);
	public static final Level APKPATH = new MyLevel("APKPATH", Level.SEVERE.intValue() + 1);
	public static final Level LOGGING_POINT = new MyLevel("LOGGING_POINT", Level.SEVERE.intValue() + 1);
	public static final Level LOGGING_POINT_REACHED = new MyLevel("LOGGING_POINT_REACHED", Level.SEVERE.intValue() + 1);
	public static final Level DECISION_REQUEST_AND_RESPONSE = new MyLevel("DECISION_REQUEST_AND_RESPONSE", Level.SEVERE.intValue() + 1);
	public static final Level REBOOT = new MyLevel("REBOOT", Level.SEVERE.intValue() + 1);
	public static final Level PRE_ANALYSIS_START = new MyLevel("PRE_ANALYSIS_START", Level.SEVERE.intValue() + 1);
	public static final Level PRE_ANALYSIS_STOP = new MyLevel("PRE_ANALYSIS_STOP", Level.SEVERE.intValue() + 1);
	public static final Level EXECUTION_START = new MyLevel("EXECUTION_START", Level.SEVERE.intValue() + 1);
	public static final Level EXECUTION_STOP = new MyLevel("EXECUTION_STOP", Level.SEVERE.intValue() + 1);
	public static final Level TODO = new MyLevel("TODO", Level.SEVERE.intValue() + 1);
	public static final Level ADB_EVENT = new MyLevel("ADB_EVENT", Level.SEVERE.intValue() + 1);
	public static final Level VMCRASH = new MyLevel("VMCRASH", Level.SEVERE.intValue() + 1);
	public static final Level NO_TARGETS = new MyLevel("NO_TARGETS", Level.SEVERE.intValue() + 1);
	public static final Level OPEN_APK = new MyLevel("OPEN_APK", Level.SEVERE.intValue() + 1);
	public static final Level INSTRUMENTATION_START = new MyLevel("INSTRUMENTATION_START", Level.SEVERE.intValue() + 1);
	public static final Level INSTRUMENTATION_STOP = new MyLevel("INSTRUMENTATION_STOP", Level.SEVERE.intValue() + 1);
	public static final Level START_ACTIVITY = new MyLevel("START_ACTIVITY", Level.SEVERE.intValue() + 1);
	public static final Level ANALYSIS_NAME = new MyLevel("ANALYSIS_NAME", Level.SEVERE.intValue() + 1);
	public static final Level RESTART = new MyLevel("APPANALYSIS_RESTART", Level.SEVERE.intValue() + 1);
	public static final Level TIMEOUT = new MyLevel("TIMEOUT", Level.SEVERE.intValue() + 1);
	public static final Level TIMING_BOMB = new MyLevel("TIMING_BOMB", Level.SEVERE.intValue() + 1);
	public static final Level SMT_SOLVER_VALUE = new MyLevel("SMT_SOLVER_VALUE", Level.SEVERE.intValue() + 1);
	public static final Level GENTETIC_ONLY_MODE = new MyLevel("GENTETIC_ONLY_MODE", Level.SEVERE.intValue() + 1);
	public static final Level DEXFILE = new MyLevel("DEXFILE", Level.SEVERE.intValue() + 1);

	  public MyLevel(String name, int value) {
	    super(name, value);
	  }
}
