package de.tu_darmstadt.sse.additionalappclasses.hookdefinitions;

import java.util.HashSet;
import java.util.Set;

import de.tu_darmstadt.sse.additionalappclasses.hooking.HookInfo;
import de.tu_darmstadt.sse.additionalappclasses.hooking.MethodHookInfo;

public class DexFileExtractorHookDefinitions implements Hook{

	@Override
	public Set<HookInfo> initializeHooks() {
		Set<HookInfo> dexFileHooks = new HashSet<HookInfo>();

		MethodHookInfo loadDex = new MethodHookInfo("<dalvik.system.DexFile: dalvik.system.DexFile loadDex(java.lang.String, java.lang.String, int)>");
        loadDex.dexFileExtractorHookBefore(0);
        dexFileHooks.add(loadDex);
        
        return dexFileHooks;
	}

}
