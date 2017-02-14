package de.tu_darmstadt.sse.apkspecific;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Set;

import soot.jimple.infoflow.android.axml.AXmlAttribute;
import soot.jimple.infoflow.android.axml.AXmlNode;
import soot.jimple.infoflow.android.manifest.ProcessManifest;
import de.tu_darmstadt.sse.FrameworkOptions;
import de.tu_darmstadt.sse.appinstrumentation.UtilInstrumenter;
import de.tu_darmstadt.sse.commandlinelogger.LoggerHelper;
import de.tu_darmstadt.sse.commandlinelogger.MyLevel;

public class UtilApk {
	
	private static final String ANDROID_NAMESPACE = "http://schemas.android.com/apk/res/android";
	private static ProcessManifest manifest = null;
	
	public static void jarsigner(String appName){
		LoggerHelper.logInfo("Started jarsigner...");
		final String[] command = new String[14];
		
		command[0] = "jarsigner";
		command[1] = "-verbose";
		command[2] = "-sigalg";
		command[3] = "SHA1withRSA";
		command[4] = "-digestalg";
		command[5] = "SHA1";
		command[6] = "-keystore";
		command[7] = FrameworkOptions.KEYSTORE_PATH;
		command[8] = UtilInstrumenter.SOOT_OUTPUT_APK;
		command[9] = FrameworkOptions.KEYSTORE_NAME;
		command[10] = "-storepass";
		command[11] = FrameworkOptions.KEYSTORE_PASSWORD;
		command[12] = "-keypass";
		command[13] = FrameworkOptions.KEYSTORE_PASSWORD;
		
		Process p;
		try {			
			p = Runtime.getRuntime().exec(command);
			
			BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = null;
			while ((line = input.readLine()) != null) {
				System.out.println(line);
			}
			
			input.close();
			
            p.waitFor();            
		}catch(Exception ex){
			LoggerHelper.logEvent(MyLevel.EXCEPTION_ANALYSIS, ex.getMessage());
			ex.printStackTrace();
			System.exit(1);
		}
		LoggerHelper.logInfo("Finished jarsigner...");
	}
	
	public static void zipalign(String appName){
		LoggerHelper.logInfo("Started zipalign...");
		final String[] command = new String[5];
		
		String toolsPath = FrameworkOptions.BUILD_TOOLS;
		if (!toolsPath.endsWith(File.separator))
			toolsPath = toolsPath + File.separator;
		
		command[0] = toolsPath + "zipalign";
		command[1] = "-v";
		command[2] = "4";
		command[3] = UtilInstrumenter.SOOT_OUTPUT_APK;
		command[4] = UtilInstrumenter.SOOT_OUTPUT_DEPLOYED_APK;
		
		
		Process p;
		try {
			p = Runtime.getRuntime().exec(command);
			
			BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = null;
			while ((line = input.readLine()) != null) {
				System.out.println(line);
			}
			
			input.close();
			
           p.waitFor();    
		}catch(Exception ex){
			LoggerHelper.logEvent(MyLevel.EXCEPTION_ANALYSIS, ex.getMessage());
			ex.printStackTrace();
			System.exit(1);
		}
		LoggerHelper.logInfo("Finished zipalign...");
	}
	
	
	public static void manipulateAndroidManifest(ProcessManifest androidManifest) {
		// process old manifest
		addHookinHelperAsApplicationIfNecessary(androidManifest);
		addInternetPermissionIfNecessary(androidManifest);
		addTracingService(androidManifest);
		addComponentCallerService(androidManifest);
		addMaxPrioForSMSReceiver(androidManifest);
		addPermissionIfNecessary("android.permission.READ_EXTERNAL_STORAGE", androidManifest);
		addPermissionIfNecessary("android.permission.WRITE_EXTERNAL_STORAGE", androidManifest);
		addPermissionIfNecessary("android.permission.WRITE_CONTACT", androidManifest);
	}
	
	
	private static void addPermissionIfNecessary(String permission, ProcessManifest androidManifest) {
		Set<String> allPermissions = androidManifest.getPermissions();
		for(String perm : allPermissions) {
			if(perm.equals(permission))
				//do nothing in case the sdcard-permission already exists
				return;
		}
		
		androidManifest.addPermission(permission);
	}

	
	@SuppressWarnings("unchecked")
	private static void addMaxPrioForSMSReceiver(ProcessManifest manifest) {
		for(AXmlNode receiver : manifest.getReceivers()) {
			for(AXmlNode receiverChild : receiver.getChildren()) {
				if(receiverChild.getTag().equals("intent-filter")) {
					//search for SMS receiver
					for(AXmlNode childChild : receiverChild.getChildren()) {
						if(childChild.getTag().equals("action")) {
							if(childChild.hasAttribute("name") && ((String)childChild.getAttribute("name").getValue()).equalsIgnoreCase("android.provider.Telephony.SMS_RECEIVED")){
								//prepare the priority filter
								if(receiverChild.hasAttribute("priority")) 
									((AXmlAttribute<Integer>)receiverChild.getAttribute("priority")).setValue(Integer.MAX_VALUE);
								else {
									AXmlAttribute<Integer> attr = new AXmlAttribute<Integer>("priority", Integer.MAX_VALUE, ANDROID_NAMESPACE);
									receiverChild.addAttribute(attr);
								}
							}
						}
					}
				}
			}
		}
	}
	
	
	private static void addComponentCallerService(ProcessManifest androidManifest) {
		AXmlNode componentCallerService = new AXmlNode("service", null, androidManifest.getApplication());
		AXmlAttribute<String> nameAttribute = new AXmlAttribute<String>("name", UtilInstrumenter.COMPONENT_CALLER_SERVICE_HELPER,  ANDROID_NAMESPACE);
		AXmlAttribute<String> exportedAttribute = new AXmlAttribute<String>("exported", "false",  ANDROID_NAMESPACE);
		componentCallerService.addAttribute(nameAttribute);
		componentCallerService.addAttribute(exportedAttribute);
		
		androidManifest.addService(componentCallerService);
	}
	
	
	private static void addHookinHelperAsApplicationIfNecessary(ProcessManifest androidManifest){
		AXmlNode application = androidManifest.getApplication();
		if(!application.hasAttribute("name")) {
			AXmlAttribute<String> nameAttribute = new AXmlAttribute<String>("name", UtilInstrumenter.HELPER_APPLICATION_FOR_HOOKING,  ANDROID_NAMESPACE);
			application.addAttribute(nameAttribute);
		}
	}
	
	
	private static void addInternetPermissionIfNecessary(ProcessManifest androidManifest) {
		String internetPerm = "android.permission.INTERNET";
		Set<String> allPermissions = androidManifest.getPermissions();
		for(String perm : allPermissions) {
			if(perm.equals(internetPerm))
				//do nothing in case the internet-permission already exists
				return;
		}
		
		androidManifest.addPermission(internetPerm);
	}
		
	
	
	private static void addTracingService(ProcessManifest androidManifest) {
		AXmlNode tracingService = new AXmlNode("service", null, androidManifest.getApplication());
		AXmlAttribute<String> nameAttribute = new AXmlAttribute<String>("name", UtilInstrumenter.HELPER_SERVICE_FOR_PATH_TRACKING,  ANDROID_NAMESPACE);
		AXmlAttribute<String> exportedAttribute = new AXmlAttribute<String>("exported", "false",  ANDROID_NAMESPACE);
		tracingService.addAttribute(nameAttribute);
		tracingService.addAttribute(exportedAttribute);
		
		androidManifest.addService(tracingService);
	}

	public static void removeOldAPKs(String appName) {
		File apkFile = new File(UtilInstrumenter.SOOT_OUTPUT_APK);
		if (apkFile.exists()) apkFile.delete();
		File apkDeployedFile = new File(UtilInstrumenter.SOOT_OUTPUT_DEPLOYED_APK);
		if (apkDeployedFile.exists()) apkDeployedFile.delete();
		
	}
	
	
	public static ProcessManifest getManifest() {					
		if(manifest == null){
			try {
				manifest = new ProcessManifest(FrameworkOptions.apkPath);
			} catch (Exception e) {
				LoggerHelper.logEvent(MyLevel.EXCEPTION_ANALYSIS, "There is a problem with the manifest: " + e.getMessage());
				e.printStackTrace();
				System.exit(-1);
			}
		}
		return manifest;
	}
}
