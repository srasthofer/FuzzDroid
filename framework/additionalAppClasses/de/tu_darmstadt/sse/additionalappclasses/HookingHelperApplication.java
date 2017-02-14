package de.tu_darmstadt.sse.additionalappclasses;

import android.app.Application;
import android.content.Context;
import de.tu_darmstadt.sse.additionalappclasses.hooking.Hooker;

public class HookingHelperApplication extends Application{	
			
	protected void attachBaseContext(Context context) {
		super.attachBaseContext(context);
		Hooker.initializeHooking(context);
	}

}
