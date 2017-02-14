package de.tu_darmstadt.sse.bootstrap;

import java.util.Arrays;


public class DexFile {
	
	private final String fileName;
	private final String localFileName;
	private final byte[] fileContents;
	
	
	public DexFile(String fileName, String localFileName, byte[] fileContents) {
		this.fileName = fileName;
		this.localFileName = localFileName;
		this.fileContents = fileContents;
	}
	
	
	public String getFileName() {
		return this.fileName;
	}
	
	
	public String getLocalFileName() {
		return this.localFileName;
	}
	
	
	public byte[] getFileContents() {
		return this.fileContents;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(fileContents);
		result = prime * result
				+ ((fileName == null) ? 0 : fileName.hashCode());
		// We deliberately ignore the local file name. This helps ensure that we
		// don't get duplicates of the same dex file.
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DexFile other = (DexFile) obj;
		if (!Arrays.equals(fileContents, other.fileContents))
			return false;
		if (fileName == null) {
			if (other.fileName != null)
				return false;
		} else if (!fileName.equals(other.fileName))
			return false;
		// We deliberately ignore the local file name. This helps ensure that we
		// don't get duplicates of the same dex file.
		return true;
	}

}
