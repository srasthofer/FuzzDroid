package de.tu_darmstadt.sse.additionalappclasses.hooking;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Set;

import android.util.Log;
import de.tu_darmstadt.sse.additionalappclasses.tracing.BytecodeLogger;
import de.tu_darmstadt.sse.sharedclasses.SharedClassesSettings;
import de.tu_darmstadt.sse.sharedclasses.util.Pair;


public class DexFileExtractorHookBefore extends AbstractMethodHookBefore{
	
	private final String methodSignature;
	private final int argumentPosition;
	
	public DexFileExtractorHookBefore(String methodSignature, int argumentPosition) {
		this.methodSignature = methodSignature;
		this.argumentPosition = argumentPosition;
	}
	
	
	public void sendDexFileToServer(String dexFilePath) {
		File dexFile = new File(dexFilePath);
		byte[] dexFileBytes = convertFileToByteArray(dexFile);
		BytecodeLogger.sendDexFileToServer(dexFilePath, dexFileBytes);
		Log.i(SharedClassesSettings.TAG, "dex file sent to client");
	}
	
	@Override
	public Set<Pair<Integer, Object>> getParamValuesToReplace() {
		// there is no need to replace any parameters
		return null;
	}

	@Override
	public boolean isValueReplacementNecessary() {
		//there is no need to replace anything
		return false;
	}

	public int getArgumentPosition() {
		return argumentPosition;
	}
	
	private byte[] convertFileToByteArray(File dexFile) {
		FileInputStream fin = null;
		byte fileContent[] = null;
		try {
			fin = new FileInputStream(dexFile);
			fileContent = new byte[(int)dexFile.length()];			
			fin.read(fileContent);			
		}
		catch (FileNotFoundException e) {
			Log.e(SharedClassesSettings.TAG, "File not found" + e);
		}
		catch (IOException ioe) {
			Log.e(SharedClassesSettings.TAG, "Exception while reading file " + ioe);
		}
		finally {
			try {
				if (fin != null) {
					fin.close();
				}
			}
			catch (IOException ioe) {
				Log.e(SharedClassesSettings.TAG, "Error while closing stream: " + ioe);
			}
		}
		
		return fileContent;
	}
}
