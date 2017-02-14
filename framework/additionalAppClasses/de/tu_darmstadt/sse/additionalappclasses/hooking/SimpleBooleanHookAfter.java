package de.tu_darmstadt.sse.additionalappclasses.hooking;

import android.util.Log;
import de.tu_darmstadt.sse.additionalappclasses.util.UtilHook;
import de.tu_darmstadt.sse.sharedclasses.SharedClassesSettings;
import de.tu_darmstadt.sse.sharedclasses.networkconnection.DecisionRequest;
import de.tu_darmstadt.sse.sharedclasses.networkconnection.NetworkConnectionInitiator;
import de.tu_darmstadt.sse.sharedclasses.networkconnection.ServerCommunicator;
import de.tu_darmstadt.sse.sharedclasses.networkconnection.ServerResponse;


public class SimpleBooleanHookAfter extends AbstractMethodHookAfter{
	private final String methodSignature;
	
	private Object runtimeValueOfReturnAfterhooking;
	
	private boolean runtimeValueOfReturnAvailable;
	
	public SimpleBooleanHookAfter(String methodSignature) {
		this.methodSignature = methodSignature;
	}
	
	public void retrieveBooleanValueFromServer() {
		ServerCommunicator sc = NetworkConnectionInitiator.getServerCommunicator();
		int lastCodePosition = getLastCodePosition();
		DecisionRequest cRequest = new DecisionRequest(lastCodePosition, methodSignature, true);
		
		ServerResponse response = sc.getResultForRequest(cRequest);
		
		if (response == null) {
			Log.e(SharedClassesSettings.TAG, "NULL response received from server");
			runtimeValueOfReturnAvailable = false;
			runtimeValueOfReturnAfterhooking = null;
			return;
		}

		Log.i(SharedClassesSettings.TAG, "Retrieved boolean decision from server");
		runtimeValueOfReturnAvailable = response.doesResponseExist();

		if(runtimeValueOfReturnAvailable) {
			runtimeValueOfReturnAfterhooking = response.getReturnValue();
			Log.d(SharedClassesSettings.TAG, "Return value from server: "
					+ runtimeValueOfReturnAfterhooking);
		}
		else
			Log.d(SharedClassesSettings.TAG, "Server had no response value for us");
	}
	
	@Override
	public Object getReturnValue() {
		return runtimeValueOfReturnAfterhooking;
	}

	@Override
	public boolean isValueReplacementNecessary() {
		return runtimeValueOfReturnAvailable;
	}

}
