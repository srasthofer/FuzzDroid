package de.tu_darmstadt.sse.decisionmaker.analysis.filefuzzer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.Scene;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.infoflow.Infoflow;
import soot.jimple.infoflow.android.data.parsers.PermissionMethodParser;
import soot.jimple.infoflow.android.source.AccessPathBasedSourceSinkManager;
import soot.jimple.infoflow.data.pathBuilders.DefaultPathBuilderFactory;
import soot.jimple.infoflow.data.pathBuilders.DefaultPathBuilderFactory.PathBuilder;
import soot.jimple.infoflow.source.ISourceSinkManager;
import soot.jimple.infoflow.taintWrappers.EasyTaintWrapper;
import de.tu_darmstadt.sse.FrameworkOptions;
import de.tu_darmstadt.sse.apkspecific.CodeModel.CodePositionManager;
import de.tu_darmstadt.sse.decisionmaker.analysis.AnalysisDecision;
import de.tu_darmstadt.sse.decisionmaker.analysis.FuzzyAnalysis;
import de.tu_darmstadt.sse.decisionmaker.server.ThreadTraceManager;
import de.tu_darmstadt.sse.decisionmaker.server.TraceManager;
import de.tu_darmstadt.sse.sharedclasses.networkconnection.DecisionRequest;
import de.tu_darmstadt.sse.sharedclasses.networkconnection.FileFormat;
import de.tu_darmstadt.sse.sharedclasses.networkconnection.ServerResponse;
import de.tu_darmstadt.sse.sharedclasses.networkconnection.serializables.FileFuzzingSerializableObject;
import de.tu_darmstadt.sse.sharedclasses.util.Pair;


public class FileFuzzer extends FuzzyAnalysis{
	CodePositionManager codePositionManager = CodePositionManager.getCodePositionManagerInstance();
	private final static String TAINT_WRAPPER_PATH = FrameworkOptions.frameworkDir + "/src/de/tu_darmstadt/sse/decisionmaker/analysis/EasyTaintWrapperSource.txt";
	private static final String SOURCES_SINKS_FILE = FrameworkOptions.frameworkDir + "/src/de/tu_darmstadt/sse/decisionmaker/analysis/filefuzzer/SourcesAndSinks.txt";
	public static final String FUZZY_FILES_DIR = FrameworkOptions.frameworkDir + "/fuzzyFiles/";
	
	//code position to file format map
	private Map<Integer, FileFormat> fileFormatsFromDataflow = new HashMap<Integer, FileFormat>();
	
	@Override
	public void doPreAnalysis(Set<Unit> targetUnits, TraceManager traceManager) {
		runDataflowAnalysis();
	}

	@Override
	public List<AnalysisDecision> resolveRequest(DecisionRequest clientRequest,
			ThreadTraceManager completeHistory) {
		List<AnalysisDecision> decisions = new ArrayList<AnalysisDecision>();
		
		int codePosID = clientRequest.getCodePosition();
		//we have to add one to it
		codePosID += 1;
		
		//decision available: concrete file format
		if(fileFormatsFromDataflow.keySet().contains(codePosID)) {
			//we always add an event for "no action" to it, since it can be the
			//case that the program will add the file at a later stage or it is not required
			//to create a file
			ServerResponse response = new ServerResponse();			
	        response.setResponseExist(false);
	        response.setAnalysisName(getAnalysisName());
			AnalysisDecision finalDecision = new AnalysisDecision();
			finalDecision.setAnalysisName(getAnalysisName());
			finalDecision.setDecisionWeight(8);
		    finalDecision.setServerResponse(response);		        
		    decisions.add(finalDecision);
						
			AnalysisDecision decision = getFileFormatFromDataflow(codePosID);
			decision.setAnalysisName(getAnalysisName());
			decisions.add(decision);
		}
		//decision available: text-based file format
		else if(fileFormatAvailable(codePosID)) {
			AnalysisDecision decision = getFileFormat(codePosID);
			decision.setAnalysisName(getAnalysisName());
			decisions.add(decision);
		}
		//no decision available
		else {
			ServerResponse response = new ServerResponse();	
			response.setAnalysisName(getAnalysisName());
	        response.setResponseExist(false);
			AnalysisDecision finalDecision = new AnalysisDecision();
			finalDecision.setDecisionWeight(8);
		    finalDecision.setServerResponse(response);		        
		    decisions.add(finalDecision);
		}				
		
		return decisions;		
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		
	}
	
	public String getAnalysisName() {
		return "FileFuzzer"; 
	}
	
	private void runDataflowAnalysis() {
		try{
			Scene.v().getOrMakeFastHierarchy();
			
			InplaceInfoflow infoflow = new InplaceInfoflow();
//			InfoflowConfiguration.setAccessPathLength(2);
			infoflow.setPathBuilderFactory(new DefaultPathBuilderFactory(
					PathBuilder.ContextSensitive, true));
			infoflow.setTaintWrapper(new EasyTaintWrapper(TAINT_WRAPPER_PATH));
			infoflow.getConfig().setEnableExceptionTracking(false);
			infoflow.getConfig().setEnableArraySizeTainting(false);
//			infoflow.getConfig().setCallgraphAlgorithm(CallgraphAlgorithm.CHA);
			
			System.out.println("Running data flow analysis...");
			PermissionMethodParser pmp = PermissionMethodParser.fromFile(SOURCES_SINKS_FILE);
			AccessPathBasedSourceSinkManager srcSinkManager =
					new AccessPathBasedSourceSinkManager(pmp.getSources(), pmp.getSinks());
			
			infoflow.addResultsAvailableHandler(new FileFuzzerResultsAvailableHandler(fileFormatsFromDataflow));
			infoflow.runAnalysis(srcSinkManager);
		}catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	
	private AnalysisDecision getFileFormatFromDataflow(int codePosID ) {
		Unit unit = codePositionManager.getUnitForCodePosition(codePosID);
		if(unit instanceof Stmt) {		
			Stmt stmt = (Stmt)unit;
			if(stmt.containsInvokeExpr()) {
				InvokeExpr inv = stmt.getInvokeExpr();
				SootMethod sm = inv.getMethod();
				Pair<Integer, Object> paramValue = retrieveCorrectFileInformation(sm);
								
				ServerResponse response = new ServerResponse();
				response.setAnalysisName(getAnalysisName());
		        response.setResponseExist(true);      
		        response.setParamValues(Collections.singleton(paramValue));
				AnalysisDecision finalDecision = new AnalysisDecision();
				finalDecision.setAnalysisName(getAnalysisName());
				finalDecision.setDecisionWeight(8);
			    finalDecision.setServerResponse(response);		    
			    return finalDecision;
			}
			else
				return noResults();
		}
		else {
			return noResults();
		}
	}
	
	
	private Pair<Integer, Object> retrieveCorrectFileInformation(SootMethod sm) {
		//property files
		if(sm.getSubSignature().equals("java.io.FileInputStream openFileInput(java.lang.String)"))
			return new Pair<Integer, Object>(0, new FileFuzzingSerializableObject(FileFormat.PROPERTIES, 0));
				
		return null;
	}

	
	private class InplaceInfoflow extends Infoflow {
		
		public InplaceInfoflow() {
			super();
		}

		@Override
		public void runAnalysis(final ISourceSinkManager sourcesSinks) {
			super.runAnalysis(sourcesSinks);
		}		
	}	
	
	
	private AnalysisDecision noResults() {
		ServerResponse response = new ServerResponse();
		response.setAnalysisName(getAnalysisName());
        response.setResponseExist(false);
		AnalysisDecision finalDecision = new AnalysisDecision();
		finalDecision.setDecisionWeight(8);
	    finalDecision.setServerResponse(response);		    
	    return finalDecision;
	}
	
	
	private boolean fileFormatAvailable(int codePosID){
		Unit unit = codePositionManager.getUnitForCodePosition(codePosID);
		if(unit instanceof Stmt) {
			Stmt stmt = (Stmt)unit;
			if(stmt.containsInvokeExpr()) {
				InvokeExpr inv = stmt.getInvokeExpr();
				SootMethod sm = inv.getMethod();
				String methodSig = sm.getSignature();			

				switch(methodSig) {
					case "<android.content.Context: java.io.FileInputStream openFileInput(java.lang.String)>":  
					case "<java.io.File: void <init>(java.io.File,java.lang.String)>":
					case "<java.io.File: void <init>(java.lang.String,java.lang.String)>":
					case "<java.io.File: void <init>(java.lang.String)>":
					case "<java.io.File: void <init>(java.net.URI)>":
					case "<android.content.ContextWrapper: java.io.FileInputStream openFileInput(java.lang.String)>":
					case "<android.content.Context: java.io.File getFileStreamPath(java.lang.String)>":
					case "<android.content.Context: java.io.File getDir(java.lang.String,int)>":
					case "<android.content.Context: java.io.File getDatabasePath(java.lang.String)>":
					case "<android.content.ContextWrapper: java.io.File getFileStreamPath(java.lang.String)>":
					case "<android.content.ContextWrapper: java.io.File getDir(java.lang.String,int)>":
					case "<android.content.ContextWrapper: java.io.File getDatabasePath(java.lang.String)>":
					case "<android.database.sqlite.SQLiteDatabase: android.database.sqlite.SQLiteDatabase openOrCreateDatabase(java.io.File,android.database.sqlite.SQLiteDatabase$CursorFactory)>":
					case "<android.database.sqlite.SQLiteDatabase: android.database.sqlite.SQLiteDatabase openOrCreateDatabase(java.lang.String,android.database.sqlite.SQLiteDatabase$CursorFactory)>":
					case "<android.database.sqlite.SQLiteDatabase: android.database.sqlite.SQLiteDatabase openOrCreateDatabase(java.lang.String,android.database.sqlite.SQLiteDatabase$CursorFactory,android.database.DatabaseErrorHandler)>":
					case "<android.content.ContextWrapper: android.database.sqlite.SQLiteDatabase openOrCreateDatabase(java.lang.String,android.database.sqlite.SQLiteDatabase$CursorFactory)>":
					case "<android.content.ContextWrapper: android.database.sqlite.SQLiteDatabase openOrCreateDatabase(java.lang.String,android.database.sqlite.SQLiteDatabase$CursorFactory,android.database.DatabaseErrorHandler)>":
						return true;
					default:
						return false;
				}
			}
			else
				return false;
		}
		else
			return false;
	}
	
	
	private AnalysisDecision getFileFormat(int codePosID) {
		ServerResponse response = new ServerResponse();
		response.setAnalysisName(getAnalysisName());
        response.setResponseExist(false);      
        			    
		Unit unit = codePositionManager.getUnitForCodePosition(codePosID);
		if(unit instanceof Stmt) {
			Stmt stmt = (Stmt)unit;
			if(stmt.containsInvokeExpr()) {
				InvokeExpr inv = stmt.getInvokeExpr();
				SootMethod sm = inv.getMethod();
				String methodSig = sm.getSignature();
				
				switch(methodSig) {
					case "<android.content.Context: java.io.FileInputStream openFileInput(java.lang.String)>":
						response.setResponseExist(true);  
						Pair<Integer, Object> param = new Pair<Integer, Object>(0, new FileFuzzingSerializableObject(FileFormat.UNKNOWN, 0));
						response.setParamValues(Collections.singleton(param));
						break;
						
					case "<java.io.File: void <init>(java.io.File,java.lang.String)>":
						response.setResponseExist(true); 
						//there is no need for a specific param index (=> -1); we need the complete file object anyway 
						param = new Pair<Integer, Object>(-1, new FileFuzzingSerializableObject(FileFormat.UNKNOWN, 1));
						response.setParamValues(Collections.singleton(param));
						break;						
						
					case "<java.io.File: void <init>(java.lang.String,java.lang.String)>":
						response.setResponseExist(true); 
						//there is no need for a specific param index (=> -1); we need the complete file object anyway 
						param = new Pair<Integer, Object>(-1, new FileFuzzingSerializableObject(FileFormat.UNKNOWN, 1));
						response.setParamValues(Collections.singleton(param));
						break;
						
					case "<java.io.File: void <init>(java.lang.String)>":
						response.setResponseExist(true); 
						//there is no need for a specific param index (=> -1); we need the complete file object anyway 
						param = new Pair<Integer, Object>(-1, new FileFuzzingSerializableObject(FileFormat.UNKNOWN, 1));
						response.setParamValues(Collections.singleton(param));
						break;
						
					case "<java.io.File: void <init>(java.net.URI)>":
						response.setResponseExist(true); 
						//there is no need for a specific param index (=> -1); we need the complete file object anyway 
						param = new Pair<Integer, Object>(-1, new FileFuzzingSerializableObject(FileFormat.UNKNOWN, 1));
						response.setParamValues(Collections.singleton(param));
						break;
						
					case "<android.content.ContextWrapper: java.io.FileInputStream openFileInput(java.lang.String)>":
						response.setResponseExist(true);  
						param = new Pair<Integer, Object>(0, new FileFuzzingSerializableObject(FileFormat.UNKNOWN, 0));
						response.setParamValues(Collections.singleton(param));
						break;
						
					case "<android.content.Context: java.io.File getFileStreamPath(java.lang.String)>":
						response.setResponseExist(true);  
						param = new Pair<Integer, Object>(0, new FileFuzzingSerializableObject(FileFormat.UNKNOWN, 0));
						response.setParamValues(Collections.singleton(param));
						break;
						
					case "<android.content.Context: java.io.File getDir(java.lang.String,int)>":
						response.setResponseExist(true);  
						param = new Pair<Integer, Object>(0, new FileFuzzingSerializableObject(FileFormat.DIRECTORY, 0));
						response.setParamValues(Collections.singleton(param));
						break;
						
					case "<android.content.Context: java.io.File getDatabasePath(java.lang.String)>":
						response.setResponseExist(true);  
						param = new Pair<Integer, Object>(0, new FileFuzzingSerializableObject(FileFormat.DATABASE, 2));
						response.setParamValues(Collections.singleton(param));
						break;
						
					case "<android.content.ContextWrapper: java.io.File getFileStreamPath(java.lang.String)>":
						response.setResponseExist(true);  
						param = new Pair<Integer, Object>(0, new FileFuzzingSerializableObject(FileFormat.UNKNOWN, 0));
						response.setParamValues(Collections.singleton(param));
						break;
						
					case "<android.content.ContextWrapper: java.io.File getDir(java.lang.String,int)>":
						response.setResponseExist(true);  
						param = new Pair<Integer, Object>(0, new FileFuzzingSerializableObject(FileFormat.DIRECTORY, 0));
						response.setParamValues(Collections.singleton(param));
						break;
						
					case "<android.content.ContextWrapper: java.io.File getDatabasePath(java.lang.String)>":
						response.setResponseExist(true);  
						param = new Pair<Integer, Object>(0, new FileFuzzingSerializableObject(FileFormat.DATABASE, 2));
						response.setParamValues(Collections.singleton(param));
						break;
						
					case "<android.database.sqlite.SQLiteDatabase: android.database.sqlite.SQLiteDatabase openOrCreateDatabase(java.io.File,android.database.sqlite.SQLiteDatabase$CursorFactory)>":
						response.setResponseExist(true);  
						param = new Pair<Integer, Object>(0, new FileFuzzingSerializableObject(FileFormat.DATABASE, 1));
						response.setParamValues(Collections.singleton(param));
						break;
						
					case "<android.database.sqlite.SQLiteDatabase: android.database.sqlite.SQLiteDatabase openOrCreateDatabase(java.lang.String,android.database.sqlite.SQLiteDatabase$CursorFactory)>":
						response.setResponseExist(true);  
						param = new Pair<Integer, Object>(0, new FileFuzzingSerializableObject(FileFormat.DATABASE, 0));
						response.setParamValues(Collections.singleton(param));
						break;
						
					case "<android.database.sqlite.SQLiteDatabase: android.database.sqlite.SQLiteDatabase openOrCreateDatabase(java.lang.String,android.database.sqlite.SQLiteDatabase$CursorFactory,android.database.DatabaseErrorHandler)>":
						response.setResponseExist(true);  
						param = new Pair<Integer, Object>(0, new FileFuzzingSerializableObject(FileFormat.DATABASE, 0));
						response.setParamValues(Collections.singleton(param));
						break;
						
					case "<android.content.ContextWrapper: android.database.sqlite.SQLiteDatabase openOrCreateDatabase(java.lang.String,android.database.sqlite.SQLiteDatabase$CursorFactory)>":
						response.setResponseExist(true);  
						param = new Pair<Integer, Object>(0, new FileFuzzingSerializableObject(FileFormat.DATABASE, 0));
						response.setParamValues(Collections.singleton(param));
						break;
						
					case "<android.content.ContextWrapper: android.database.sqlite.SQLiteDatabase openOrCreateDatabase(java.lang.String,android.database.sqlite.SQLiteDatabase$CursorFactory,android.database.DatabaseErrorHandler)>":
						response.setResponseExist(true);  
						param = new Pair<Integer, Object>(0, new FileFuzzingSerializableObject(FileFormat.DATABASE, 0));
						response.setParamValues(Collections.singleton(param));
						break;
				}
			}
		}
		
		if(response.doesResponseExist()) {
			AnalysisDecision finalDecision = new AnalysisDecision();
			finalDecision.setAnalysisName(getAnalysisName());
			finalDecision.setDecisionWeight(8);
		    finalDecision.setServerResponse(response);
		    return finalDecision;
		}
		else
			return null;
	}
}
