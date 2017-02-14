package de.tu_darmstadt.sse;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.xmlpull.v1.XmlPullParserException;

import soot.PackManager;
import soot.Scene;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.infoflow.android.SetupApplication;
import soot.jimple.infoflow.android.config.SootConfigForAndroid;
import soot.jimple.infoflow.android.manifest.ProcessManifest;
import soot.jimple.infoflow.cfg.LibraryClassPatcher;
import soot.jimple.infoflow.solver.cfg.BackwardsInfoflowCFG;
import soot.jimple.infoflow.source.data.NullSourceSinkDefinitionProvider;
import soot.options.Options;

import com.android.ddmlib.AndroidDebugBridge;

import de.tu_darmstadt.sse.apkspecific.UtilApk;
import de.tu_darmstadt.sse.apkspecific.CodeModel.CodePosition;
import de.tu_darmstadt.sse.apkspecific.CodeModel.CodePositionManager;
import de.tu_darmstadt.sse.appinstrumentation.Instrumenter;
import de.tu_darmstadt.sse.appinstrumentation.UtilInstrumenter;
import de.tu_darmstadt.sse.appinstrumentation.transformer.InstrumentedCodeTag;
import de.tu_darmstadt.sse.bootstrap.AnalysisTask;
import de.tu_darmstadt.sse.bootstrap.AnalysisTaskManager;
import de.tu_darmstadt.sse.bootstrap.DexFile;
import de.tu_darmstadt.sse.bootstrap.DexFileManager;
import de.tu_darmstadt.sse.bootstrap.InstanceIndependentCodePosition;
import de.tu_darmstadt.sse.commandlinelogger.LoggerHelper;
import de.tu_darmstadt.sse.commandlinelogger.MyLevel;
import de.tu_darmstadt.sse.decisionmaker.DecisionMaker;
import de.tu_darmstadt.sse.decisionmaker.DecisionMakerConfig;
import de.tu_darmstadt.sse.decisionmaker.DeterministicRandom;
import de.tu_darmstadt.sse.decisionmaker.UtilDecisionMaker;
import de.tu_darmstadt.sse.frameworkevents.FrameworkEvent;
import de.tu_darmstadt.sse.frameworkevents.manager.FrameworkEventManager;


public class Main {
	
	private static Main SINGLETON = new Main();
	
	private Main() {
		
	}
	
	public static Main v() {
		return SINGLETON;
	}
	
	public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

	
	public static void main(String[] args) {		
		Timer timer = new Timer();
        timer.schedule(new TimerTask() {        	
            @Override
            public void run() {
            	LoggerHelper.logEvent(MyLevel.TIMEOUT, "-1 | Complete analysis stopped due to timeout of 40 minutes");
                System.exit(0);
            }
        }, 40 * 60000);
		
		
		try{
			Main.v().run(args);
			AndroidDebugBridge.terminate();
		}catch(Exception ex) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			ex.printStackTrace(pw);			
			LoggerHelper.logEvent(MyLevel.EXCEPTION_ANALYSIS, sw.toString());
			UtilMain.writeToFile("mainException.txt", FrameworkOptions.apkPath + "\n");
		}
		LoggerHelper.logEvent(MyLevel.EXECUTION_STOP, "Analysis successfully terminated");
		//this is necessary otherwise we will wait for a max of 20 minutes for the TimerTask
		System.exit(0);
	}
	
	
	public Set<EnvironmentResult> run(String[] args) throws IOException, XmlPullParserException {		
		FrameworkOptions frameworkOptions = new FrameworkOptions();
		frameworkOptions.parse(args);
		LoggerHelper.initialize(FrameworkOptions.apkPath);
		LoggerHelper.logEvent(MyLevel.APKPATH, FrameworkOptions.apkPath);
		LoggerHelper.logEvent(MyLevel.ANALYSIS, 
				String.format("Force timeout: %d || Ininactivity timeout: %d || Max restarts: %d", 
						FrameworkOptions.forceTimeout, 
						FrameworkOptions.inactivityTimeout,
						FrameworkOptions.maxRestarts));	
		
						
		//remove all files in sootOutput folder
		org.apache.commons.io.FileUtils.cleanDirectory(new File(UtilInstrumenter.SOOT_OUTPUT));
		
		Set<String> blacklistedAPKs = UtilMain.getBlacklistedAPKs();
		
		if(!blacklistedAPKs.contains(FrameworkOptions.apkPath)) {		
			DexFileManager dexFileManager = new DexFileManager();
			
			// Schedule the initial analysis task
			AnalysisTaskManager analysisTaskManager = new AnalysisTaskManager();
			analysisTaskManager.enqueueAnalysisTask(new AnalysisTask());
			
			// Execute all of our analysis tasks
			Set<EnvironmentResult> results = new HashSet<>();
			AnalysisTask currentTask;
			int taskId = 0;
			while ((currentTask = analysisTaskManager.scheduleNextTask()) != null) {
				LoggerHelper.logInfo(String.format("Starting analysis for task %d with %d dex files",
						taskId++, currentTask.getDexFilesToMerge().size()));
				try {
					//needed for the extractAllTargetLocations method
					initializeSoot(currentTask);
				}
				catch (Exception ex) {
					System.err.println("Could not initialize Soot, skipping analysis task: " + ex.getMessage());
					ex.printStackTrace();
					continue;
				}
				
				// The DecisionMaker configuration needs to initialize the metrics.
				// Therefore, it requires a running Soot instance.
				DecisionMakerConfig config = new DecisionMakerConfig();		
				config.initializeCFG();
							
				//extract all target locations
				Set<Unit> allTargetLocations = UtilDecisionMaker.extractAllTargetLocations();
				if(allTargetLocations.isEmpty()) {
					LoggerHelper.logEvent(MyLevel.NO_TARGETS, "There are no reachable target locations");
					UtilMain.writeToFile("noLoggingPoint.txt", FrameworkOptions.apkPath + "\n");
				}
				
				//we have to do this hack due to a reset of soot. This step is necessary otherwise, we will not get a clean apk for each logging point
				Set<InstanceIndependentCodePosition> targetsAsCodePos = UtilMain.convertUnitsToIndependentCodePosition(allTargetLocations, config.getBackwardsCFG());			
				boolean firstRun = true;
				
				//treat every target location individually
				for(InstanceIndependentCodePosition singleTargetAsPos : targetsAsCodePos) {							
					if(!firstRun)
						initializeSoot(currentTask);
					firstRun = false;
					
					//we need to do this step, because we reseted soot
					Unit singleTarget = UtilMain.convertIndependentCodePositionToUnits(singleTargetAsPos);
					if(singleTarget == null) {
						LoggerHelper.logEvent(MyLevel.EXCEPTION_ANALYSIS, "############ PLEASE DOUBLE CHECK TARGET LOCATION ");
						continue;
					}
									
					Set<Unit> singleTargetLocation = Collections.singleton(singleTarget);										
					
					// We may need to remove some statements before running the analysis
					removeStatementsForAnalysis(currentTask);
					
					// needs to be re-initialized due to soot-reset
					config = new DecisionMakerConfig();
					config.initializeCFG();					
					
					//now we have access to the CFG
					//check if target is reachable:
					if(config.getBackwardsCFG().getMethodOf(singleTarget) == null) {
						LoggerHelper.logEvent(MyLevel.LOGGING_POINT, "target is not statically reachable!");
						continue;
					}
					
					boolean successfullInitialization = config.initialize(singleTargetLocation);
					
					if(successfullInitialization) {	
						//get potential Android event which trigger the initial code section for reaching the logging point					
						Set<FrameworkEvent> events = null;
						//todo PAPER-EVAL ONLY
						if(FrameworkOptions.evaluationJustStartApp) {
							events = new HashSet<FrameworkEvent>();
							events.add(null);
						}
						else						
						events = getFrameworkEvents(singleTarget, config.getBackwardsCFG());
						if(events.isEmpty()) {
							LoggerHelper.logEvent(MyLevel.ADB_EVENT, "no events available");
							events.add(null);
						}
						
						DecisionMaker decisionMaker = new DecisionMaker(config, dexFileManager, analysisTaskManager);
						CodePositionManager codePositionManager = CodePositionManager.getCodePositionManagerInstance();							
																
						appPreparationPhase(codePositionManager, config);
						
						int totalEvents = events.size();
						int currentEvent = 0;
						Set<EnvironmentResult> resultsPerApp = new HashSet<>();
						for(FrameworkEvent event : events) {					
							try{
								currentEvent += 1;
								
								//todo PAPER-EVAL ONLY
								if(!FrameworkOptions.evaluationOnly)
									decisionMaker.runPreAnalysisPhase();
								
								//after the pre-analysis, we are able to get access to the code positions
								//let's log the code position
								CodePosition codePos = codePositionManager.getCodePositionForUnit(singleTarget);
								String info = String.format("Enclosing Method: %s | %d | %s", codePos.getEnclosingMethod(), codePos.getID(), singleTarget.toString());			
								LoggerHelper.logEvent(MyLevel.LOGGING_POINT, info);		
								
								if(events.isEmpty())
									LoggerHelper.logEvent(MyLevel.INFO, String.format("%s: 0/0 events sent", codePos.getID()));
								else
									LoggerHelper.logEvent(MyLevel.INFO, String.format("%s: %s/%s events (%s) ready for process", codePos.getID(), currentEvent, totalEvents, event));
								
								LoggerHelper.logEvent(MyLevel.EXECUTION_START, "");
								repeatedlyExecuteAnalysis(decisionMaker, resultsPerApp, event);
								LoggerHelper.logEvent(MyLevel.EXECUTION_STOP, "");
								
								// If we have reached the goal, there is no need to try the other events
								for (EnvironmentResult result : resultsPerApp)
									if (result.isTargetReached())
										break;
							}catch(Exception ex) {
								LoggerHelper.logEvent(MyLevel.EXCEPTION_RUNTIME, ex.getMessage());
								ex.printStackTrace();							
							}
						}
						
						results.addAll(resultsPerApp);
					}
				}
			}
			return results;
		}
		return null;		
	}
	
		
	
	private void removeStatementsForAnalysis(AnalysisTask currentTask) {
		for (InstanceIndependentCodePosition codePos : currentTask.getStatementsToRemove()) {
			// Get the method in which to remove the statement
			SootMethod sm = Scene.v().grabMethod(codePos.getMethodSignature());
			if (sm == null) {
				LoggerHelper.logWarning("Method " + codePos.getMethodSignature() + " not found");
				continue;
			}

			int lineNum = 0;
			for (Iterator<Unit> unitIt = sm.getActiveBody().getUnits().iterator(); unitIt.hasNext(); ) {
				// Is this the statement to remove?
				Unit u = unitIt.next();
				
				// Ignore statements that were added by an instrumenter
				if (!u.hasTag(InstrumentedCodeTag.name))
					if (lineNum == codePos.getLineNumber())
						if (codePos.getStatement() == null || u.toString().equals(codePos.getStatement())) {
							unitIt.remove();
							break;
						}
				lineNum++;
			}
		}
	}
	
	

	
	private void appPreparationPhase(CodePositionManager codePositionManager,
			DecisionMakerConfig config) {
		LoggerHelper.logEvent(MyLevel.ANALYSIS, "Prepare app for fuzzing...");
		
		UtilApk.removeOldAPKs(FrameworkOptions.getAPKName());
		
		Instrumenter instrumenter = new Instrumenter(codePositionManager, config);
		LoggerHelper.logEvent(MyLevel.INSTRUMENTATION_START, "");
		instrumenter.doInstrumentation();
		LoggerHelper.logEvent(MyLevel.INSTRUMENTATION_STOP, "");
		
		if(FrameworkOptions.deployApp) {
			UtilApk.jarsigner(FrameworkOptions.getAPKName());
			UtilApk.zipalign(FrameworkOptions.getAPKName());
		}
	}
	
	private void repeatedlyExecuteAnalysis(DecisionMaker decisionMaker,
			Set<EnvironmentResult> results, FrameworkEvent event) {
		decisionMaker.initialize();
		
		for (int seed = 0; seed < FrameworkOptions.nbSeeds; seed++) {
			LoggerHelper.logEvent(MyLevel.ANALYSIS, "Running analysis with seed "+seed);	
			DeterministicRandom.reinitialize(seed);
			EnvironmentResult curResult = decisionMaker.executeDecisionMaker(event);
			
			results.add(curResult);
			if (curResult.isTargetReached())
				LoggerHelper.logEvent(MyLevel.RUNTIME, "target reached");
			else
				LoggerHelper.logEvent(MyLevel.RUNTIME, "NO target was reached");
		}
		decisionMaker.tearDown();
	}
	
	
	private void initializeSoot(AnalysisTask currentTask) throws IOException, XmlPullParserException{
		SetupApplication app = null;
		app = new SetupApplication(FrameworkOptions.androidJarPath,
				FrameworkOptions.apkPath);
        app.calculateSourcesSinksEntrypoints(new NullSourceSinkDefinitionProvider());

        new SootConfigForAndroid().setSootOptions(Options.v());
		
		Options.v().set_allow_phantom_refs(true);
		Options.v().set_validate(true);
		Options.v().set_force_overwrite(true);
		
		// We need a callgraph
		Options.v().set_whole_program(true);
		Options.v().setPhaseOption("cg.spark", "on");
		
		Options.v().set_src_prec(Options.src_prec_apk);
		Options.v().set_output_format(Options.output_format_dex);
		
		List<String> processDir = new ArrayList<String>();
		processDir.add(FrameworkOptions.apkPath);
		
		// dex files to merge
		for (DexFile dexFile : currentTask.getDexFilesToMerge()) {
			if (dexFile.getLocalFileName() != null
					&& !dexFile.getLocalFileName().isEmpty()
					&& new File(dexFile.getLocalFileName()).exists())
				processDir.add(dexFile.getLocalFileName());
			else
				throw new RuntimeException("Could not find local dex file");
		}
		
		//hooking specific
		processDir.add(UtilInstrumenter.HOOKING_LIBRARY);
		processDir.add(UtilInstrumenter.ADDITIONAL_APP_CLASSES_BIN);
		processDir.add(UtilInstrumenter.SHARED_CLASSES_BIN);
		Options.v().set_process_dir(processDir);
		
		Options.v().set_android_jars(FrameworkOptions.androidJarPath);
		Options.v().set_no_writeout_body_releasing(true);				
		
		//the bin folder has to be added to the classpath in order to
		//use the Java part for the instrumentation (JavaClassForInstrumentation)
		String androidJar = Scene.v().getAndroidJarPath(FrameworkOptions.androidJarPath, FrameworkOptions.apkPath);
		Options.v().set_soot_classpath(UtilInstrumenter.ADDITIONAL_APP_CLASSES_BIN + File.pathSeparator +
				UtilInstrumenter.SHARED_CLASSES_BIN + File.pathSeparator + androidJar);
		
		Scene.v().loadNecessaryClasses();
		
		new LibraryClassPatcher().patchLibraries();
		
		// Create the entry point
		app.getEntryPointCreator().setDummyMethodName("main");
		SootMethod entryPoint = app.getEntryPointCreator().createDummyMain();
		entryPoint.getDeclaringClass().setLibraryClass();
		Options.v().set_main_class(entryPoint.getDeclaringClass().getName());
		Scene.v().setEntryPoints(Collections.singletonList(entryPoint));
		
		PackManager.v().runPacks();
	}
	
	
	private void printTargetLocationInfo(DecisionMakerConfig config, CodePositionManager codePositionManager) {
		LoggerHelper.logEvent(MyLevel.ANALYSIS, "Found "+config.getAllTargetLocations().size()+" target location(s)");
		for(Unit unit : config.getAllTargetLocations()) {
			CodePosition codePos = codePositionManager.getCodePositionForUnit(unit);
			String info = String.format("Enclosing Method: %s | %d | %s", codePos.getEnclosingMethod(), codePos.getID(), unit.toString());			
			LoggerHelper.logEvent(MyLevel.LOGGING_POINT, info);
		}
	}
	
	
	private Set<FrameworkEvent> getFrameworkEvents(Unit targetLocation, BackwardsInfoflowCFG cfg) {
		FrameworkEventManager eventManager =  FrameworkEventManager.getEventManager();
		ProcessManifest manifest = UtilApk.getManifest();									
		Set<FrameworkEvent> targetAndroidEvents = eventManager.extractInitalEventsForReachingTarget(targetLocation, cfg, manifest);			
		return targetAndroidEvents;
	}				
}
