package de.tu_darmstadt.sse.additionalappclasses;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import de.tu_darmstadt.sse.additionalappclasses.util.UtilAddContact;


public class ComponentCallerService extends Service{

	private static Map<Class<?>, Object> globalInstances = new HashMap<>();
	
	
	public static void registerGlobalInstance(Class<?> clazz, Object instance) {
		globalInstances.put(clazz, instance);
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	
	@Override
	public void onStart(Intent intent, int startId) {		
		String clazz = null;
		String task = null;
		String action = null;	
		String mimeType = null;
		if(intent.hasExtra("className"))
			clazz = intent.getStringExtra("className");
		if(intent.hasExtra("task"))
			task = intent.getStringExtra("task");
		if(intent.hasExtra("action"))
			action = intent.getStringExtra("action");
		if(intent.hasExtra("mimeType"))
			mimeType = intent.getStringExtra("mimeType");								
		
		try {
			Class<?> className = null;
			Object thisObject = null;
			if(clazz != null) {
				//inner class
				if(clazz.contains("$")) {
					className = Class.forName(clazz);
					Constructor<?>[] ctors = className.getDeclaredConstructors();				
					Constructor<?> ctor = getConstructorWithLeastParams(ctors);
					ctor.setAccessible(true);
					
					Class<?>[] firstCtorParms = ctor.getParameterTypes();
					List<Object> paramObjects = new ArrayList<Object>();
					for(Class<?> param : firstCtorParms)
						paramObjects.add(getDefaultObject(param));
					thisObject = ctor.newInstance(paramObjects.toArray());												
				}
				else {
					className = Class.forName(clazz);
					thisObject = className.newInstance();			
				}
			}
			
			
			if(task.equals("broadcast")) {
				Intent intent2Sent = new Intent();
				if(action != null)
					intent2Sent.setAction(action);
				if(mimeType != null)
					intent2Sent.setType(mimeType);
				Method onReceive = className.getMethod("onReceive", Context.class, Intent.class);				
				onReceive.invoke(thisObject, getApplicationContext(), intent2Sent);				
			}			
			
			else if(task.equals("onClick")) {
				Method onClick = className.getMethod("onClick", View.class);
				View view = new View(getApplicationContext());
				onClick.invoke(thisObject, view);
			}
			
			else if(task.equals("addContact")) {
				UtilAddContact.writePhoneContact("Fraunhofer", "9999999999");
			}
		} catch (Exception e) {
			Log.i("SSE", "ComponentCallerService exception");
			e.printStackTrace();
		}
	}
	
	private Object getDefaultObject(Class<?> clazz) {
		Log.i("SSE", clazz.getName());
		if(clazz.getName().equals("java.lang.Integer"))
			return 0;
		else if(clazz.getName().equals("java.lang.Long"))
			return 0L;
		else if(clazz.getName().equals("java.lang.Double"))
			return 0.0d;
		else if(clazz.getName().equals("java.lang.Float"))
			return 0.0f;
		else if(clazz.getName().equals("java.lang.Boolean"))
			return false;
		else if(clazz.getName().equals("java.lang.Character"))
			return '\u0000';
		else if(clazz.getName().equals("java.lang.Byte"))
			return 0;
		else if(clazz.getName().equals("java.lang.Short"))
			return 0;
		else
			return globalInstances.get(clazz);
	}
	
	private Constructor<?> getConstructorWithLeastParams(Constructor<?>[] ctors) {
		int countArgs = -1;
		Constructor<?> leastCtor = null;
		for(Constructor<?> ctor : ctors) {
			int ctorLength = ctor.getParameterTypes().length;
			if(countArgs == -1 || countArgs > ctorLength) {
				countArgs = ctorLength;
				leastCtor = ctor;
			}			
		}		
		return leastCtor;
	}

}
