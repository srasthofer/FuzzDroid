package de.tu_darmstadt.sse;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.util.regex.Pattern;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

import de.tu_darmstadt.sse.commandlinelogger.LoggerHelper;
import de.tu_darmstadt.sse.commandlinelogger.MyLevel;


public class FrameworkOptions {
	public static String apkPath;
	//if there is an apk that needs a pre-analysis with Harvester, we need to store the original apk somehow (integrity check)
	public static String apkPathOriginalAPK;
	public static String androidJarPath;
	public static String packageName;
	public static String apkMD5;
	public static String resultDir;
	public static String frameworkDir;
	public static String devicePort;
	
	public static int nbSeeds = 1;
//	public static int forceTimeout = 60;
	public static int forceTimeout = 60;
//	public static int inactivityTimeout = 40;
	public static int inactivityTimeout = 30;
	public static int maxRestarts = 12;
//	public static int maxRestarts = 5;
	
	public static String KEYSTORE_PATH = "";
	public static String KEYSTORE_NAME = "";
	public static String KEYSTORE_PASSWORD = "";
	public static String BUILD_TOOLS = "";
	public static String PLATFORM_TOOLS = "";
	public static String Z3SCRIPT_LOCATION = "";
	public static boolean deployApp = false;
	public static boolean recordPathExecution = false;
	public static boolean mergeDataFlows = false;
	
	public static boolean testServer = false;
	
	public static boolean evaluationJustStartApp = false;
	public static boolean evaluationStartAppAndSimpleEvent = false;
	public static boolean evaluationOnly = false;
		
	public static boolean enableLogcatViewer = false;
	public static TraceConstructionMode traceConstructionMode = TraceConstructionMode.Genetic;
	
	
	public enum TraceConstructionMode {
		
		AnalysesOnly,
		
		Genetic,
		
		RandomCombine
	}
	
	private final Options options = new Options();
	
	public FrameworkOptions() {
		setOptions();
	}
	
	
	public static String getAPKName() {
		String[] tokens = apkPath.split(Pattern.quote(File.separator));
		String tmp = tokens[tokens.length-1];
		
		return tmp.substring(0, tmp.length()-4);
	}
			
	public void parse(String[] args) {
		CommandLineParser parser = new BasicParser();

		CommandLine cmd = null;
		try {
			cmd = parser.parse(options, args);

			if (cmd.hasOption("h") || cmd.hasOption("help"))
				showHelpMessage();
									
			//mandatory options
			apkPath = cmd.getOptionValue("apk");
			androidJarPath = cmd.getOptionValue("androidJar");
			packageName = cmd.getOptionValue("packageName");
			resultDir = cmd.getOptionValue("resultDir");
			frameworkDir = cmd.getOptionValue("frameworkDir");
			devicePort = cmd.getOptionValue("devicePort");
			
			int devicePortInt = Integer.parseInt(devicePort);
			if(devicePortInt < 5554 || devicePortInt > 5680)
				throw new RuntimeException("port number has to be an integer number between 5554 and 5680");
			if(devicePortInt%2 != 0)
				throw new RuntimeException("port number has to be an even integer number");
			
			apkMD5 = generateMD5OfFile(apkPath);
			
			//optional options
			if(cmd.hasOption("KEYSTORE_PATH")) {
				KEYSTORE_PATH = cmd.getOptionValue("KEYSTORE_PATH");
			}
			if(cmd.hasOption("KEYSTORE_NAME")) {
				KEYSTORE_NAME = cmd.getOptionValue("KEYSTORE_NAME");
			}
			if(cmd.hasOption("KEYSTORE_PASSWORD")) {
				KEYSTORE_PASSWORD = cmd.getOptionValue("KEYSTORE_PASSWORD");
			}
			if(cmd.hasOption("BUILD_TOOLS")) {
				BUILD_TOOLS = cmd.getOptionValue("BUILD_TOOLS");
			}
			if(cmd.hasOption("KEYSTORE_PATH") &&
					cmd.hasOption("KEYSTORE_NAME") &&
					cmd.hasOption("KEYSTORE_PASSWORD") &&
					cmd.hasOption("BUILD_TOOLS")
					)
				deployApp = true;
				
			if(cmd.hasOption("PLATFORM_TOOLS")) {
				PLATFORM_TOOLS = cmd.getOptionValue("PLATFORM_TOOLS");
			}
			if(cmd.hasOption("Z3SCRIPT_LOCATION")) {
				Z3SCRIPT_LOCATION = cmd.getOptionValue("Z3SCRIPT_LOCATION");
			}
			if(cmd.hasOption("TEST_SERVER")) {
				testServer = true;
			}
			if(cmd.hasOption("RECORD_PATH_EXECUTION")) {
				recordPathExecution = true;
			}			
			if(cmd.hasOption("MERGE_DATAFLOWS")) {
				mergeDataFlows = true;
			}
			if(cmd.hasOption("ORIGINAL_APK_PATH")) {
				apkPathOriginalAPK = cmd.getOptionValue("ORIGINAL_APK_PATH");
			}
			
			if(cmd.hasOption("nbSeeds")) {
				nbSeeds = Integer.parseInt(cmd.getOptionValue("nbSeeds"));
			}
			
			if(cmd.hasOption("inactivityTimeout")) {
				inactivityTimeout = Integer.parseInt(cmd.getOptionValue("inactivityTimeout"));
			}
			
			if(cmd.hasOption("maxRestarts")) {
				maxRestarts = Integer.parseInt(cmd.getOptionValue("maxRestarts"));
			}			
			if(cmd.hasOption("enableLogcatViewer")) {
				enableLogcatViewer = true;
			}
			if(cmd.hasOption("traceConstructionMode")) {
				traceConstructionMode = TraceConstructionMode.valueOf(
						cmd.getOptionValue("traceConstructionMode"));				
			}
			if(cmd.hasOption("evaluationJustStartApp")) {
				evaluationJustStartApp = true;
				evaluationOnly = true;
			}
			if(cmd.hasOption("evaluationStartAppAndSimpleEvent")) {
				evaluationStartAppAndSimpleEvent = true;
				evaluationOnly = true;
			}
		} catch (Exception e) {
			LoggerHelper.logEvent(MyLevel.EXCEPTION_ANALYSIS, e.getMessage());
			e.printStackTrace();
			showHelpMessage();
			System.exit(1);
		}
	}
	
	private void setOptions() {
		options.addOption("h", "help", false, "help");
		
		options.addOption(OptionBuilder
        		.withDescription("Path to apk")
        		.isRequired()
        		.hasArg()
        		.create("apk"));
		
		options.addOption(OptionBuilder
        		.withDescription("Path to android jar location")
        		.isRequired()
        		.hasArg()
        		.create("androidJar"));
		
		options.addOption(OptionBuilder
				.withDescription("Directory for analysis results")
				.isRequired()
				.hasArg()
				.create("resultDir"));
		
		options.addOption(OptionBuilder
				.withDescription("Path to the EvoFuzz Framework (e.g. /Users/siegfriedrasthofer/framework/")
				.isRequired()
				.hasArg()
				.create("frameworkDir"));
		
		options.addOption(OptionBuilder
				.withDescription("Please provide a port for the device; it has to be an even integer number between 5554 and 5680")
				.isRequired()
				.hasArg()
				.create("devicePort"));

		
		options.addOption("KEYSTORE_PATH", true, "Path to your keystore");
		options.addOption("KEYSTORE_NAME", true, "Name of your keystore");
		options.addOption("KEYSTORE_PASSWORD", true, "Password of your keystore");
		options.addOption("BUILD_TOOLS", true, "Path to build-tools folder in sdk");
		options.addOption("PLATFORM_TOOLS", true, "Path to platform-tools folder");
		options.addOption("TEST_SERVER", false, "runs only the server-component");
		options.addOption("Z3SCRIPT_LOCATION", true, "path to the Z3str2 python script within the Z3str2 project (e.g. /root/project/Z3/Z3-str.py)");
		options.addOption("RECORD_PATH_EXECUTION", "RECORD", true, "Path to a file location where of a .dot file. The .dot file contains the path execution (method-access, method-callers, conditions taken and return-stmts) of the app.");
		options.addOption("packageName", true, "Package name of your app");
		options.addOption("nbSeeds", true, "How often to repeat the entire experiment with a different seed. Default: "+nbSeeds);
		options.addOption("inactivityTimeout", true, "After how many seconds of inactivity to restart the app. Default: "+inactivityTimeout);
		options.addOption("maxRestarts", true, "Maximum number of restarts of the app (per experiment). -1 means infinitely often. Default: "+maxRestarts);
		options.addOption("MERGE_DATAFLOWS", false, "Merging dataflows can improve the extraction of concrete fuzzing values");
		options.addOption("ORIGINAL_APK_PATH", true, "If one needs to analyse a modfied version of an apk, we still keep the original apk. Path to original apk stored in ORIGINAL_APK_PATH");
		options.addOption("enableHooks", true, "enables specific hooks that are usually not enabled. Usability: enableHooks \"integrity\"");
		options.addOption("enableLogcatViewer", false, "once enabled, we will log whether the VM crashes (useful for bug hunting)");
		
		options.addOption("evaluationStartAppAndSimpleEvent", false, "EVALUATION-REASOSN");
		options.addOption("evaluationJustStartApp", false, "EVALUATION-REASOSN");
	}
	
	private void showHelpMessage() {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp( "Android Environment Constraint Generator", options );
	}
	
	private String generateMD5OfFile(String apkPath) {
		String md5 = null;
		try{
			MessageDigest md = MessageDigest.getInstance("MD5");
		    FileInputStream fis = new FileInputStream(apkPath);
		    byte[] dataBytes = new byte[1024];
		    
		    int nread = 0; 
		    
		    while ((nread = fis.read(dataBytes)) != -1) {
		      md.update(dataBytes, 0, nread);
		    };
	
		    byte[] mdbytes = md.digest();
		   
		    //convert the byte to hex format
		    StringBuffer sb = new StringBuffer("");
		    for (int i = 0; i < mdbytes.length; i++) {
		    	sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
		    }
		    
		    md5 = sb.toString();
		}catch(Exception ex) {
			LoggerHelper.logEvent(MyLevel.EXCEPTION_ANALYSIS, ex.getMessage());
			ex.printStackTrace();
			System.exit(-1);
		}
		return md5;
	}
}

