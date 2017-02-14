package de.tu_darmstadt.sse.additionalappclasses.hooking;

import java.util.Set;

import android.util.Log;
import de.tu_darmstadt.sse.additionalappclasses.tracing.BytecodeLogger;
import de.tu_darmstadt.sse.additionalappclasses.util.UtilHook;
import de.tu_darmstadt.sse.sharedclasses.SharedClassesSettings;
import de.tu_darmstadt.sse.sharedclasses.networkconnection.DecisionRequest;
import de.tu_darmstadt.sse.sharedclasses.networkconnection.NetworkConnectionInitiator;
import de.tu_darmstadt.sse.sharedclasses.networkconnection.ServerCommunicator;
import de.tu_darmstadt.sse.sharedclasses.networkconnection.ServerResponse;
import de.tu_darmstadt.sse.sharedclasses.util.Pair;

public class AnalysisDependentMethodHookBefore extends AbstractMethodHookBefore{

	
	private final String methodSignature;
	
	private Set<Pair<Integer, Object>> paramValuesToReplace;
	
	private boolean needToChangeValues;
	
	public AnalysisDependentMethodHookBefore(String methodSignature) {
		this.methodSignature = methodSignature;
	}		
	
	
	public void retrieveValueFromServer(Object[] runtimeValues) {
		// Make sure to always flush the trace before we ask for a decision
//		BytecodeLogger.dumpTracingDataSynchronous();
//		Log.i(SharedClassesSettings.TAG, "Flushed tracing queue to server");
		
		ServerCommunicator sc = NetworkConnectionInitiator.getServerCommunicator();
		int lastCodePosition = getLastCodePosition();
		DecisionRequest cRequest = new DecisionRequest(lastCodePosition, methodSignature, false);
		Object[] preparedParameter = prepareParameterForExchange(runtimeValues);
		cRequest.setRuntimeValuesOfParams(preparedParameter);		
		
		ServerResponse response = sc.getResultForRequest(cRequest);
		Log.i(SharedClassesSettings.TAG, "Retrieved decision from server");
		
		needToChangeValues = response.doesResponseExist();
		if(needToChangeValues)
			paramValuesToReplace = response.getParamValues();
	}

	@Override
	public Set<Pair<Integer, Object>> getParamValuesToReplace() {
		return paramValuesToReplace;
	}
	
	
	@Override
	public boolean isValueReplacementNecessary() {
		return needToChangeValues;
	}

	

	
	private Object[] prepareParameterForExchange(Object[] params) {
		Object[] preparedParams = new Object[params.length];
		for(int i = 0; i < params.length; i++)
			preparedParams[i] = UtilHook.prepareValueForExchange(params[i]);
		return preparedParams;
	}
}
