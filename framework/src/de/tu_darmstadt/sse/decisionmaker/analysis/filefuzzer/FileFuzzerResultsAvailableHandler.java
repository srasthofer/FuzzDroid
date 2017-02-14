package de.tu_darmstadt.sse.decisionmaker.analysis.filefuzzer;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import soot.SootMethod;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.infoflow.handlers.ResultsAvailableHandler;
import soot.jimple.infoflow.results.InfoflowResults;
import soot.jimple.infoflow.results.ResultSinkInfo;
import soot.jimple.infoflow.results.ResultSourceInfo;
import soot.jimple.infoflow.solver.cfg.IInfoflowCFG;
import de.tu_darmstadt.sse.apkspecific.CodeModel.CodePosition;
import de.tu_darmstadt.sse.apkspecific.CodeModel.CodePositionManager;
import de.tu_darmstadt.sse.commandlinelogger.LoggerHelper;
import de.tu_darmstadt.sse.commandlinelogger.MyLevel;
import de.tu_darmstadt.sse.sharedclasses.networkconnection.FileFormat;


public class FileFuzzerResultsAvailableHandler implements ResultsAvailableHandler {
	private final Map<Integer, FileFormat> valuesToFuzz;
	CodePositionManager codePositionManager = CodePositionManager.getCodePositionManagerInstance();
	
	Set<String> PROPERTIES_SINKS = new HashSet<String>() {{
		  add("<java.util.Properties: void load(java.io.InputStream)>"); 			  		
		  add("<java.util.Properties: void store(java.io.OutputStream,java.lang.String)>"); 			  		
	}};
	
	Set<String> DIR_SINKS = new HashSet<String>() {{
		add("<java.io.File: boolean mkdirs()>"); 			  		
		add("<java.io.File: java.io.File[] listFiles()>"); 			  		
		add("<java.io.File: boolean isDirectory()>"); 			  		
	}};
	
	
	public FileFuzzerResultsAvailableHandler(Map<Integer, FileFormat> valuesToFuzz) {
		this.valuesToFuzz = valuesToFuzz;
	}
	
	@Override
	public void onResultsAvailable(IInfoflowCFG cfg, InfoflowResults results) {
		for(ResultSinkInfo sinkInfo : results.getResults().keySet()) {
			//all sources
			Set<ResultSourceInfo> sourceInfos = results.getResults().get(sinkInfo);
			Stmt sinkStmt = sinkInfo.getSink();	
			if(sinkStmt.containsInvokeExpr()) {
				InvokeExpr inv = sinkStmt.getInvokeExpr();
				SootMethod sm = inv.getMethod();
							
				//check for properties files
				if(PROPERTIES_SINKS.contains(sm.getSignature())) {
					for(ResultSourceInfo source : sourceInfos) {
						CodePosition codePos = codePositionManager.getCodePositionForUnit(source.getSource());
						valuesToFuzz.put(codePos.getID(), FileFormat.PROPERTIES);
					}
				}
				//directory
				else if(DIR_SINKS.contains(sm.getSignature())) {
					for(ResultSourceInfo source : sourceInfos) {
						CodePosition codePos = codePositionManager.getCodePositionForUnit(source.getSource());
						valuesToFuzz.put(codePos.getID(), FileFormat.DIRECTORY);
					}
				}
				else
					LoggerHelper.logEvent(MyLevel.TODO, "WE NEED TO ADD A NEW FILE FORMAT: " + sinkInfo);
			}
			else
				throw new RuntimeException("this should not happen in FileFuzzerResultsAvailableHandler");
		}
	}
}