package de.tu_darmstadt.sse.sharedclasses.tracing;

import java.io.File;

import de.tu_darmstadt.sse.sharedclasses.SharedClassesSettings;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Environment;
import android.util.Log;


@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class FileBasedTracingUtils {
	
	
	public static File getFuzzerDirectory() {
		File storageDir = new File(Environment.getExternalStorageDirectory(),
				"evoFuzzDumps/");
		if (!storageDir.exists() && !storageDir.mkdirs())
			Log.e(SharedClassesSettings.TAG, "Could not create communication directory for watchdog: " + storageDir);
		storageDir.setWritable(true, false);
		Log.i(SharedClassesSettings.TAG, "Communication directory for watchdog: " + storageDir);
		return storageDir;
	}

}
