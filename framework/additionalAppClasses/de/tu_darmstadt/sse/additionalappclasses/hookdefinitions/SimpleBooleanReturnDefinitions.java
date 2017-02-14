package de.tu_darmstadt.sse.additionalappclasses.hookdefinitions;

import java.util.HashSet;
import java.util.Set;

import de.tu_darmstadt.sse.additionalappclasses.hooking.HookInfo;
import de.tu_darmstadt.sse.additionalappclasses.hooking.MethodHookInfo;

public class SimpleBooleanReturnDefinitions implements Hook{

	@Override
	public Set<HookInfo> initializeHooks() {
		Set<HookInfo> booleanHooks = new HashSet<HookInfo>();

		MethodHookInfo getBooleanSP = new MethodHookInfo("<android.app.SharedPreferencesImpl: boolean getBoolean(java.lang.String, boolean)>");
		getBooleanSP.simpleBooleanHookAfter();
        booleanHooks.add(getBooleanSP);
        
        return booleanHooks;
	}

}
