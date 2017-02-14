package de.tu_darmstadt.sse.appinstrumentation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.xmlpull.v1.XmlPullParserException;

import soot.Body;
import soot.Local;
import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.SootMethodRef;
import soot.Type;
import soot.Unit;
import soot.VoidType;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.infoflow.android.manifest.ProcessManifest;
import de.tu_darmstadt.sse.FrameworkOptions;
import de.tu_darmstadt.sse.apkspecific.UtilApk;
import de.tu_darmstadt.sse.apkspecific.CodeModel.CodePositionManager;
import de.tu_darmstadt.sse.apkspecific.CodeModel.CodePositionWriter;
import de.tu_darmstadt.sse.appinstrumentation.transformer.ClassLoaderTransformer;
import de.tu_darmstadt.sse.appinstrumentation.transformer.CodePositionTracking;
import de.tu_darmstadt.sse.appinstrumentation.transformer.ConditionTracking;
import de.tu_darmstadt.sse.appinstrumentation.transformer.CrashReporterInjection;
import de.tu_darmstadt.sse.appinstrumentation.transformer.DummyMethodHookTransformer;
import de.tu_darmstadt.sse.appinstrumentation.transformer.DynamicCallGraphTracking;
import de.tu_darmstadt.sse.appinstrumentation.transformer.DynamicValueTransformer;
import de.tu_darmstadt.sse.appinstrumentation.transformer.GlobalInstanceTransformer;
import de.tu_darmstadt.sse.appinstrumentation.transformer.GoalReachedTracking;
import de.tu_darmstadt.sse.appinstrumentation.transformer.PathExecutionTransformer;
import de.tu_darmstadt.sse.appinstrumentation.transformer.TimingBombTransformer;
import de.tu_darmstadt.sse.commandlinelogger.LoggerHelper;
import de.tu_darmstadt.sse.commandlinelogger.MyLevel;
import de.tu_darmstadt.sse.decisionmaker.DecisionMakerConfig;


public class Instrumenter {
	
	private final DecisionMakerConfig config;
	private CodePositionManager codePositionManager;
	
	public Instrumenter(CodePositionManager codePositionManager,
			DecisionMakerConfig config) {
		this.codePositionManager = codePositionManager;
		this.config = config;
	}
	
	
	private void postProcessForHookingFunctionality(String apkFile) {
		//add native code
		File instrumentedAPK = new File(UtilInstrumenter.SOOT_OUTPUT_APK);
		File hookingBinaryARM = new File(UtilInstrumenter.HOOKING_LIBRARY_ARM);		
		File hookingBinaryX86 = new File(UtilInstrumenter.HOOKING_LIBRARY_X86);
		
		addFilesToZip(instrumentedAPK.getAbsolutePath(), 
				hookingBinaryARM.getAbsolutePath(),
				hookingBinaryX86.getAbsolutePath());
		
		//manipulate AndroidManifest.xml
		ProcessManifest androidManifest = null;
		try {
			androidManifest = new ProcessManifest(apkFile);
			UtilApk.manipulateAndroidManifest(androidManifest);
			addTemporalManifestChanges(androidManifest);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		}
		finally {
			if (androidManifest != null)
				androidManifest.close();
		}
	}
	
	
	private void addFilesToZip(String instrumentedAPKPath,
			String armHookingFilePath,
			String x86HookingFilePath) {
		// Make sure that we don't add any removable parts
		instrumentedAPKPath = instrumentedAPKPath.replace("/./", "/");
		instrumentedAPKPath = instrumentedAPKPath.replace("\\.\\", "\\");
		
		File apkFile = new File(instrumentedAPKPath);
		if (!apkFile.exists())
			throw new RuntimeException("Output APK file not found: " + apkFile);
		
		URI uri;
		try {
			String uriPath = apkFile.toURI().toString();
			uriPath = uriPath.replace("%20", " ");		// Windows hiccups
			uri = new URI("jar", uriPath, null);
		} catch (URISyntaxException ex) {
			throw new RuntimeException(ex);
		}
		final Map<String, ?> env = Collections.singletonMap("create", "true");

		try (
		    final FileSystem fs = FileSystems.newFileSystem(uri, env);
		) {
			final Path armDir = fs.getPath("lib", "armeabi");			
			final Path x86Dir = fs.getPath("lib", "x86");
			final Path armFile = fs.getPath("lib", "armeabi", "libZHook.so");
			final Path x86File = fs.getPath("lib", "x86", "libZHook.so");
			
			Set<PosixFilePermission> perms = PosixFilePermissions.fromString("rwxrwx--x");
		    FileAttribute<Set<PosixFilePermission>> fileAttributes =
		    		PosixFilePermissions.asFileAttribute(perms);
		    
		    Files.createDirectories(armDir, fileAttributes);
		    Files.createDirectories(x86Dir, fileAttributes);

		    final Path armSrc = Paths.get(armHookingFilePath);
		    final Path x86Src = Paths.get(x86HookingFilePath);

		    if (!Files.exists(armFile))
		    	Files.copy(armSrc, armFile);
		    if (!Files.exists(x86File))
		    	Files.copy(x86Src, x86File);
		} catch (Exception e) {
			LoggerHelper.logEvent(MyLevel.EXCEPTION_ANALYSIS, "There is a problem in addFilesToZip:\n");
			e.printStackTrace();
		}
	}
	
	
	public void doInstrumentation() {
		LoggerHelper.logInfo("Started instrumentation...");
				
		//get the manifest
		ProcessManifest manifest = null;
		try {
			File instrumentedAPK = new File(FrameworkOptions.apkPath);			
			try {
				manifest = new ProcessManifest(instrumentedAPK);				
			}
			finally {
				if (manifest != null)
					manifest.close();
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
						
		executeTransformers(manifest);
		//todo PAPER-EVAL ONLY
		if(!FrameworkOptions.evaluationOnly)
			initializeHooking(manifest);
				
		PackManager.v().writeOutput();
		
		try {
			//todo PAPER-EVAL ONLY
			if(!FrameworkOptions.evaluationOnly){
				//hooking & path-tracking
				postProcessForHookingFunctionality(UtilInstrumenter.SOOT_OUTPUT_APK);
				//codePositionTracking
				postProcessForPositionTracking();
			}
						
			LoggerHelper.logInfo("Finished instrumentation...");
		} catch (Exception e) {
			LoggerHelper.logEvent(MyLevel.EXCEPTION_ANALYSIS, "There is a problem in the instrumentation phase: " + e.getMessage());
			e.printStackTrace();
		}
	}	
		
	
	private void executeTransformers(ProcessManifest manifest) {
		// We first need to retrieve some information from the manifest
		Set<String> constructors = new HashSet<>();
		for (String className : manifest.getEntryPointClasses())
			constructors.add("<" + className + ": void <init>()>");
				
		ConditionTracking conditionTracking = new ConditionTracking();
		CodePositionTracking codePositionTracking = new CodePositionTracking(codePositionManager);
		DynamicCallGraphTracking dynamicCallGraphTracking = new DynamicCallGraphTracking(codePositionManager);
		PathExecutionTransformer pathExecutionTransformer = new PathExecutionTransformer();
		GoalReachedTracking goalReachedTracking = new GoalReachedTracking(config.getAllTargetLocations());
		TimingBombTransformer timingBombs = new TimingBombTransformer();
		DummyMethodHookTransformer dummyMethods = new DummyMethodHookTransformer();
		DynamicValueTransformer dynamicValues = new DynamicValueTransformer(true);
		ClassLoaderTransformer classLoaders = new ClassLoaderTransformer();
		
		for (SootClass sc : Scene.v().getApplicationClasses())
			for (SootMethod sm : sc.getMethods())
				if (sm.isConcrete()) {
					Body body = sm.getActiveBody();
					//todo PAPER-EVAL ONLY
					codePositionTracking.transform(body);					
					if(!FrameworkOptions.evaluationOnly){
//						conditionTracking.transform(body);
//						dynamicCallGraphTracking.transform(body);
					}
//					if (FrameworkOptions.recordPathExecution)
//						pathExecutionTransformer.transform(body);
					goalReachedTracking.transform(body);
					//todo PAPER-EVAL ONLY
					if(!FrameworkOptions.evaluationOnly){
						timingBombs.transform(body);
						dummyMethods.transform(body);
						dynamicValues.transform(body);
					}
					classLoaders.transform(body);
				}
		//todo PAPER-EVAL ONLY
		if(!FrameworkOptions.evaluationOnly)
			new CrashReporterInjection(constructors).transform();
		new GlobalInstanceTransformer().transform();
	}
	
	
	private void initializeHooking(ProcessManifest manifest) {						
		String applicationName = manifest.getApplicationName();
		//case 1
		if(applicationName != null) {
			if(applicationName.startsWith(".")) {
				String packageName = manifest.getPackageName();
				if(packageName == null)
					throw new RuntimeException("There is a problem with the package name");
				applicationName = packageName + applicationName;
			}
			SootClass applicationSootClass = Scene.v().getSootClass(applicationName);
			if(applicationSootClass != null) {				
				String attachMethodName = String.format("<%s: void attachBaseContext(android.content.Context)>", applicationName);
				SootMethod attachMethod = Scene.v().grabMethod(attachMethodName);	
				//case 1
				if(attachMethod != null) {
					Body body = attachMethod.getActiveBody();
					Local contextParam = body.getParameterLocal(0);
					
					List<Unit> unitsToInstrument = new ArrayList<Unit>();										
					String hookingHelperApplicationClassAttachMethodName = String.format("<%s: void initializeHooking(android.content.Context)>", UtilInstrumenter.HOOKER_CLASS);
					SootMethod hookingHelperApplicationClassAttachMethod = Scene.v().getMethod(hookingHelperApplicationClassAttachMethodName);
					if(hookingHelperApplicationClassAttachMethod == null)
						throw new RuntimeException("this should not happen");					
					SootMethodRef ref = hookingHelperApplicationClassAttachMethod.makeRef();					
					InvokeExpr invExpr = Jimple.v().newStaticInvokeExpr(ref, contextParam);
					unitsToInstrument.add(Jimple.v().newInvokeStmt(invExpr));
					
					
					Unit instrumentAfterUnit = null;
					for(Unit unit : body.getUnits()) {
						if(unit instanceof InvokeStmt) {
							InvokeStmt invStmt = (InvokeStmt)unit;
							if(invStmt.getInvokeExpr().getMethod().getSubSignature().equals("void attachBaseContext(android.content.Context)")) {
								instrumentAfterUnit = unit;
								break;
							}
						}
					}
					
					if(instrumentAfterUnit == null)
						throw new RuntimeException("this should not happen");
					body.getUnits().insertAfter(unitsToInstrument, instrumentAfterUnit);								
				}
				//case 2
				else {
					attachMethodName = String.format("<%s: void attachBaseContext(android.content.Context)>", UtilInstrumenter.HELPER_APPLICATION_FOR_HOOKING);	
					attachMethod = Scene.v().grabMethod(attachMethodName);
					if(attachMethod == null)
						throw new RuntimeException("this should not happen");
					
					List<Type> params = new ArrayList<Type>();
					SootClass contextClass = Scene.v().getSootClass("android.content.Context");
					params.add(contextClass.getType());
					SootMethod newAttachMethod = new SootMethod("attachBaseContext", params, VoidType.v());
					newAttachMethod.setModifiers(soot.Modifier.PROTECTED);
					newAttachMethod.setActiveBody(attachMethod.getActiveBody());
					applicationSootClass.addMethod(newAttachMethod);
				}
				
				//there is no need for our Application class
				Scene.v().getSootClass(UtilInstrumenter.HELPER_APPLICATION_FOR_HOOKING).setLibraryClass();
			}
			else {
				throw new RuntimeException("There is a problem with the Application class!");
			}
		}
		//case 3
		else{
			//there is no need for any instrumentation since the Application class is set to application-class.
		}
	}
	
	private void addTemporalManifestChanges(ProcessManifest androidManifest) {
		File manifestFile = null;
		try{
			//temporarily save the modified AndroidManifest
			manifestFile = File.createTempFile("AndroidManifest.xml", null);		
			FileOutputStream fos = new FileOutputStream(manifestFile.getPath());
			byte[] output = androidManifest.getOutput();
			fos.write(output);
			fos.close();
			
			ArrayList<File> files = new ArrayList<File>();
			files.add(manifestFile);
			HashMap<String, String> paths = new HashMap<String, String>();
			paths.put(manifestFile.getAbsolutePath(), "AndroidManifest.xml");
			//add the modified AndroidManifest into the original APK
			androidManifest.getApk().addFilesToApk(files, paths);
		}catch(Exception ex) {
			LoggerHelper.logEvent(MyLevel.EXCEPTION_ANALYSIS, ex.getMessage());
			ex.printStackTrace();
			System.exit(-1);
		}
		finally {
			if (manifestFile != null && manifestFile.exists())
				manifestFile.delete();
		}
	}

	private void postProcessForPositionTracking() {
		CodePositionWriter writer = new CodePositionWriter(codePositionManager);
		try {
			writer.writeCodePositions("CodePositions.log");
		} catch (FileNotFoundException e) {
			System.err.println("Could not write code position file");
			e.printStackTrace();
		}
	}	
		
}
