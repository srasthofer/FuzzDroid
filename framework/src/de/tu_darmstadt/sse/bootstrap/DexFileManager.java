package de.tu_darmstadt.sse.bootstrap;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


public class DexFileManager {
	
	private ConcurrentMap<DexFile, DexFile> dexFiles = new ConcurrentHashMap<>();
	
	public DexFileManager() {
		
	}
	
	
	public DexFile add(DexFile dexFile) {
		DexFile ret = dexFiles.putIfAbsent(dexFile, dexFile);
		return ret == null ? dexFile : ret;
	}

}
