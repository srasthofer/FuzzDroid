package de.tu_darmstadt.sse.additionalappclasses.hookdefinitions;

import java.util.HashSet;
import java.util.Set;

import android.telephony.PhoneNumberUtils;
import android.util.Log;

import com.morgoo.hook.zhook.MethodHook.MethodHookParam;

import dalvik.system.DexClassLoader;
import de.tu_darmstadt.sse.additionalappclasses.hooking.Condition;
import de.tu_darmstadt.sse.additionalappclasses.hooking.HookInfo;
import de.tu_darmstadt.sse.additionalappclasses.hooking.MethodHookInfo;
import de.tu_darmstadt.sse.additionalappclasses.hooking.ParameterConditionValueInfo;

public class ConditionalHookDefinitions implements Hook{

	@Override
	public Set<HookInfo> initializeHooks() {
		Set<HookInfo> allConditionalHooks = new HashSet<HookInfo>();
//		allConditionalHooks.addAll(fileSpecificEmulatorChecks());		
//		allConditionalHooks.addAll(systemPropEmulatorChecks());		
//		allConditionalHooks.addAll(appSpecificEmulatorChecks());
		allConditionalHooks.addAll(textMessageCrashPrevention());	
//		allConditionalHooks.addAll(reflectionHooks());
		return allConditionalHooks;
	}		
	
	
	private Set<HookInfo> textMessageCrashPrevention() {
		Set<HookInfo> textMessageHooks = new HashSet<HookInfo>();

		MethodHookInfo smsManagerSendTextMessage = new MethodHookInfo("<android.telephony.SmsManager: void sendTextMessage(java.lang.String, java.lang.String, java.lang.String, android.app.PendingIntent, android.app.PendingIntent)>");       
		Set<ParameterConditionValueInfo> parameterInfos1 = new HashSet<ParameterConditionValueInfo>();
		ParameterConditionValueInfo arg1 = new ParameterConditionValueInfo(0, new Condition() {
			@Override
			public boolean isConditionSatisfied(MethodHookParam param) {
				if(!PhoneNumberUtils.isGlobalPhoneNumber((String)param.args[0]))
					return true;
				return false;
			}			
		}, "555555");
		parameterInfos1.add(arg1);
		smsManagerSendTextMessage.conditionDependentHookBefore(parameterInfos1);
		textMessageHooks.add(smsManagerSendTextMessage);
		
		MethodHookInfo smsManagerSendMultipartTextMessage = new MethodHookInfo("<android.telephony.SmsManager: void sendMultipartTextMessage(java.lang.String, java.lang.String, java.util.ArrayList, java.util.ArrayList, java.util.ArrayList)>");		
		Set<ParameterConditionValueInfo> parameterInfos2 = new HashSet<ParameterConditionValueInfo>();
		ParameterConditionValueInfo arg2 = new ParameterConditionValueInfo(0, new Condition() {			
			@Override
			public boolean isConditionSatisfied(MethodHookParam param) {
				if(!PhoneNumberUtils.isGlobalPhoneNumber((String)param.args[0]))
					return true;
				return false;
			}			
		}, "555555");
		parameterInfos2.add(arg2);
		smsManagerSendMultipartTextMessage.conditionDependentHookBefore(parameterInfos2);		
		textMessageHooks.add(smsManagerSendMultipartTextMessage);
		
		MethodHookInfo gsmSendTextMessage = new MethodHookInfo("<android.telephony.gsm.SmsManager: void sendTextMessage(java.lang.String, java.lang.String, java.lang.String, android.app.PendingIntent, android.app.PendingIntent)>");		
		Set<ParameterConditionValueInfo> parameterInfos3 = new HashSet<ParameterConditionValueInfo>();
		ParameterConditionValueInfo arg3 = new ParameterConditionValueInfo(0, new Condition() {			
			@Override
			public boolean isConditionSatisfied(MethodHookParam param) {
				if(!PhoneNumberUtils.isGlobalPhoneNumber((String)param.args[0]))
					return true;
				return false;
			}			
		}, "555555");
		parameterInfos3.add(arg3);
		gsmSendTextMessage.conditionDependentHookBefore(parameterInfos3);		
		textMessageHooks.add(gsmSendTextMessage);
		
		MethodHookInfo gsmSendMultiTextMessage = new MethodHookInfo("<android.telephony.gsm.SmsManager: void sendMultipartTextMessage(java.lang.String, java.lang.String, java.util.ArrayList, java.util.ArrayList, java.util.ArrayList)>");		
		Set<ParameterConditionValueInfo> parameterInfos4 = new HashSet<ParameterConditionValueInfo>();
		ParameterConditionValueInfo arg4 = new ParameterConditionValueInfo(0, new Condition() {			
			@Override
			public boolean isConditionSatisfied(MethodHookParam param) {
				if(!PhoneNumberUtils.isGlobalPhoneNumber((String)param.args[0]))
					return true;
				return false;
			}			
		}, "555555");
		parameterInfos4.add(arg4);
		gsmSendMultiTextMessage.conditionDependentHookBefore(parameterInfos4);		
		textMessageHooks.add(gsmSendMultiTextMessage);
		        
        return textMessageHooks;
	}
	

	
	private Set<HookInfo> fileSpecificEmulatorChecks() {
		Set<HookInfo> fileHooks = new HashSet<HookInfo>();
		
        MethodHookInfo exists = new MethodHookInfo("<java.io.File: boolean exists()>");
        //qemu_pipe check
        exists.conditionDependentHookAfter(new Condition() {
        	@Override
        	public boolean isConditionSatisfied(MethodHookParam param) {
				if(param.thisObject.toString().equals("/dev/qemu_pipe"))
					return true;
				return false;
        	}        	
        }, false);
        //qemud check
        exists.conditionDependentHookAfter(new Condition() {
			@Override
			public boolean isConditionSatisfied(MethodHookParam param) {
				if(param.thisObject.toString().equals("/dev/socket/qemud"))
					return true;
				return false;
			}        	
        }, false);
        //goldfish check
        exists.conditionDependentHookAfter(new Condition() {
        	@Override
        	public boolean isConditionSatisfied(MethodHookParam param) {
        		if(param.thisObject.toString().equals("/init.goldfish.rc"))
        			return true;
        		return false;
        	}        	
        }, false);
        //qemu_trace check
        exists.conditionDependentHookAfter(new Condition() {
        	@Override
        	public boolean isConditionSatisfied(MethodHookParam param) {
        		if(param.thisObject.toString().equals("/sys/qemu_trace"))
        			return true;
        		return false;
        	}        	
        }, false);
                
                
        fileHooks.add(exists);        
        return fileHooks;
	}
	
	
	private Set<HookInfo> systemPropEmulatorChecks() {
		Set<HookInfo> systemPropHooks = new HashSet<HookInfo>();
		
		MethodHookInfo systemPropGet = new MethodHookInfo("<android.os.SystemProperties: java.lang.String get(java.lang.String)>");
        //gsm.sim.operator.alpha check
        systemPropGet.conditionDependentHookAfter(new Condition() {
        	@Override
        	public boolean isConditionSatisfied(MethodHookParam param) {
				if(param.args[0].toString().equals("gsm.sim.operator.alpha"))
					return true;
				return false;
        	}        	
        }, "T-mobile D");
        //gsm.operator.numeric check
        systemPropGet.conditionDependentHookAfter(new Condition() {
        	@Override
        	public boolean isConditionSatisfied(MethodHookParam param) {
        		if(param.args[0].toString().equals("gsm.operator.numeric"))
        			return true;
        		return false;
        	}        	
        }, "26201");
        //gsm.sim.operator.numeric check
        systemPropGet.conditionDependentHookAfter(new Condition() {
        	@Override
        	public boolean isConditionSatisfied(MethodHookParam param) {
        		if(param.args[0].toString().equals("gsm.sim.operator.numeric"))
        			return true;
        		return false;
        	}        	
        }, "8923440000000000003");
        //gsm.version.ril-impl check
        systemPropGet.conditionDependentHookAfter(new Condition() {
        	@Override
        	public boolean isConditionSatisfied(MethodHookParam param) {
        		if(param.args[0].toString().equals("gsm.version.ril-impl"))
        			return true;
        		return false;
        	}        	
        }, "");
        //ro.baseband check
        systemPropGet.conditionDependentHookAfter(new Condition() {
        	@Override
        	public boolean isConditionSatisfied(MethodHookParam param) {
        		if(param.args[0].toString().equals("ro.baseband"))
        			return true;
        		return false;
        	}        	
        }, "");
        //ro.bootloader check
        systemPropGet.conditionDependentHookAfter(new Condition() {
        	@Override
        	public boolean isConditionSatisfied(MethodHookParam param) {
        		if(param.args[0].toString().equals("ro.bootloader"))
        			return true;
        		return false;
        	}        	
        }, "PRIMEMD04");
        //ro.build.description check
        systemPropGet.conditionDependentHookAfter(new Condition() {
        	@Override
        	public boolean isConditionSatisfied(MethodHookParam param) {
        		if(param.args[0].toString().equals("ro.build.description"))
        			return true;
        		return false;
        	}        	
        }, "");
        //ro.build.display.id check
        systemPropGet.conditionDependentHookAfter(new Condition() {
        	@Override
        	public boolean isConditionSatisfied(MethodHookParam param) {
        		if(param.args[0].toString().equals("ro.build.display.id"))
        			return true;
        		return false;
        	}        	
        }, "JWR66Y");
        //ro.build.fingerprint check
        systemPropGet.conditionDependentHookAfter(new Condition() {
        	@Override
        	public boolean isConditionSatisfied(MethodHookParam param) {
        		if(param.args[0].toString().equals("ro.build.fingerprint"))
        			return true;
        		return false;
        	}        	
        }, "google/takju/maguro:4.3/JWR66Y/776638:user/release-keys");
        //ro.build.tags check
        systemPropGet.conditionDependentHookAfter(new Condition() {
        	@Override
        	public boolean isConditionSatisfied(MethodHookParam param) {
        		if(param.args[0].toString().equals("ro.build.tags"))
        			return true;
        		return false;
        	}        	
        }, "release-keys");
        //ro.build.user check
        systemPropGet.conditionDependentHookAfter(new Condition() {
        	@Override
        	public boolean isConditionSatisfied(MethodHookParam param) {
        		if(param.args[0].toString().equals("ro.build.user"))
        			return true;
        		return false;
        	}        	
        }, "android-build");
        //ro.hardware check
        systemPropGet.conditionDependentHookAfter(new Condition() {
        	@Override
        	public boolean isConditionSatisfied(MethodHookParam param) {
        		if(param.args[0].toString().equals("ro.hardware"))
        			return true;
        		return false;
        	}        	
        }, "tuna");
        //ro.product.board check
        systemPropGet.conditionDependentHookAfter(new Condition() {
        	@Override
        	public boolean isConditionSatisfied(MethodHookParam param) {
        		if(param.args[0].toString().equals("ro.product.board"))
        			return true;
        		return false;
        	}        	
        }, "tuna");
        //ro.product.brand check
        systemPropGet.conditionDependentHookAfter(new Condition() {
        	@Override
        	public boolean isConditionSatisfied(MethodHookParam param) {
        		if(param.args[0].toString().equals("ro.product.brand"))
        			return true;
        		return false;
        	}        	
        }, "google");
        //ro.product.device check
        systemPropGet.conditionDependentHookAfter(new Condition() {
        	@Override
        	public boolean isConditionSatisfied(MethodHookParam param) {
        		if(param.args[0].toString().equals("ro.product.device"))
        			return true;
        		return false;
        	}        	
        }, "maguro");
        //ro.product.manufacturer check
        systemPropGet.conditionDependentHookAfter(new Condition() {
        	@Override
        	public boolean isConditionSatisfied(MethodHookParam param) {
        		if(param.args[0].toString().equals("ro.product.manufacturer"))
        			return true;
        		return false;
        	}        	
        }, "samsung");
        //ro.product.name check
        systemPropGet.conditionDependentHookAfter(new Condition() {
        	@Override
        	public boolean isConditionSatisfied(MethodHookParam param) {
        		if(param.args[0].toString().equals("ro.product.name"))
        			return true;
        		return false;
        	}        	
        }, "takju");
        //ro.serialno check
        systemPropGet.conditionDependentHookAfter(new Condition() {
        	@Override
        	public boolean isConditionSatisfied(MethodHookParam param) {
        		if(param.args[0].toString().equals("ro.serialno"))
        			return true;
        		return false;
        	}        	
        }, "0149E08209007013");
        //ro.setupwizard.mode check
        systemPropGet.conditionDependentHookAfter(new Condition() {
        	@Override
        	public boolean isConditionSatisfied(MethodHookParam param) {
        		if(param.args[0].toString().equals("ro.setupwizard.mode"))
        			return true;
        		return false;
        	}        	
        }, "");
        //ro.build.type check
        systemPropGet.conditionDependentHookAfter(new Condition() {
        	@Override
        	public boolean isConditionSatisfied(MethodHookParam param) {
        		if(param.args[0].toString().equals("ro.build.type"))
        			return true;
        		return false;
        	}        	
        }, "user");
        //ARGH check
        systemPropGet.conditionDependentHookAfter(new Condition() {
        	@Override
        	public boolean isConditionSatisfied(MethodHookParam param) {
        		if(param.args[0].toString().equals("ARGH"))
        			return true;
        		return false;
        	}        	
        }, "");
        //init.svc.goldfish-logcat check
        systemPropGet.conditionDependentHookAfter(new Condition() {
        	@Override
        	public boolean isConditionSatisfied(MethodHookParam param) {
        		if(param.args[0].toString().equals("init.svc.goldfish-logcat"))
        			return true;
        		return false;
        	}        	
        }, "");
        //init.svc.goldfish-setup check
        systemPropGet.conditionDependentHookAfter(new Condition() {
        	@Override
        	public boolean isConditionSatisfied(MethodHookParam param) {
        		if(param.args[0].toString().equals("init.svc.goldfish-setup"))
        			return true;
        		return false;
        	}        	
        }, "");
        //init.svc.qemud check
        systemPropGet.conditionDependentHookAfter(new Condition() {
        	@Override
        	public boolean isConditionSatisfied(MethodHookParam param) {
        		if(param.args[0].toString().equals("init.svc.qemud"))
        			return true;
        		return false;
        	}        	
        }, "");
        //qemu.hw.mainkeys check
        systemPropGet.conditionDependentHookAfter(new Condition() {
        	@Override
        	public boolean isConditionSatisfied(MethodHookParam param) {
        		if(param.args[0].toString().equals("qemu.hw.mainkeys"))
        			return true;
        		return false;
        	}        	
        }, "");
        //init.svc.qemu-props check
        systemPropGet.conditionDependentHookAfter(new Condition() {
        	@Override
        	public boolean isConditionSatisfied(MethodHookParam param) {
        		if(param.args[0].toString().equals("init.svc.qemu-props"))
        			return true;
        		return false;
        	}        	
        }, "");
        //qemu.sf.fake_camera check
        systemPropGet.conditionDependentHookAfter(new Condition() {
        	@Override
        	public boolean isConditionSatisfied(MethodHookParam param) {
        		if(param.args[0].toString().equals("qemu.sf.fake_camera"))
        			return true;
        		return false;
        	}        	
        }, "");
        //qemu.sf.lcd_density check
        systemPropGet.conditionDependentHookAfter(new Condition() {
        	@Override
        	public boolean isConditionSatisfied(MethodHookParam param) {
        		if(param.args[0].toString().equals("qemu.sf.lcd_density"))
        			return true;
        		return false;
        	}        	
        }, "");
        //ro.kernel.android.checkjni check
        systemPropGet.conditionDependentHookAfter(new Condition() {
        	@Override
        	public boolean isConditionSatisfied(MethodHookParam param) {
        		if(param.args[0].toString().equals("ro.kernel.android.checkjni"))
        			return true;
        		return false;
        	}        	
        }, "");
        //ro.kernel.android.qemud check
        systemPropGet.conditionDependentHookAfter(new Condition() {
        	@Override
        	public boolean isConditionSatisfied(MethodHookParam param) {
        		if(param.args[0].toString().equals("ro.kernel.android.qemud"))
        			return true;
        		return false;
        	}        	
        }, "");
        //ro.kernel.console check
        systemPropGet.conditionDependentHookAfter(new Condition() {
        	@Override
        	public boolean isConditionSatisfied(MethodHookParam param) {
        		if(param.args[0].toString().equals("ro.kernel.console"))
        			return true;
        		return false;
        	}        	
        }, "");
        //ro.kernel.ndns check
        systemPropGet.conditionDependentHookAfter(new Condition() {
        	@Override
        	public boolean isConditionSatisfied(MethodHookParam param) {
        		if(param.args[0].toString().equals("ro.kernel.ndns"))
        			return true;
        		return false;
        	}        	
        }, "");
        //ro.kernel.qemu.gles check
        systemPropGet.conditionDependentHookAfter(new Condition() {
        	@Override
        	public boolean isConditionSatisfied(MethodHookParam param) {
        		if(param.args[0].toString().equals("ro.kernel.qemu.gles"))
        			return true;
        		return false;
        	}        	
        }, "");
        //ro.kernel.qemu check
        systemPropGet.conditionDependentHookAfter(new Condition() {
        	@Override
        	public boolean isConditionSatisfied(MethodHookParam param) {
        		if(param.args[0].toString().equals("ro.kernel.qemu"))
        			return true;
        		return false;
        	}        	
        }, "");
        
        systemPropHooks.add(systemPropGet);
		
		return systemPropHooks;
	}

	
	private Set<HookInfo> appSpecificEmulatorChecks() {
		Set<HookInfo> appSpecificHooks = new HashSet<HookInfo>();
		
		MethodHookInfo addCategory = new MethodHookInfo("<android.content.Intent: android.content.Intent addCategory(java.lang.String)>");
        
		Set<ParameterConditionValueInfo> parameterInfos = new HashSet<ParameterConditionValueInfo>();
		ParameterConditionValueInfo arg1 = new ParameterConditionValueInfo(0, new Condition() {

			@Override
			public boolean isConditionSatisfied(MethodHookParam param) {
				if(param.args[0].toString().equals("android.intent.category.APP_MARKET"))
					return true;
				return false;
			}
			
		}, "android.intent.category.LAUNCHER");
		parameterInfos.add(arg1);
		addCategory.conditionDependentHookBefore(parameterInfos);
		
		appSpecificHooks.add(addCategory);
		
		return appSpecificHooks;
	}
	
	private Set<HookInfo> reflectionHooks() {
		Set<HookInfo> reflectionHooks = new HashSet<HookInfo>();
		
		 MethodHookInfo dexClassLoaderLoadClass = new MethodHookInfo("<dalvik.system.DexClassLoader: java.lang.Class loadClass(java.lang.String)>");		 
		 Set<ParameterConditionValueInfo> parameterInfos = new HashSet<ParameterConditionValueInfo>();
		 ParameterConditionValueInfo arg0 = new ParameterConditionValueInfo(0, new Condition() {
				@Override
				public boolean isConditionSatisfied(MethodHookParam param) {
					//we check if there is a class available
					try{
						Log.i("SSE1", "in loadClass");
						DexClassLoader dcl = (DexClassLoader)param.thisObject;
						dcl.loadClass((String)param.args[0]);
					}catch(Exception ex) {
						return true;
					}
					return false;
				}				
		 }, "de.tu_darmstadt.sse.additionalappclasses.reflections.DummyReflectionClass");
		 parameterInfos.add(arg0);
		 dexClassLoaderLoadClass.conditionDependentHookBefore(parameterInfos);		 
		 reflectionHooks.add(dexClassLoaderLoadClass);
		 
		 MethodHookInfo classGetMethod = new MethodHookInfo("<java.lang.Class: java.lang.reflect.Method getMethod(java.lang.String,java.lang.Class[])>");
		 Set<ParameterConditionValueInfo> parameterInfosClassGetMethod = new HashSet<ParameterConditionValueInfo>();
		 ParameterConditionValueInfo classGetMethodArg0 = new ParameterConditionValueInfo(0, new Condition() {
				@Override
				public boolean isConditionSatisfied(MethodHookParam param) {
					//we check if there is a class available
					try{
						Log.i("SSE1", "in getMethod");
						Class<?> clazz = (Class<?>)param.thisObject;
						clazz.getMethod((String)param.args[0], (Class[])param.args[1]);
					}catch(Exception ex) {
						return true;
					}
					return false;
				}				
		 }, "dummyReflectionMethod");
		 
		 ParameterConditionValueInfo classGetMethodArg1 = new ParameterConditionValueInfo(1, new Condition() {
			 @Override
			 public boolean isConditionSatisfied(MethodHookParam param) {
				 //we check if there is a class available
				 try{
					 Class<?> clazz = (Class<?>)param.thisObject;
					 clazz.getMethod((String)param.args[0], (Class[])param.args[1]);
				 }catch(Exception ex) {
					 return true;
				 }
				 return false;
			 }				
		 }, null);
		 
		 parameterInfosClassGetMethod.add(classGetMethodArg0);
		 parameterInfosClassGetMethod.add(classGetMethodArg1);
		 classGetMethod.conditionDependentHookBefore(parameterInfosClassGetMethod);
		 reflectionHooks.add(classGetMethod);
		
		return reflectionHooks;
	}
}
