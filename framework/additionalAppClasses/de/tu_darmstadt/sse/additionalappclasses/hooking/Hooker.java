

package de.tu_darmstadt.sse.additionalappclasses.hooking;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.net.URI;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.Signature;
import android.util.Log;

import com.morgoo.hook.zhook.MethodHook;
import com.morgoo.hook.zhook.MethodHook.MethodHookParam;
import com.morgoo.hook.zhook.ZHook;

import de.tu_darmstadt.sse.additionalappclasses.tracing.BytecodeLogger;
import de.tu_darmstadt.sse.additionalappclasses.util.UtilHook;
import de.tu_darmstadt.sse.sharedclasses.SharedClassesSettings;
import de.tu_darmstadt.sse.sharedclasses.networkconnection.FileFormat;
import de.tu_darmstadt.sse.sharedclasses.networkconnection.NetworkConnectionInitiator;
import de.tu_darmstadt.sse.sharedclasses.networkconnection.serializables.FileFuzzingSerializableObject;
import de.tu_darmstadt.sse.sharedclasses.networkconnection.serializables.SignatureSerializableObject;
import de.tu_darmstadt.sse.sharedclasses.util.Pair;


public class Hooker {
	
	public static Context applicationContext;
	
	public static void initializeHooking(Context context) {
		applicationContext = context;
		Log.i("SSE", "Initialize hooker...");		
		NetworkConnectionInitiator.initNetworkConnection();
		BytecodeLogger.initialize(context);
		Hooker.doHooking(UtilHook.initAllHookers());
		Log.i("SSE", "Hooking ready...");
	}
	
	public static void doHooking(final Set<HookInfo> infos) {
    	for(HookInfo info : infos) {
    		if(info instanceof MethodHookInfo)
    			doMethodHooking((MethodHookInfo)info);
    		else if (info instanceof FieldHookInfo)
    			doFieldHooking((FieldHookInfo)info);
    	}
    		
    	
    }
    	
    private static void doMethodHooking(final MethodHookInfo info) {
    	MethodHook callback = new MethodHook() {
        	
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				super.beforeHookedMethod(param);
						
				if(info.hasHookBefore()) {
					List<AbstractMethodHookBefore> orderedBeforeHooks = info.getBeforeHooks();
					
					for(AbstractMethodHookBefore singleBeforeHook : orderedBeforeHooks) {	
						
						String hookingType = null;
						// adding the runtime values of the params before hook
						if(singleBeforeHook instanceof AnalysisDependentMethodHookBefore) {
							hookingType = "AnalysisDependentMethodHookBefore";
							AnalysisDependentMethodHookBefore analysisDependentHook = (AnalysisDependentMethodHookBefore)singleBeforeHook;					
							analysisDependentHook.retrieveValueFromServer(param.args);									
						}
						else if(singleBeforeHook instanceof ConditionalMethodHookBefore) {
//							Log.i(SharedClassesSettings.TAG, "in ConditionalMethodHookBefore");
							hookingType = "ConditionalMethodHookBefore";
							ConditionalMethodHookBefore conditionalHook = (ConditionalMethodHookBefore)singleBeforeHook;
							conditionalHook.testConditionSatisfaction(param);
						}
						else if(singleBeforeHook instanceof DexFileExtractorHookBefore) {
							Log.i(SharedClassesSettings.TAG, "in DexFileExtractorHookBefore");
							hookingType = "DexFileExtractorHookBefore";
							DexFileExtractorHookBefore dexFileHook = (DexFileExtractorHookBefore)singleBeforeHook;
							int argumentPos = dexFileHook.getArgumentPosition();
							String dexFilePath = (String)param.args[argumentPos];							
							dexFileHook.sendDexFileToServer(dexFilePath);
						}
						
						// first match of hooks quits the hooking
						if(singleBeforeHook.isValueReplacementNecessary()) {
							
							//only for logging purpose
							Log.i(SharedClassesSettings.TAG, String.format("[HOOK] %s || MethodSign: %s || Replace: %s", hookingType, param.method.toString(), singleBeforeHook.getParamValuesToReplace()));

							// change only those params that are required
							for (Pair<Integer, Object> paramValuePair : singleBeforeHook.getParamValuesToReplace()) {
								//special handling for file fuzzing:
								if(paramValuePair.getSecond() instanceof FileFuzzingSerializableObject)
									doFileFuzzingIfNecessary(param, paramValuePair.getFirst(), (FileFuzzingSerializableObject)paramValuePair.getSecond());
								else
									param.args[paramValuePair.getFirst()] = paramValuePair.getSecond();
		                	}	
							return;
						}											
					}
                }
            }
	        	
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);                                
                
            	if(info.hasHookAfter()) {
            		List<AbstractMethodHookAfter> orderedAfterHooks = info.getAfterHooks();
            		
            		for(AbstractMethodHookAfter singleAfterHook : orderedAfterHooks) {  
            			String hookingType = null;
	            		// adding the runtime value of the return value
	            		if(singleAfterHook instanceof AnalysisDependentMethodHookAfter) {
	            			hookingType = "AnalysisDependentMethodHookAfter";
	            			AnalysisDependentMethodHookAfter analysisDependenHook = (AnalysisDependentMethodHookAfter)singleAfterHook;
	            			analysisDependenHook.retrieveValueFromServer(param.getResult());            			
	            		}
	            		else if(singleAfterHook instanceof ConditionalMethodHookAfter) {
	            			hookingType = "ConditionalMethodHookAfter";
	            			ConditionalMethodHookAfter conditionalHook = (ConditionalMethodHookAfter)singleAfterHook;
	            			conditionalHook.testConditionSatisfaction(param);          				
	            		}
	            		else if(singleAfterHook instanceof SimpleBooleanHookAfter) {
	            			hookingType = "SimpleBooleanHookAfter";
	            			SimpleBooleanHookAfter boolHookAfer = (SimpleBooleanHookAfter)singleAfterHook;
	            			boolHookAfer.retrieveBooleanValueFromServer();
	            		}
	            		
	            		// first match of hooks quits the hooking
	            		if(singleAfterHook.isValueReplacementNecessary()) {
							//this is a hardcoded check due to serialization problems with the non-serializable PackageInfo object
							Object returnValue = singleAfterHook.getReturnValue();
							if(returnValue instanceof SignatureSerializableObject) {
								Log.i(SharedClassesSettings.TAG, "TEST: SignatureSerializableObject");
								SignatureSerializableObject sso = (SignatureSerializableObject)returnValue;
								if(param.getResult() instanceof PackageInfo) {
									Log.i(SharedClassesSettings.TAG, "TEST: PackageInfo");
									PackageInfo pm = (PackageInfo)param.getResult();
									pm.signatures[0] = new Signature(sso.getEncodedCertificate());
									
									//only for logging purpose
									Log.i(SharedClassesSettings.TAG, String.format("[HOOK] %s || MethodSign: %s || Replace: %s", hookingType, param.method.toString(), pm.signatures[0]));
								}
							}
							else {
								param.setResult(returnValue);
								//only for logging purpose
								Log.i(SharedClassesSettings.TAG, String.format("[HOOK] %s || MethodSign: %s || Replace: %s", hookingType, param.method.toString(), singleAfterHook.getReturnValue()));
							}
	            			return;
	            		}
            		}
            	}
        	}    	            
	        };
                
        Member methodOrConstructor = null;		
		try {
			Class<?> cls = Class.forName(info.getClassName());
			Class<?>[] tmp = info.getParams();
			if(info.getMethodName().equals("<init>")){
				methodOrConstructor = cls.getDeclaredConstructor(tmp);
			}
			else {
				if(tmp == null)				
					methodOrConstructor = cls.getDeclaredMethod(info.getMethodName());
				else
					methodOrConstructor = cls.getDeclaredMethod(info.getMethodName(), tmp);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
        ZHook.hookMethod(methodOrConstructor, callback);  
    }
    
    
    private static void doFieldHooking(final FieldHookInfo info) {
    	AbstractFieldHookAfter afterHook = info.getAfterHook();    	
    	
    	if(afterHook.isValueReplacementNecessary()) {
			try {				
				Class<?> tmp  = Class.forName(info.getClassName());	
				Field field = tmp.getField(info.getFieldName());
				field.setAccessible(true);
				Object oldValue = retrieveOldFieldValue(info.getClassName(), info.getFieldName());
		        // Sets the field to the new value				
		        field.set(oldValue, afterHook.getNewValue());
			} catch (Exception e) {
				e.printStackTrace();
			}
    	}
    }
    
    private static Object retrieveOldFieldValue(String className, String fieldName) {
    	try {
	    	Class<?> tmp  = Class.forName(className);	
			Field field = tmp.getField(fieldName);
			field.setAccessible(true);
			return field.get(Class.forName(className));
    	}catch(Exception ex) {
    		ex.printStackTrace();
    		return null;
    	}
    }
    
    private static void doFileFuzzingIfNecessary(MethodHookParam param, int index, FileFuzzingSerializableObject fuzzyingObject) {
    	//file is in private dir and only the file name is necessary
    	if(fuzzyingObject.getStorageMode() == 0)
    		doPrivateDirFileFuzzing(param, index, fuzzyingObject);
    	//there is a file object available
    	else if(fuzzyingObject.getStorageMode() == 1)
    		doFileFuzzingBasedOnFileObject(param, fuzzyingObject);
    	//absolute path of file is given
    	else if(fuzzyingObject.getStorageMode() == 2)
    		doFileFuzzingBasedOnAbsoluteStringPath(param, index, fuzzyingObject);
    }

    
    private static void doFileFuzzingBasedOnAbsoluteStringPath(MethodHookParam param, int index, FileFuzzingSerializableObject fuzzyingObject) {
    	//get file
    	File file = new File((String)param.args[index]);
    	
    	//second copy corresponding file to file path
    	copyCorrectFile(file, fuzzyingObject.getFileFormat());
	}

	
    private static void doFileFuzzingBasedOnFileObject(MethodHookParam param, FileFuzzingSerializableObject fuzzyingObject) {
		File file = null;
    	//first get correct file
    	if(param.args.length == 2) {
    		//File(File parent, String child)
    		if(param.args[0] instanceof File && param.args[1] instanceof String) 
    			file = new File((File)param.args[0], (String)param.args[1]);
    		//File(String parent, String child)
    		else if(param.args[0] instanceof String && param.args[1] instanceof String)
    			file = new File((String)param.args[0], (String)param.args[1]);
    		else
    			return;
    			
    	}
    	else if(param.args.length == 1) {
    		//File(String pathname)
    		if(param.args[0] instanceof String)
    			file = new File((String)param.args[0]);
    		//File(URI uri)
    		else if(param.args[0] instanceof URI)
    			file = new File((URI)param.args[0]);
    		else 
    			return;
    	}
    	
    	//there is nothing to do
    	if(file == null || file.exists())
    		return;
    	
    	//second copy corresponding file to file path
    	copyCorrectFile(file, fuzzyingObject.getFileFormat());
	}

	
	private static void doPrivateDirFileFuzzing(MethodHookParam param, int index, FileFuzzingSerializableObject fuzzyingObject) {
		String fileName = (String)param.args[index];
		Context appContext = Hooker.applicationContext;
		File localFile = appContext.getFileStreamPath(fileName);
		//only create a dummy file if there is no file
		if(!localFile.exists()) {
			//what file format do we need?
			copyCorrectFile(localFile, fuzzyingObject.getFileFormat());			
		}
	}
	
	
	private static void copyCorrectFile(File localFile, FileFormat fileFormat) {
		String sdCardFilePath = null;
		
		if(fileFormat == FileFormat.DIRECTORY) {
			if(!localFile.exists()) {
				localFile.mkdir();
			}
		}
		else {
			if(fileFormat == FileFormat.PROPERTIES)
				sdCardFilePath =  SharedClassesSettings.FUZZY_FILES_DIR_PATH + "/properties.properties";
			else if(fileFormat == FileFormat.UNKNOWN)
				sdCardFilePath =  SharedClassesSettings.FUZZY_FILES_DIR_PATH + "/text.text";
			else if(fileFormat == FileFormat.DEX)
				sdCardFilePath =  SharedClassesSettings.FUZZY_FILES_DIR_PATH + "/dex.dex";
			else if(fileFormat == FileFormat.DATABASE)
				sdCardFilePath =  SharedClassesSettings.FUZZY_FILES_DIR_PATH + "/db.db";
			
			File sdCardFile = new File(sdCardFilePath);
			try {
				InputStream in = new FileInputStream(sdCardFile);
			    OutputStream out = new FileOutputStream(localFile);
	
			    // Transfer bytes from in to out
			    byte[] buf = new byte[1024];
			    int len;
			    while ((len = in.read(buf)) > 0) {
			        out.write(buf, 0, len);
			    }
			    in.close();
			    out.close();
			} catch (IOException e) {					
				e.printStackTrace();
			}
		}
	}
}
