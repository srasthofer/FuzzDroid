package de.tu_darmstadt.sse.decisionmaker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import soot.Body;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.util.Chain;
import de.tu_darmstadt.sse.appinstrumentation.UtilInstrumenter;
import de.tu_darmstadt.sse.commandlinelogger.LoggerHelper;
import de.tu_darmstadt.sse.commandlinelogger.MyLevel;


public class UtilDecisionMaker {
	
	private static final String TARGET_METHODS_FILENAME = "." + File.separator + "files" + File.separator + "targetMethods.txt";
	
	
	public static Set<Unit> extractAllTargetLocations() {
		//extract all logging points from file
		Set<String> targetLocationsTmp = new HashSet<String>();
		
		Set<String> targetMethods = new HashSet<String>();		
		Set<Unit> allTargetLocations = new HashSet<Unit>();
		
		try{
			BufferedReader br = new BufferedReader(new FileReader(TARGET_METHODS_FILENAME));
		    try {
		        String line;
		        while ((line = br.readLine()) != null) {
		        	targetLocationsTmp.add(line);
		        }
		    } finally {
		        br.close();
		    }
		}catch(Exception ex) {
			LoggerHelper.logEvent(MyLevel.EXCEPTION_ANALYSIS, ex.getMessage());
			ex.printStackTrace();
			System.exit(-1);
		}
		
		targetMethods.addAll(targetLocationsTmp);
		
		if(!targetLocationsTmp.isEmpty()) {
			
			Chain<SootClass> applicationClasses = Scene.v().getApplicationClasses();
			for(SootClass clazz : applicationClasses) {				
				//no need to look into our code
				if (!UtilInstrumenter.isAppDeveloperCode(clazz)) 
					continue;
				
				for(SootMethod method : clazz.getMethods()) {
					if(method.hasActiveBody()) {
						Body body = method.retrieveActiveBody();
						for (Iterator<Unit> unitIt = body.getUnits().iterator(); unitIt.hasNext(); ) {
							Unit curUnit = unitIt.next();
							if(curUnit instanceof Stmt) {
								Stmt statement = (Stmt)curUnit;
								
								if(statement.containsInvokeExpr()){
									InvokeExpr invExpr = statement.getInvokeExpr();
									String invokeExprMethodSignature = invExpr.getMethod().getSignature();
									
									for(String targetLocation : targetLocationsTmp) {
										//we accept all classes
										if(targetLocation.startsWith("<*:")) {
											String pattern = "<.:\\s(.*)\\s(.*)\\((.*)\\)>";
										      Pattern r = Pattern.compile(pattern);

										      Matcher m = r.matcher(targetLocation);
										      if (m.find()) {
										    	  if(m.group(1).equals(invExpr.getMethod().getReturnType().toString()) &&
										    		  m.group(2).equals(invExpr.getMethod().getName()))
										    		  allTargetLocations.add(curUnit);
										      }
										}
										else if(targetLocation.equals(invokeExprMethodSignature))
											allTargetLocations.add(curUnit);
									}
								}
							}
						}
					}
				}
			}
		}
		
		return allTargetLocations;		
	}
	
}
