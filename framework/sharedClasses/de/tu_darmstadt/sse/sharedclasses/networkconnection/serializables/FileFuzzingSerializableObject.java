package de.tu_darmstadt.sse.sharedclasses.networkconnection.serializables;

import java.io.Serializable;

import de.tu_darmstadt.sse.sharedclasses.networkconnection.FileFormat;


public class FileFuzzingSerializableObject implements Serializable{
	private static final long serialVersionUID = 8055219086869125404L;

	private final FileFormat fileFormat;
	
	
	private final int storageMode;
	
	public FileFuzzingSerializableObject(FileFormat fileFormat, int storageMode) {
		this.fileFormat = fileFormat;
		this.storageMode = storageMode;
	}

	public FileFormat getFileFormat() {
		return fileFormat;
	}

	public int getStorageMode() {
		return storageMode;
	}	
	
	public String toString() {
		return String.format("file format: %s | mode: %d", fileFormat, storageMode);
	}
}
