package de.tu_darmstadt.sse.additionalappclasses.hookdefinitions;

import java.util.Set;

import de.tu_darmstadt.sse.additionalappclasses.hooking.HookInfo;

public interface Hook {
	public Set<HookInfo> initializeHooks();
}
