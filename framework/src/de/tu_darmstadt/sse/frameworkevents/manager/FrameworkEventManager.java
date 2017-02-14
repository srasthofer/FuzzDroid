package de.tu_darmstadt.sse.frameworkevents.manager;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.infoflow.android.axml.AXmlNode;
import soot.jimple.infoflow.android.manifest.ProcessManifest;
import soot.jimple.infoflow.solver.cfg.BackwardsInfoflowCFG;
import soot.util.Chain;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.logcat.LogCatListener;
import com.android.ddmlib.logcat.LogCatMessage;
import com.android.ddmlib.logcat.LogCatReceiverTask;

import de.tu_darmstadt.sse.FrameworkOptions;
import de.tu_darmstadt.sse.commandlinelogger.LoggerHelper;
import de.tu_darmstadt.sse.commandlinelogger.MyLevel;
import de.tu_darmstadt.sse.frameworkevents.ActivityEvent;
import de.tu_darmstadt.sse.frameworkevents.AddContactEvent;
import de.tu_darmstadt.sse.frameworkevents.FrameworkEvent;
import de.tu_darmstadt.sse.frameworkevents.GenericReceiver;
import de.tu_darmstadt.sse.frameworkevents.InstallApkEvent;
import de.tu_darmstadt.sse.frameworkevents.KillAppProcessEvent;
import de.tu_darmstadt.sse.frameworkevents.OnClickEvent;
import de.tu_darmstadt.sse.frameworkevents.PushFilesEvent;
import de.tu_darmstadt.sse.frameworkevents.ServiceEvent;
import de.tu_darmstadt.sse.frameworkevents.StartActivityEvent;
import de.tu_darmstadt.sse.frameworkevents.StartApkEvent;
import de.tu_darmstadt.sse.frameworkevents.UninstallAppEvent;
import de.tu_darmstadt.sse.frameworkevents.broadcastevents.FakeBroadcastEvent;
import de.tu_darmstadt.sse.frameworkevents.broadcastevents.IncomingCallEvent;
import de.tu_darmstadt.sse.frameworkevents.broadcastevents.OutgoingCallEvent;
import de.tu_darmstadt.sse.frameworkevents.broadcastevents.RealBroadcastEvent;
import de.tu_darmstadt.sse.frameworkevents.broadcastevents.SMSReceivedEvent;


public class FrameworkEventManager {
	
	private IDevice device;
	private AndroidDebugBridge adb = null;
	private static final long ADB_CONNECT_TIMEOUT_MS = 5000;	
	private static final long ADB_CONNECT_TIME_STEP_MS = ADB_CONNECT_TIMEOUT_MS / 10;

	
	private static FrameworkEventManager frameworkEventManager = null;

	public static FrameworkEventManager getEventManager() {
		if (frameworkEventManager == null)
			frameworkEventManager = new FrameworkEventManager();
		return frameworkEventManager;
	}

	
	public void connectToAndroidDevice() {
		LoggerHelper.logEvent(MyLevel.RUNTIME, "Connecting to ADB...");				
		if(adb == null) {
		    AndroidDebugBridge.init(false);		    			
	//		AndroidDebugBridge bridge = AndroidDebugBridge.createBridge(
	//				FrameworkOptions.PLATFORM_TOOLS + File.separator + "adb", true);
			adb = AndroidDebugBridge.createBridge();				
		}
		
		waitForDevice();		
		this.device = getDevice(FrameworkOptions.devicePort);
		if(this.device == null) {
			LoggerHelper.logEvent(MyLevel.EXCEPTION_RUNTIME, String.format("Device with port %s not found! -- retry it", FrameworkOptions.devicePort));
			connectToAndroidDevice();
		}
		LoggerHelper.logEvent(MyLevel.RUNTIME, "Successfully connected to ADB...");		
	}

	
	public Object sendEvent(FrameworkEvent event) {
		if (device.isOffline()) {
			LoggerHelper
					.logWarning("Device is offline! Trying to restart it...");
			connectToAndroidDevice();
		}

		return event.onEventReceived(device);
	}		
	
	
	public void installApp(String packageName) {
		sendEvent(new InstallApkEvent(packageName));
	}
	
	
	public void startApp(String packageName) {
		sendEvent(new StartApkEvent(packageName));
	}
	
	public void startActivity(String packageName, String activityName) {
		sendEvent(new StartActivityEvent(packageName, activityName));
	}
	
	public void startService(String packageName, String servicePath) {
		sendEvent(new ServiceEvent(packageName, servicePath));
	}
	
	
	public void killAppProcess(String packageName) {
		LoggerHelper.logEvent(MyLevel.RUNTIME, "killing application");
		sendEvent(new KillAppProcessEvent(packageName));
	}
	
	
	public void uninstallAppProcess(String packageName) {
		sendEvent(new UninstallAppEvent(packageName));
	}
	
	
	public void pushFiles(String dirPath) {
		sendEvent(new PushFilesEvent(dirPath));
	}

	
	public void addContacts(String packageName) {
		sendEvent(new AddContactEvent(packageName));
	}
	
	
	public Set<FrameworkEvent> extractInitalEventsForReachingTarget(Unit targetLocation, BackwardsInfoflowCFG backwardsCFG, ProcessManifest manifest) {
		Set<Unit> headUnits = getAllInitalMethodCalls(targetLocation, backwardsCFG);
		Set<FrameworkEvent> androidEvents = getAndroidEventsFromManifest(backwardsCFG, headUnits, manifest);
		return androidEvents;
	}
	
	
	private Set<Unit> getAllInitalMethodCalls(Unit targetLocation, BackwardsInfoflowCFG backwardsCFG) {
		Set<Unit> headUnits = new HashSet<Unit>();						
		Set<Unit> reachedUnits = new HashSet<Unit>();
		LinkedList<Unit> worklist = new LinkedList<Unit>();
		Unit previousUnit = null;
		worklist.add(targetLocation);
		
		while(!worklist.isEmpty()) {
			// get front element
			Unit currentUnit = worklist.removeFirst();	
			
			if(reachedUnits.contains(currentUnit)){
				previousUnit = currentUnit;
				continue;
			}else
				reachedUnits.add(currentUnit);
			
			SootMethod currentMethod = backwardsCFG.getMethodOf(currentUnit);
			//we reached the head unit
			if(currentMethod.getDeclaringClass().toString().equals("dummyMainClass")) {	
				if(previousUnit == null)
					throw new RuntimeException("there should be a previous unit");
				
				headUnits.add(previousUnit);
				
				continue;								
			}
			
			//in case we reached the start of the method (vice verse in backward analysis)
			if(backwardsCFG.isExitStmt(currentUnit)) {		
				SootMethod sm = backwardsCFG.getMethodOf(currentUnit);
				//first: get all callers
				Collection<Unit> callers = backwardsCFG.getCallersOf(sm);
				for(Unit caller : callers) {						
					//get the predecessors (aka succs of cfg) of the callers and add them to the worklist
					List<Unit> succOfCaller = backwardsCFG.getSuccsOf(caller);
					for(Unit unit : succOfCaller)
						worklist.addFirst(unit);
				}
				previousUnit = currentUnit;
				//there is no need for further progress
				continue;
			}
			
			List<Unit> nextUnits = backwardsCFG.getSuccsOf(currentUnit);
			for(Unit unit : nextUnits)
				worklist.addFirst(unit);
			previousUnit = currentUnit;
		}
		
		return headUnits;
	}
	
	
	private Set<FrameworkEvent> getAndroidEventsFromManifest(BackwardsInfoflowCFG backwardsCFG, Set<Unit> headUnits, ProcessManifest manifest) {
		Set<FrameworkEvent> events = new HashSet<FrameworkEvent>();		
			for(Unit head : headUnits) {
				SootMethod sm = backwardsCFG.getMethodOf(head);
				SootClass sc = sm.getDeclaringClass();
				SootClass superClass = sc.getSuperclass();
				Chain<SootClass> interfaceClasses = sc.getInterfaces();				
				
				if(superClass.getName().equals("android.app.Service")) {
					String packageName = manifest.getPackageName();
					String servicePath = sc.getName();
					events.add(new ServiceEvent(packageName, servicePath));
				}
				else if(superClass.getName().equals("android.content.BroadcastReceiver")) {
					for(AXmlNode receiver : manifest.getReceivers()) {
						if(receiver.hasAttribute("name")){
							String receiverName = (String)receiver.getAttribute("name").getValue();
							//now we have to find the correct name of the receiver class
							String fullyQualifiedReceiverName = getFullyQualifiedName(manifest, receiverName);
							
							if(sc.getName().equals(fullyQualifiedReceiverName)) {
								for(AXmlNode children1 : receiver.getChildren()) {
									if(children1.getTag().equals("intent-filter")) {
										Set<String> actions = new HashSet<String>();
										Set<String> mimeTypes = new HashSet<String>();
										for(AXmlNode children2 : children1.getChildren()) {
											if(children2.getTag().equals("action")) {
												String actionName = (String)children2.getAttribute("name").getValue();																										
												actions.add(actionName);																																				
											}
											else if(children2.getTag().equals("data")) {
												String mimeType = (String)children2.getAttribute("mimeType").getValue();
												mimeTypes.add(mimeType);
											}
										}
										
										String mimeType = null;
										if(mimeTypes.size() > 1) {
											LoggerHelper.logEvent(MyLevel.EXCEPTION_ANALYSIS, "THERE IS MORE THAN 1 DATA ELEMETN IN THE INTENT-FILTER");
											mimeType = mimeTypes.iterator().next();
										}
										else if(mimeTypes.size() == 1)
											mimeType = mimeTypes.iterator().next();
										
										for(String action : actions)
											events.add(getCorrectBroadCast(action, mimeType, fullyQualifiedReceiverName, manifest));
									}
									//no intent defined in the manifest; maybe dynamically defined during runtime;
									//we try to call it anyway
									else {
										events.add(new FakeBroadcastEvent(fullyQualifiedReceiverName, null, null, manifest.getPackageName()));
									}
								}
							}
						}
					}
					if(events.isEmpty())
						LoggerHelper.logEvent(MyLevel.TODO, "generateAndroidEvents: maybe a dynamic generated broadcast?");
				}
				else if(superClass.getName().equals("android.app.Activity")) {
					//is it the main launchable activity; if yes, we do not have to add any android event since 
					//we are opening the app anyway
					if(!isLaunchableAcitivity(sc, manifest)) {
						String packageName = manifest.getPackageName();
						String activityPath = sc.getName();
						events.add(new ActivityEvent(packageName, activityPath));
					}	
					else
						continue;
				}
				else{
					//check for listeners (interfaces)
					for(SootClass interfaceClass : interfaceClasses) {
						if(interfaceClass.getName().equals("android.view.View$OnClickListener")){
							events.add(new OnClickEvent(sc.getName(), manifest.getPackageName()));
						}
					}
					if(events.isEmpty())
						LoggerHelper.logEvent(MyLevel.TODO, "generateAndroidEvents: did not find a proper event for head: " + head.toString());
				}								
											
		}
		return events;
	}
	
	
	private String getFullyQualifiedName(ProcessManifest manifest, String componentName) {
		String fullyQualifiedName = null;
		if(componentName.startsWith(".")) {
			String packageName = manifest.getPackageName();
			fullyQualifiedName = packageName + componentName;
		}
		//fully qualified name
		else if(componentName.contains(".")) {
			fullyQualifiedName = componentName;
		}
		//not documented, but still working
		else {
			String packageName = manifest.getPackageName();
			fullyQualifiedName = packageName + "." + componentName;
		}
		return fullyQualifiedName;
	}
	
	
	private boolean isLaunchableAcitivity(SootClass sc, ProcessManifest manifest) {
		Set<AXmlNode> launchableActivities = manifest.getLaunchableActivities();
		for(AXmlNode node : launchableActivities) {
			if(node.hasAttribute("name")) {
				String activityName = (String)node.getAttribute("name").getValue();
				activityName = getFullyQualifiedName(manifest, activityName);
				
				if(activityName.equals(sc.getName()))
					return true;
			}
		}
		return false;
	}
	
	
	private FrameworkEvent getCorrectBroadCast(String actionName, String mimeType, String fullyQualifiedReceiverName, ProcessManifest manifest) {
		FrameworkEvent event = null;
		
		if(actionName.equalsIgnoreCase("android.provider.telephony.SMS_RECEIVED"))
			event = new SMSReceivedEvent();		
		else if(actionName.equalsIgnoreCase("android.intent.action.NEW_OUTGOING_CALL"))
			event = new OutgoingCallEvent();
		else if(actionName.equalsIgnoreCase("android.intent.action.NEW_INCOMING_CALL"))
			event = new IncomingCallEvent();
		//all these actions are taken from android-sdks/platforms/android-23/data/broadcast_actions.txt
		else if(actionName.equalsIgnoreCase("android.intent.action.BOOT_COMPLETED")
				|| actionName.equalsIgnoreCase("android.app.action.ACTION_PASSWORD_CHANGED")
				|| actionName.equalsIgnoreCase("android.app.action.ACTION_PASSWORD_EXPIRING")
				|| actionName.equalsIgnoreCase("android.app.action.ACTION_PASSWORD_FAILED")
				|| actionName.equalsIgnoreCase("android.app.action.ACTION_PASSWORD_SUCCEEDED")
				|| actionName.equalsIgnoreCase("android.app.action.DEVICE_ADMIN_DISABLED")
				|| actionName.equalsIgnoreCase("android.app.action.DEVICE_ADMIN_DISABLE_REQUESTED")
				|| actionName.equalsIgnoreCase("android.app.action.DEVICE_ADMIN_ENABLED")
				|| actionName.equalsIgnoreCase("android.app.action.DEVICE_OWNER_CHANGED")
				|| actionName.equalsIgnoreCase("android.app.action.INTERRUPTION_FILTER_CHANGED")
				|| actionName.equalsIgnoreCase("android.app.action.LOCK_TASK_ENTERING")
				|| actionName.equalsIgnoreCase("android.app.action.LOCK_TASK_EXITING")
				|| actionName.equalsIgnoreCase("android.app.action.NEXT_ALARM_CLOCK_CHANGED")
				|| actionName.equalsIgnoreCase("android.app.action.NOTIFICATION_POLICY_ACCESS_GRANTED_CHANGED")
				|| actionName.equalsIgnoreCase("android.app.action.NOTIFICATION_POLICY_CHANGED")
				|| actionName.equalsIgnoreCase("android.app.action.PROFILE_PROVISIONING_COMPLETE")
				|| actionName.equalsIgnoreCase("android.app.action.SYSTEM_UPDATE_POLICY_CHANGED")
				|| actionName.equalsIgnoreCase("android.bluetooth.a2dp.profile.action.CONNECTION_STATE_CHANGED")
				|| actionName.equalsIgnoreCase("android.bluetooth.a2dp.profile.action.PLAYING_STATE_CHANGED")
				|| actionName.equalsIgnoreCase("android.bluetooth.adapter.action.CONNECTION_STATE_CHANGED")
				|| actionName.equalsIgnoreCase("android.bluetooth.adapter.action.DISCOVERY_FINISHED")
				|| actionName.equalsIgnoreCase("android.bluetooth.adapter.action.DISCOVERY_STARTED")
				|| actionName.equalsIgnoreCase("android.bluetooth.adapter.action.LOCAL_NAME_CHANGED")
				|| actionName.equalsIgnoreCase("android.bluetooth.adapter.action.SCAN_MODE_CHANGED")
				|| actionName.equalsIgnoreCase("android.bluetooth.adapter.action.STATE_CHANGED")
				|| actionName.equalsIgnoreCase("android.bluetooth.device.action.ACL_CONNECTED")
				|| actionName.equalsIgnoreCase("android.bluetooth.device.action.ACL_DISCONNECTED")
				|| actionName.equalsIgnoreCase("android.bluetooth.device.action.ACL_DISCONNECT_REQUESTED")
				|| actionName.equalsIgnoreCase("android.bluetooth.device.action.BOND_STATE_CHANGED")
				|| actionName.equalsIgnoreCase("android.bluetooth.device.action.CLASS_CHANGED")
				|| actionName.equalsIgnoreCase("android.bluetooth.device.action.FOUND")
				|| actionName.equalsIgnoreCase("android.bluetooth.device.action.NAME_CHANGED")
				|| actionName.equalsIgnoreCase("android.bluetooth.device.action.PAIRING_REQUEST")
				|| actionName.equalsIgnoreCase("android.bluetooth.device.action.UUID")
				|| actionName.equalsIgnoreCase("android.bluetooth.devicepicker.action.DEVICE_SELECTED")
				|| actionName.equalsIgnoreCase("android.bluetooth.devicepicker.action.LAUNCH")
				|| actionName.equalsIgnoreCase("android.bluetooth.headset.action.VENDOR_SPECIFIC_HEADSET_EVENT")
				|| actionName.equalsIgnoreCase("android.bluetooth.headset.profile.action.AUDIO_STATE_CHANGED")
				|| actionName.equalsIgnoreCase("android.bluetooth.headset.profile.action.CONNECTION_STATE_CHANGED")
				|| actionName.equalsIgnoreCase("android.bluetooth.input.profile.action.CONNECTION_STATE_CHANGED")
				|| actionName.equalsIgnoreCase("android.bluetooth.pan.profile.action.CONNECTION_STATE_CHANGED")
				|| actionName.equalsIgnoreCase("android.hardware.action.NEW_PICTURE")
				|| actionName.equalsIgnoreCase("android.hardware.action.NEW_VIDEO")
				|| actionName.equalsIgnoreCase("android.hardware.hdmi.action.OSD_MESSAGE")
				|| actionName.equalsIgnoreCase("android.hardware.input.action.QUERY_KEYBOARD_LAYOUTS")
				|| actionName.equalsIgnoreCase("android.intent.action.ACTION_POWER_CONNECTED")
				|| actionName.equalsIgnoreCase("android.intent.action.ACTION_POWER_DISCONNECTED")
				|| actionName.equalsIgnoreCase("android.intent.action.ACTION_SHUTDOWN")
				|| actionName.equalsIgnoreCase("android.intent.action.AIRPLANE_MODE")
				|| actionName.equalsIgnoreCase("android.intent.action.APPLICATION_RESTRICTIONS_CHANGED")
				|| actionName.equalsIgnoreCase("android.intent.action.BATTERY_CHANGED")
				|| actionName.equalsIgnoreCase("android.intent.action.BATTERY_LOW")
				|| actionName.equalsIgnoreCase("android.intent.action.BATTERY_OKAY")
				|| actionName.equalsIgnoreCase("android.intent.action.BOOT_COMPLETED")
				|| actionName.equalsIgnoreCase("android.intent.action.CAMERA_BUTTON")
				|| actionName.equalsIgnoreCase("android.intent.action.CONFIGURATION_CHANGED")
				|| actionName.equalsIgnoreCase("android.intent.action.CONTENT_CHANGED")
				|| actionName.equalsIgnoreCase("android.intent.action.DATA_SMS_RECEIVED")
				|| actionName.equalsIgnoreCase("android.intent.action.DATE_CHANGED")
				|| actionName.equalsIgnoreCase("android.intent.action.DEVICE_STORAGE_LOW")
				|| actionName.equalsIgnoreCase("android.intent.action.DEVICE_STORAGE_OK")
				|| actionName.equalsIgnoreCase("android.intent.action.DOCK_EVENT")
				|| actionName.equalsIgnoreCase("android.intent.action.DOWNLOAD_COMPLETE")
				|| actionName.equalsIgnoreCase("android.intent.action.DOWNLOAD_NOTIFICATION_CLICKED")
				|| actionName.equalsIgnoreCase("android.intent.action.DREAMING_STARTED")
				|| actionName.equalsIgnoreCase("android.intent.action.DREAMING_STOPPED")
				|| actionName.equalsIgnoreCase("android.intent.action.EXTERNAL_APPLICATIONS_AVAILABLE")
				|| actionName.equalsIgnoreCase("android.intent.action.EXTERNAL_APPLICATIONS_UNAVAILABLE")
				|| actionName.equalsIgnoreCase("android.intent.action.FETCH_VOICEMAIL")
				|| actionName.equalsIgnoreCase("android.intent.action.GTALK_CONNECTED")
				|| actionName.equalsIgnoreCase("android.intent.action.GTALK_DISCONNECTED")
				|| actionName.equalsIgnoreCase("android.intent.action.HEADSET_PLUG")
				|| actionName.equalsIgnoreCase("android.intent.action.HEADSET_PLUG")
				|| actionName.equalsIgnoreCase("android.intent.action.INPUT_METHOD_CHANGED")
				|| actionName.equalsIgnoreCase("android.intent.action.LOCALE_CHANGED")
				|| actionName.equalsIgnoreCase("android.intent.action.MANAGE_PACKAGE_STORAGE")
				|| actionName.equalsIgnoreCase("android.intent.action.MEDIA_BAD_REMOVAL")
				|| actionName.equalsIgnoreCase("android.intent.action.MEDIA_BUTTON")
				|| actionName.equalsIgnoreCase("android.intent.action.MEDIA_CHECKING")
				|| actionName.equalsIgnoreCase("android.intent.action.MEDIA_EJECT")
				|| actionName.equalsIgnoreCase("android.intent.action.MEDIA_MOUNTED")
				|| actionName.equalsIgnoreCase("android.intent.action.MEDIA_NOFS")
				|| actionName.equalsIgnoreCase("android.intent.action.MEDIA_REMOVED")
				|| actionName.equalsIgnoreCase("android.intent.action.MEDIA_SCANNER_FINISHED")
				|| actionName.equalsIgnoreCase("android.intent.action.MEDIA_SCANNER_SCAN_FILE")
				|| actionName.equalsIgnoreCase("android.intent.action.MEDIA_SCANNER_STARTED")
				|| actionName.equalsIgnoreCase("android.intent.action.MEDIA_SHARED")
				|| actionName.equalsIgnoreCase("android.intent.action.MEDIA_UNMOUNTABLE")
				|| actionName.equalsIgnoreCase("android.intent.action.MEDIA_UNMOUNTED")
				|| actionName.equalsIgnoreCase("android.intent.action.MY_PACKAGE_REPLACED")
				|| actionName.equalsIgnoreCase("android.intent.action.NEW_OUTGOING_CALL")
				|| actionName.equalsIgnoreCase("android.intent.action.NEW_VOICEMAIL")
				|| actionName.equalsIgnoreCase("android.intent.action.PACKAGE_ADDED")
				|| actionName.equalsIgnoreCase("android.intent.action.PACKAGE_CHANGED")
				|| actionName.equalsIgnoreCase("android.intent.action.PACKAGE_DATA_CLEARED")
				|| actionName.equalsIgnoreCase("android.intent.action.PACKAGE_FIRST_LAUNCH")
				|| actionName.equalsIgnoreCase("android.intent.action.PACKAGE_FULLY_REMOVED")
				|| actionName.equalsIgnoreCase("android.intent.action.PACKAGE_INSTALL")
				|| actionName.equalsIgnoreCase("android.intent.action.PACKAGE_NEEDS_VERIFICATION")
				|| actionName.equalsIgnoreCase("android.intent.action.PACKAGE_REMOVED")
				|| actionName.equalsIgnoreCase("android.intent.action.PACKAGE_REPLACED")
				|| actionName.equalsIgnoreCase("android.intent.action.PACKAGE_RESTARTED")
				|| actionName.equalsIgnoreCase("android.intent.action.PACKAGE_VERIFIED")
				|| actionName.equalsIgnoreCase("android.intent.action.PHONE_STATE")
				|| actionName.equalsIgnoreCase("android.intent.action.PROVIDER_CHANGED")
				|| actionName.equalsIgnoreCase("android.intent.action.PROXY_CHANGE")
				|| actionName.equalsIgnoreCase("android.intent.action.REBOOT")
				|| actionName.equalsIgnoreCase("android.intent.action.SCREEN_OFF")
				|| actionName.equalsIgnoreCase("android.intent.action.SCREEN_ON")
				|| actionName.equalsIgnoreCase("android.intent.action.TIMEZONE_CHANGED")
				|| actionName.equalsIgnoreCase("android.intent.action.TIME_SET")
				|| actionName.equalsIgnoreCase("android.intent.action.TIME_TICK")
				|| actionName.equalsIgnoreCase("android.intent.action.UID_REMOVED")
				|| actionName.equalsIgnoreCase("android.intent.action.USER_PRESENT")
				|| actionName.equalsIgnoreCase("android.intent.action.WALLPAPER_CHANGED")
				|| actionName.equalsIgnoreCase("android.media.ACTION_SCO_AUDIO_STATE_UPDATED")
				|| actionName.equalsIgnoreCase("android.media.AUDIO_BECOMING_NOISY")
				|| actionName.equalsIgnoreCase("android.media.RINGER_MODE_CHANGED")
				|| actionName.equalsIgnoreCase("android.media.SCO_AUDIO_STATE_CHANGED")
				|| actionName.equalsIgnoreCase("android.media.VIBRATE_SETTING_CHANGED")
				|| actionName.equalsIgnoreCase("android.media.action.CLOSE_AUDIO_EFFECT_CONTROL_SESSION")
				|| actionName.equalsIgnoreCase("android.media.action.HDMI_AUDIO_PLUG")
				|| actionName.equalsIgnoreCase("android.media.action.OPEN_AUDIO_EFFECT_CONTROL_SESSION")
				|| actionName.equalsIgnoreCase("android.net.conn.BACKGROUND_DATA_SETTING_CHANGED")
				|| actionName.equalsIgnoreCase("android.net.conn.CONNECTIVITY_CHANGE")
				|| actionName.equalsIgnoreCase("android.net.nsd.STATE_CHANGED")
				|| actionName.equalsIgnoreCase("android.net.scoring.SCORER_CHANGED")
				|| actionName.equalsIgnoreCase("android.net.scoring.SCORE_NETWORKS")
				|| actionName.equalsIgnoreCase("android.net.wifi.NETWORK_IDS_CHANGED")
				|| actionName.equalsIgnoreCase("android.net.wifi.RSSI_CHANGED")
				|| actionName.equalsIgnoreCase("android.net.wifi.SCAN_RESULTS")
				|| actionName.equalsIgnoreCase("android.net.wifi.STATE_CHANGE")
				|| actionName.equalsIgnoreCase("android.net.wifi.WIFI_STATE_CHANGED")
				|| actionName.equalsIgnoreCase("android.net.wifi.p2p.CONNECTION_STATE_CHANGE")
				|| actionName.equalsIgnoreCase("android.net.wifi.p2p.DISCOVERY_STATE_CHANGE")
				|| actionName.equalsIgnoreCase("android.net.wifi.p2p.PEERS_CHANGED")
				|| actionName.equalsIgnoreCase("android.net.wifi.p2p.STATE_CHANGED")
				|| actionName.equalsIgnoreCase("android.net.wifi.p2p.THIS_DEVICE_CHANGED")
				|| actionName.equalsIgnoreCase("android.net.wifi.supplicant.CONNECTION_CHANGE")
				|| actionName.equalsIgnoreCase("android.net.wifi.supplicant.STATE_CHANGE")
				|| actionName.equalsIgnoreCase("android.nfc.action.ADAPTER_STATE_CHANGED")
				|| actionName.equalsIgnoreCase("android.os.action.DEVICE_IDLE_MODE_CHANGED")
				|| actionName.equalsIgnoreCase("android.os.action.POWER_SAVE_MODE_CHANGED")
				|| actionName.equalsIgnoreCase("android.provider.Telephony.SIM_FULL")
				|| actionName.equalsIgnoreCase("android.provider.Telephony.SMS_CB_RECEIVED")
				|| actionName.equalsIgnoreCase("android.provider.Telephony.SMS_DELIVER")
				|| actionName.equalsIgnoreCase("android.provider.Telephony.SMS_EMERGENCY_CB_RECEIVED")
				|| actionName.equalsIgnoreCase("android.provider.Telephony.SMS_RECEIVED")
				|| actionName.equalsIgnoreCase("android.provider.Telephony.SMS_REJECTED")
				|| actionName.equalsIgnoreCase("android.provider.Telephony.SMS_SERVICE_CATEGORY_PROGRAM_DATA_RECEIVED")
				|| actionName.equalsIgnoreCase("android.provider.Telephony.WAP_PUSH_DELIVER")
				|| actionName.equalsIgnoreCase("android.provider.Telephony.WAP_PUSH_RECEIVED")
				|| actionName.equalsIgnoreCase("android.speech.tts.TTS_QUEUE_PROCESSING_COMPLETED")
				|| actionName.equalsIgnoreCase("android.speech.tts.engine.TTS_DATA_INSTALLED") 
		)
			event = new FakeBroadcastEvent(fullyQualifiedReceiverName, actionName, mimeType, manifest.getPackageName());		
		else			
			event = new RealBroadcastEvent(null, actionName, mimeType);
		
		return event;
	}
	
	
	private void waitForDevice() {
		long start = System.currentTimeMillis();
		while (adb.hasInitialDeviceList() == false) {
			long timeLeft = start + ADB_CONNECT_TIMEOUT_MS - System.currentTimeMillis();
			
			if (timeLeft <= 0) {
				break;
			}
			
			try{
				Thread.sleep(ADB_CONNECT_TIME_STEP_MS);
			}catch(Exception ex) {
				LoggerHelper.logEvent(MyLevel.EXCEPTION_ANALYSIS, ex.getMessage());
				ex.printStackTrace();
			}
		}
		
		if(!adb.hasInitialDeviceList()) {
			LoggerHelper.logEvent(MyLevel.EXCEPTION_RUNTIME, "NOT POSSIBLE TO CONNECT TO ADB -- giving up and close program!");
			System.exit(-1);
		}
		
		int count = 0;		
		while (adb.getDevices().length == 0) {
			try {
				Thread.sleep(5000);
				LoggerHelper.logEvent(MyLevel.RUNTIME, "not possible to find a device...");
				count++;
			} catch (InterruptedException e) {
				LoggerHelper.logEvent(MyLevel.EXCEPTION_ANALYSIS, e.getMessage());
				e.printStackTrace();
				AndroidDebugBridge.terminate();
			}
			if (count > 50) {
				LoggerHelper
						.logEvent(MyLevel.RUNTIME, "After 100 seconds not able to find an Android device. Shutting down...");
				AndroidDebugBridge.terminate();				
			}
		}
	}
	
	
	private IDevice getDevice(String devicePort) {
		for (IDevice iDev : adb.getDevices()) {
			if (iDev.isEmulator() && iDev.getSerialNumber().contains(devicePort)) {								
				LoggerHelper.logEvent(MyLevel.RUNTIME, "Successfully connected to emulator: "
						+ iDev.getAvdName());
				return iDev;
			}
		}
		return null;
	}
	
	
	public void startLogcatCrashViewer() {
		try {
			device.executeShellCommand("logcat -c", new GenericReceiver(), 10000, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			e.printStackTrace();
		} 
		LogCatReceiverTask lcrt = new LogCatReceiverTask(device);
		lcrt.addLogCatListener(new LogCatListener() {
			
			@Override
			public void log(List<LogCatMessage> msgList) {
				for(LogCatMessage lcmsg : msgList) {
					String msg = lcmsg.getMessage();
					
					if(/*msg.contains("Shutting down VM") ||*/
							msg.contains("VFY:") 
							)
						LoggerHelper.logEvent(MyLevel.VMCRASH, String.format("############### VM CRASHED ###############\n%s", lcmsg.toString()));
				}
			}
		});

		Thread logcatViewerThread = new Thread(lcrt);
		logcatViewerThread.start();
		
	}
}
