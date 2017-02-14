package de.tu_darmstadt.sse.additionalappclasses.hookdefinitions;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import android.net.NetworkInfo.DetailedState;
import de.tu_darmstadt.sse.additionalappclasses.hooking.FieldHookInfo;
import de.tu_darmstadt.sse.additionalappclasses.hooking.HookInfo;
import de.tu_darmstadt.sse.additionalappclasses.hooking.MethodHookInfo;
import de.tu_darmstadt.sse.sharedclasses.util.Pair;


public class PersistentHookDefinitions implements Hook{
	
	@Override
	public Set<HookInfo> initializeHooks() {
		Set<HookInfo> allPersistentHooks = new HashSet<HookInfo>();
//		allPersistentHooks.addAll(emulatorCheckHooks());	
//		allPersistentHooks.addAll(networkRelatedHooks());
		return allPersistentHooks;
	}		

	
	private Set<HookInfo> emulatorCheckHooks() {
		Set<HookInfo> emulatorHoocks = new HashSet<HookInfo>();
		
		emulatorHoocks.addAll(buildSpecificEmulatorCheckHooks());
		emulatorHoocks.addAll(telephonyManagerSpecificEmulatorCheckHooks());				
		emulatorHoocks.addAll(applicationPackageManagerEmulatorCheckHooks());               

        return emulatorHoocks;            
	}
	
	
	private Set<HookInfo> buildSpecificEmulatorCheckHooks() {
		Set<HookInfo> buildSpecificEmulatorCheckHooks = new HashSet<HookInfo>();
		
		FieldHookInfo build_abi = new FieldHookInfo("<android.os.Build: java.lang.String CPU_ABI>");
		build_abi.persistentHookAfter("armeabi-v7a");
        FieldHookInfo build_abi2 = new FieldHookInfo("<android.os.Build: java.lang.String CPU_ABI2>");
        build_abi2.persistentHookAfter("armeabi");
        FieldHookInfo build_board = new FieldHookInfo("<android.os.Build: java.lang.String BOARD>");
        build_board.persistentHookAfter("MAKO");
        FieldHookInfo build_brand = new FieldHookInfo("<android.os.Build: java.lang.String BRAND>");
        build_brand.persistentHookAfter("google");
        FieldHookInfo build_device = new FieldHookInfo("<android.os.Build: java.lang.String DEVICE>");
        build_device.persistentHookAfter("mako");        
        FieldHookInfo build_fingerprint = new FieldHookInfo("<android.os.Build: java.lang.String FINGERPRINT>");
        build_fingerprint.persistentHookAfter("google/occam/mako:4.4.2/KOT49H/937116:user/release-keys");        
        FieldHookInfo build_hardware = new FieldHookInfo("<android.os.Build: java.lang.String HARDWARE>");
        build_hardware.persistentHookAfter("mako");        
        FieldHookInfo build_host = new FieldHookInfo("<android.os.Build: java.lang.String HOST>");
        build_host.persistentHookAfter("kpfj3.cbf.corp.google.com");        
        FieldHookInfo build_id = new FieldHookInfo("<android.os.Build: java.lang.String ID>");
        build_id.persistentHookAfter("KOT49H");        
        FieldHookInfo build_manufacturer = new FieldHookInfo("<android.os.Build: java.lang.String MANUFACTURER>");
        build_manufacturer.persistentHookAfter("LGE");        
        FieldHookInfo build_model = new FieldHookInfo("<android.os.Build: java.lang.String MODEL>");
        build_model.persistentHookAfter("Nexus 4");        
        FieldHookInfo build_product = new FieldHookInfo("<android.os.Build: java.lang.String PRODUCT>");
        build_product.persistentHookAfter("occam");        
        FieldHookInfo build_radio = new FieldHookInfo("<android.os.Build: java.lang.String RADIO>");
        build_radio.persistentHookAfter("unknown");        
        FieldHookInfo build_serial = new FieldHookInfo("<android.os.Build: java.lang.String SERIAL>");
        build_serial.persistentHookAfter("016ff0251853784a");        
        FieldHookInfo build_tags = new FieldHookInfo("<android.os.Build: java.lang.String TAGS>");
        build_tags.persistentHookAfter("release-keys");        
        FieldHookInfo build_user = new FieldHookInfo("<android.os.Build: java.lang.String USER>");
        build_user.persistentHookAfter("android-build");
		
        
        buildSpecificEmulatorCheckHooks.add(build_abi);
        buildSpecificEmulatorCheckHooks.add(build_abi2);
        buildSpecificEmulatorCheckHooks.add(build_board);
        buildSpecificEmulatorCheckHooks.add(build_brand);
        buildSpecificEmulatorCheckHooks.add(build_device);        
        buildSpecificEmulatorCheckHooks.add(build_fingerprint);        
        buildSpecificEmulatorCheckHooks.add(build_hardware);        
        buildSpecificEmulatorCheckHooks.add(build_host);        
        buildSpecificEmulatorCheckHooks.add(build_id);        
        buildSpecificEmulatorCheckHooks.add(build_manufacturer);        
        buildSpecificEmulatorCheckHooks.add(build_model);        
        buildSpecificEmulatorCheckHooks.add(build_product);        
        buildSpecificEmulatorCheckHooks.add(build_radio);        
        buildSpecificEmulatorCheckHooks.add(build_serial);
        buildSpecificEmulatorCheckHooks.add(build_tags);
        buildSpecificEmulatorCheckHooks.add(build_user);
		
		return buildSpecificEmulatorCheckHooks;
	}
	
	
	private Set<HookInfo> telephonyManagerSpecificEmulatorCheckHooks() {
		Set<HookInfo> telephonyManagerSpecificEmulatorCheckHooks = new HashSet<HookInfo>();
		
//		 MethodHookInfo deviceId = new MethodHookInfo("<android.telephony.TelephonyManager: java.lang.String getDeviceId()>");
//		 deviceId.persistentHookAfter("353918056991322");	        
		 MethodHookInfo line1Number = new MethodHookInfo("<android.telephony.TelephonyManager: java.lang.String getLine1Number()>");
		 line1Number.persistentHookAfter("");
		 MethodHookInfo simSerial = new MethodHookInfo("<android.telephony.TelephonyManager: java.lang.String getSimSerialNumber()>");
		 line1Number.persistentHookAfter("8923440000000000003");
		 MethodHookInfo softwareVersion = new MethodHookInfo("<android.telephony.TelephonyManager: java.lang.String getDeviceSoftwareVersion()>");
		 line1Number.persistentHookAfter("57");
		 MethodHookInfo countryIso = new MethodHookInfo("<android.telephony.TelephonyManager: java.lang.String getNetworkCountryIso()>");
		 countryIso.persistentHookAfter("de");
		 MethodHookInfo networkOperator = new MethodHookInfo("<android.telephony.TelephonyManager: java.lang.String getNetworkOperator()>");
		 networkOperator.persistentHookAfter("26207");
		 MethodHookInfo simCountryIso = new MethodHookInfo("<android.telephony.TelephonyManager: java.lang.String getSimCountryIso()>");
		 simCountryIso.persistentHookAfter("de");
		 MethodHookInfo voiceMail = new MethodHookInfo("<android.telephony.TelephonyManager: java.lang.String getVoiceMailNumber()>");
		 voiceMail.persistentHookAfter("+491793000333");
		 MethodHookInfo phoneType = new MethodHookInfo("<android.telephony.TelephonyManager: java.lang.String getPhoneType()>");
		 phoneType.persistentHookAfter("1");
		 MethodHookInfo networkType = new MethodHookInfo("<android.telephony.TelephonyManager: java.lang.String getNetworkType()>");
		 phoneType.persistentHookAfter("1");
		 
		 
		 
//		 telephonyManagerSpecificEmulatorCheckHooks.add(deviceId);
		 telephonyManagerSpecificEmulatorCheckHooks.add(line1Number);
		 telephonyManagerSpecificEmulatorCheckHooks.add(simSerial);
		 telephonyManagerSpecificEmulatorCheckHooks.add(softwareVersion);
		 telephonyManagerSpecificEmulatorCheckHooks.add(countryIso);
		 telephonyManagerSpecificEmulatorCheckHooks.add(networkOperator);
		 telephonyManagerSpecificEmulatorCheckHooks.add(simCountryIso);
		 telephonyManagerSpecificEmulatorCheckHooks.add(voiceMail);
		 telephonyManagerSpecificEmulatorCheckHooks.add(phoneType);
		 telephonyManagerSpecificEmulatorCheckHooks.add(networkType);
				
		return telephonyManagerSpecificEmulatorCheckHooks;
	}
	
	
	private Set<HookInfo> applicationPackageManagerEmulatorCheckHooks() {
		Set<HookInfo> appPackageMngHooks = new HashSet<HookInfo>();
		
		MethodHookInfo systemFeature = new MethodHookInfo("<android.app.ApplicationPackageManager: boolean hasSystemFeature(java.lang.String)>");
		systemFeature.persistentHookAfter(true);
		
		appPackageMngHooks.add(systemFeature);
		
		return appPackageMngHooks;
	}
	
	
	
	
	// TIMING BOMBS are covered by a bytecode tranformer (TimingBombTransformer)
//	
//	private Set<HookInfo> timingBombHooks() {
//		Set<HookInfo> timingBombHooks = new HashSet<HookInfo>();
//		
//		MethodHookInfo setRepeating = new MethodHookInfo("<android.app.AlarmManager: void set(int,long,android.app.PendingIntent)>");
//		Pair<Integer, Object> paramTime = new Pair<Integer, Object>(1, 2000L);
//		setRepeating.persistentHookBefore(Collections.singleton(paramTime));
//			
//		//yes, this is not a conditional hook, but it belongs to the cat timingbombs
//		MethodHookInfo hPostDelayed = new MethodHookInfo("<android.os.Handler: boolean postDelayed(java.lang.Runnable,long)>");       
//		Set<ParameterConditionValueInfo> parameterInfos1 = new HashSet<ParameterConditionValueInfo>();
//		ParameterConditionValueInfo arg1 = new ParameterConditionValueInfo(1, new Condition() {
//			@Override
//			public boolean isConditionSatisfied(MethodHookParam param) {				
//
//				if(!param.args[0].toString().startsWith("de.tu_darmstadt.sse.additionalappclasses.tracing")) {
//					return true;
//				}
//				return false;
//			}			
//		}, 2000L);
//		parameterInfos1.add(arg1);
//		hPostDelayed.conditionDependentHookBefore(parameterInfos1);
//		
//				
////		timingBombHooks.add(setRepeating);
//		timingBombHooks.add(hPostDelayed);
//		return timingBombHooks;
//	}
	
	
	private Set<HookInfo> networkRelatedHooks() {
		Set<HookInfo> networkHooks = new HashSet<HookInfo>();
		MethodHookInfo getActiveNetworkInfo = new MethodHookInfo("<android.net.ConnectivityManager: android.net.NetworkInfo getActiveNetworkInfo()>");
		try {
			Class<?> networkInfo = Class.forName("android.net.NetworkInfo");
			Class<?>[] networkInfoParams = new Class[4];
			networkInfoParams[0] = int.class;
			networkInfoParams[1] = int.class;
			networkInfoParams[2] = String.class;
			networkInfoParams[3] = String.class;
			Constructor<?> init = networkInfo.getConstructor(networkInfoParams);
			init.setAccessible(true);
			Object obj = init.newInstance(0, 3, "mobile", "UMTS");
			Class<?>[] booleanParam = new Class[1];
			booleanParam[0] = boolean.class;
			Method setIsAvailable = networkInfo.getMethod("setIsAvailable", booleanParam);
			setIsAvailable.setAccessible(true);
			setIsAvailable.invoke(obj, true);
			Method setIsConnectedToProvisioningNetwork = networkInfo.getMethod("setIsConnectedToProvisioningNetwork", booleanParam);
			setIsConnectedToProvisioningNetwork.setAccessible(true);
			setIsConnectedToProvisioningNetwork.invoke(obj, true);
			Method setRoaming = networkInfo.getMethod("setRoaming", booleanParam);
			setRoaming.setAccessible(true);
			setRoaming.invoke(obj, true);
			Class<?>[] setDetailedStateParams = new Class[3];
			setDetailedStateParams[0] = DetailedState.class;
			setDetailedStateParams[1] = String.class;
			setDetailedStateParams[2] = String.class;
			Method setDetailedState = networkInfo.getMethod("setDetailedState", setDetailedStateParams);
			setDetailedState.setAccessible(true);
			setDetailedState.invoke(obj, DetailedState.CONNECTED, "connected", "epc.tmobile.com");
			getActiveNetworkInfo.persistentHookAfter(obj);
		}catch(Exception ex) {
			ex.printStackTrace();
		}
				
		networkHooks.add(getActiveNetworkInfo);
		return networkHooks;
	}		
}
