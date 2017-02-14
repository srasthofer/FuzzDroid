package de.tu_darmstadt.sse.decisionmaker;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import soot.Unit;
import soot.jimple.infoflow.solver.cfg.BackwardsInfoflowCFG;
import soot.jimple.infoflow.solver.cfg.InfoflowCFG;
import de.tu_darmstadt.sse.commandlinelogger.LoggerHelper;
import de.tu_darmstadt.sse.commandlinelogger.MyLevel;
import de.tu_darmstadt.sse.decisionmaker.analysis.FuzzyAnalysis;
import de.tu_darmstadt.sse.dynamiccfg.utils.FileUtils;
import de.tu_darmstadt.sse.progressmetric.IProgressMetric;


public class DecisionMakerConfig {
	
	
	private final String ANALYSES_FILENAME = "." + File.separator + "files" + File.separator + "analysesNames.txt";
	
	private final String METRICS_FILENAME = "." + File.separator + "files" + File.separator + "metricsNames.txt";
	
	
	private Set<FuzzyAnalysis> allAnalyses;
	private Map<String, FuzzyAnalysis> nameToAnalysis;
	
	private Set<IProgressMetric> progressMetrics;
	
	
	private Set<Unit> allTargetLocations = null;	
	
	private BackwardsInfoflowCFG backwardsCFG;
	
	public boolean initialize(Set<Unit> targetLocations) {	
		boolean successfull = true;
		allTargetLocations = targetLocations;
		allAnalyses = new HashSet<FuzzyAnalysis>();
		nameToAnalysis = new HashMap<String, FuzzyAnalysis>();
		progressMetrics = new HashSet<IProgressMetric>();
		successfull = registerFuzzyAnalyses();
		if(!successfull)
			return false;
		successfull = registerProgressMetrics();
		if(!successfull)
			return false;
		return true;
	}
	
	public void initializeCFG() {
		InfoflowCFG forwardCFG = new InfoflowCFG();
		backwardsCFG = new BackwardsInfoflowCFG(forwardCFG);
	}
	
	
	private boolean registerFuzzyAnalyses() {
		Set<String> registeredAnalyses = getAnalysesNames();
		for(String registeredAnalysisClassName : registeredAnalyses)
			if (!registeredAnalysisClassName.startsWith("%")) {
				try{					
					Class<?> analysisClass = Class.forName(registeredAnalysisClassName);
					Constructor<?> defaultConstructor = analysisClass.getConstructor();
					defaultConstructor.isAccessible();
					Object constructorObject = defaultConstructor.newInstance();
					if(!(constructorObject instanceof FuzzyAnalysis))
						throw new RuntimeException("There is a problem with the registered analysis in the files/analysesNames.txt file!");
					FuzzyAnalysis analysis = (FuzzyAnalysis) constructorObject;
					
					allAnalyses.add(analysis);
					nameToAnalysis.put(analysis.getAnalysisName(), analysis);
					LoggerHelper.logEvent(MyLevel.ANALYSIS, "[ANALYSIS-TYPE] " + registeredAnalysisClassName);
				}
				catch(Exception ex) {
					LoggerHelper.logEvent(MyLevel.EXCEPTION_ANALYSIS, ex.getMessage());
					ex.printStackTrace();
					return false;
				}
			}
		return true;
	}

	
	private boolean registerProgressMetrics() {
		Set<String> registeredMetrics = getProgressMetricNames();
		for(String registeredMetricsClassName : registeredMetrics)
			if (!registeredMetricsClassName.startsWith("%")) {
				try{						
					Class<?> metricClass = Class.forName(registeredMetricsClassName);
					Constructor<?> defaultConstructor = metricClass.getConstructor(Collection.class, InfoflowCFG.class);
					defaultConstructor.isAccessible();
					Object constructorObject = defaultConstructor.newInstance(allTargetLocations, backwardsCFG);
					if(!(constructorObject instanceof IProgressMetric))
						throw new RuntimeException("There is a problem with the registered metric in the files/metricsNames.txt file!");
					IProgressMetric metric = (IProgressMetric)constructorObject;
					LoggerHelper.logEvent(MyLevel.ANALYSIS, "[METRIC-TYPE] " + registeredMetricsClassName);
					
					//currently, there can be only a single target
					if(allTargetLocations.size() != 1)
						throw new RuntimeException("There can be only 1 target location per run");
					Unit target = allTargetLocations.iterator().next();
					if(backwardsCFG.getMethodOf(target) != null) {													
						metric.setCurrentTargetLocation(target);
					
						//initialize the metric, otherwise it is empty!
						metric.initalize();										
						progressMetrics.add(metric);
					}
					else{
						LoggerHelper.logEvent(MyLevel.LOGGING_POINT, "target is not statically reachable!");
						return false;
					}
				}
				catch(Exception ex) {
					LoggerHelper.logEvent(MyLevel.EXCEPTION_ANALYSIS, ex.getMessage());
					ex.printStackTrace();
					System.exit(-1);
				}				
			}
		return true;
	}
	
	
	private Set<String> getAnalysesNames() {
		return FileUtils.textFileToLineSet(ANALYSES_FILENAME);
	}
	
	
	private Set<String> getProgressMetricNames() {
		return FileUtils.textFileToLineSet(METRICS_FILENAME);
	}
	
//	
//	private void extractAllTargetLocations() {
//		//extract all logging points from file
//		Set<String> targetLocationsTmp = new HashSet<String>();
//		
//		try{
//			BufferedReader br = new BufferedReader(new FileReader(TARGET_METHODS_FILENAME));
//		    try {
//		        String line;
//		        while ((line = br.readLine()) != null) {
//		        	targetLocationsTmp.add(line);
//		        }
//		    } finally {
//		        br.close();
//		    }
//		}catch(Exception ex) {
//			ex.printStackTrace();
//			System.exit(-1);
//		}
//		
//		targetMethods.addAll(targetLocationsTmp);
//		
//		if(!targetLocationsTmp.isEmpty()) {
//			
//			Chain<SootClass> applicationClasses = Scene.v().getApplicationClasses();
//			for(SootClass clazz : applicationClasses) {				
//				//no need to look into our code
//				if (!UtilInstrumenter.isAppDeveloperCode(clazz)) 
//					continue;
//				
//				for(SootMethod method : clazz.getMethods()) {
//					if(method.hasActiveBody()) {
//						Body body = method.retrieveActiveBody();
//						for (Iterator<Unit> unitIt = body.getUnits().iterator(); unitIt.hasNext(); ) {
//							Unit curUnit = unitIt.next();
//							if(curUnit instanceof Stmt) {
//								Stmt statement = (Stmt)curUnit;
//								
//								if(statement.containsInvokeExpr()){
//									InvokeExpr invExpr = statement.getInvokeExpr();
//									String invokeExprMethodSignature = invExpr.getMethod().getSignature();
//									
//									for(String targetLocation : targetLocationsTmp) {
//										//we accept all classes
//										if(targetLocation.startsWith("<*:")) {
//											String pattern = "<.:\\s(.*)\\s(.*)\\((.*)\\)>";
//										      Pattern r = Pattern.compile(pattern);
//
//										      Matcher m = r.matcher(targetLocation);
//										      if (m.find()) {
//										    	  if(m.group(1).equals(invExpr.getMethod().getReturnType().toString()) &&
//										    		  m.group(2).equals(invExpr.getMethod().getName()))
//										    		  this.allTargetLocations.add(curUnit);
//										      }
//										}
//										else if(targetLocation.equals(invokeExprMethodSignature))
//											this.allTargetLocations.add(curUnit);
//									}
//								}
//							}
//						}
//					}
//				}
//			}
//		}		
//		if(this.allTargetLocations.size() == 0) {
//			LoggerHelper.logWarning("There are no reachable target locations");
//			System.exit(0);
//		}
//		
//	}
	
	public Set<FuzzyAnalysis> getAnalyses() {
		return this.allAnalyses;
	}
	
	public Set<IProgressMetric> getMetrics() {
		return this.progressMetrics;
	}
	
	public Set<Unit> getAllTargetLocations() {
		return this.allTargetLocations;
	}	

	public BackwardsInfoflowCFG getBackwardsCFG() {
		return backwardsCFG;
	}

	public void setAllTargetLocations(Set<Unit> allTargetLocations) {
		this.allTargetLocations = allTargetLocations;
	}

	
	public FuzzyAnalysis getAnalysisByName(String name) {
		return nameToAnalysis.get(name);
	}
	
}
