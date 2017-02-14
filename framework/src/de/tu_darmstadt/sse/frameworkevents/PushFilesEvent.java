package de.tu_darmstadt.sse.frameworkevents;

import java.io.File;

import com.android.ddmlib.IDevice;

import de.tu_darmstadt.sse.commandlinelogger.LoggerHelper;
import de.tu_darmstadt.sse.commandlinelogger.MyLevel;
import de.tu_darmstadt.sse.sharedclasses.SharedClassesSettings;


public class PushFilesEvent extends FrameworkEvent{
	private final String dirPath;
	
	public PushFilesEvent(String dirPath) {
		this.dirPath = dirPath;
	}
	
	@Override
	public Object onEventReceived(IDevice device) {
		File fileDir = new File(dirPath);
		if(!fileDir.exists())
			throw new RuntimeException("The directory of the files which need to be pushed onto the device is not correct!");
		for(File file : fileDir.listFiles()) {
			String remoteFilePath = SharedClassesSettings.FUZZY_FILES_DIR_PATH + file.getName();
			
			try {
				device.pushFile(file.getAbsolutePath(), remoteFilePath);
			} catch (Exception e) {
				LoggerHelper.logEvent(MyLevel.EXCEPTION_RUNTIME, "Problem with pushing files onto device: " + e.getMessage());
				e.printStackTrace();
			}
		}			
		
		return null;
	}

}
