package de.tu_darmstadt.sse.additionalappclasses.wrapper;

import java.lang.reflect.Method;
import java.util.Properties;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;
import de.tu_darmstadt.sse.sharedclasses.SharedClassesSettings;

public class DummyWrapper {
	
	public static PackageInfo dummyWrapper_getPackageInfo(PackageManager manager, String packageName, int flags) {
		Log.i(SharedClassesSettings.TAG, "Dummy getPackage called");
		try {
			return manager.getPackageInfo(packageName, flags);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	public static String dummyWrapper_getProperty(Properties props, String key, String defaultValue) {
		return props.getProperty(key, defaultValue);
	}
	
	
	public static String dummyWrapper_getProperty(Properties props, String key) {
		return props.getProperty(key);
	}
	
	
	public static Class<?> dummyWrapper_loadClass(String className, ClassLoader classLoader) {
		Log.i(SharedClassesSettings.TAG, "Dummy loadClass() called for " + className);
		Class<?> clazz = null;
		try {
			clazz = classLoader.loadClass(className);
			//in case it does not exist, we use our dummy class
		} catch (ClassNotFoundException e) {
			try {
				clazz = DummyWrapper.class.getClassLoader().loadClass(className);
			} catch (ClassNotFoundException e2) {
				try{
					clazz = classLoader.loadClass("de.tu_darmstadt.sse.additionalappclasses.reflections.DummyReflectionClass");
					Log.i(SharedClassesSettings.TAG, "Dummy class returned for " + className);
				}catch (ClassNotFoundException ex) {
					ex.printStackTrace();
				}
			}
		}
		return clazz;
	}
	
	public static Method dummyWrapper_getMethod(Class clazz, String methodName, Class[] parameterTypes) {
		Log.i(SharedClassesSettings.TAG, String.format("Dummy getMethod() called for %s.%s",
				clazz.getName(), methodName));
		
		// For some methods, we need to inject our own implementations
		if (clazz.getName().equals("dalvik.system.DexFile") && methodName.equals("loadClass")) {
			try {
				Method m = DummyWrapper.class.getMethod("dummyWrapper_loadClass",
						String.class, ClassLoader.class);
				Log.i(SharedClassesSettings.TAG, "Dummy getMethod() obtained: " + m);
				return m;
			} catch (NoSuchMethodException | SecurityException e) {
				Log.i(SharedClassesSettings.TAG, "Could not get dummy implementation for loadClass(), falling "
						+ "back to original one");
			}
		}
		
		Method method = null;
		try{
			method = clazz.getMethod(methodName, parameterTypes);
		}catch(Exception ex) {
			Log.i(SharedClassesSettings.TAG, "Could not find method, falling back to dummy");
			try{
				Class dummyClass = Class.forName("de.tu_darmstadt.sse.additionalappclasses.reflections.DummyReflectionClass");
				method = dummyClass.getMethod("dummyReflectionMethod", null);
			}catch(Exception ex2) {
				ex2.printStackTrace();
			}
		}
		return method;
	}
}
