package de.tu_darmstadt.sse.frameworkevents;

import com.android.ddmlib.MultiLineReceiver;


public class GenericReceiver extends MultiLineReceiver {

    public GenericReceiver() {
    }

    @Override
    public void processNewLines(String[] lines) {
        for (String line : lines) {
            System.out.format("[ADB] %s\n", line);
        }
    }

    @Override
    public boolean isCancelled() {
        return false;
    }
}
