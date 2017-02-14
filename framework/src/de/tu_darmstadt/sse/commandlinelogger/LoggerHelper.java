package de.tu_darmstadt.sse.commandlinelogger;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import de.tu_darmstadt.sse.FrameworkOptions;

public class LoggerHelper {
	private static final Logger log = Logger.getLogger( LoggerHelper.class.getName() );
	private static FileHandler fh = null;
	
	public static void logInfo(String message) {
		log.log(Level.INFO, message);
		if (fh != null)
			fh.flush();
	}
	
	public static void logWarning(String message) {
//		log.log(Level.WARNING, message);
		System.err.println(message);
		if (fh != null)
			fh.flush();
	}
	
	public static void logEvent(Level level, String message) {
		log.log(level, message);
		if (fh != null)
			fh.flush();
	}
	
	public static void initialize(String apkPath) {		
		log.setUseParentHandlers(false);
	    Handler conHdlr = new ConsoleHandler();
	    conHdlr.setFormatter(new Formatter() {
	      public String format(LogRecord record) {
	    	  String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(record.getMillis()));
	    	  StringBuilder sb = new StringBuilder();
	    	  sb.append("[");
	    	  sb.append(timestamp + " - " + record.getLevel());	    	
	    	  sb.append("] ");
	    	  sb.append(record.getMessage());
	    	  sb.append("\n");
	        return sb.toString();
	      }
	    });
	    log.addHandler(conHdlr);
	    
	    //analysis results:	    
		try {
			File resultsDir = new File(FrameworkOptions.resultDir);
			if(!resultsDir.exists())
				resultsDir.mkdir();
			String logFile = String.format("%s/%s.xml", resultsDir.getAbsolutePath(), FrameworkOptions.apkMD5);
			fh = new FileHandler(logFile);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}  
        log.addHandler(fh);
	}
}
