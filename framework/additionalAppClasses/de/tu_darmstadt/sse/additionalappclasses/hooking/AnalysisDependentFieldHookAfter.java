package de.tu_darmstadt.sse.additionalappclasses.hooking;

import de.tu_darmstadt.sse.additionalappclasses.util.UtilHook;
import de.tu_darmstadt.sse.sharedclasses.networkconnection.DecisionRequest;
import de.tu_darmstadt.sse.sharedclasses.networkconnection.NetworkConnectionInitiator;
import de.tu_darmstadt.sse.sharedclasses.networkconnection.ServerCommunicator;
import de.tu_darmstadt.sse.sharedclasses.networkconnection.ServerResponse;


public class AnalysisDependentFieldHookAfter extends AbstractFieldHookAfter{
	
	private boolean newValueAvailable;
	
	private Object newValue;
	
	private final String fieldSignature;
	
	public AnalysisDependentFieldHookAfter(String fieldSignature) {
		this.fieldSignature = fieldSignature;
	}
	
	
	public void retrieveValueFromServer(Object runtimeValue) {
		ServerCommunicator sc = NetworkConnectionInitiator.getServerCommunicator();
		int lastCodePosition = getLastCodePosition();
		DecisionRequest cRequest = new DecisionRequest(lastCodePosition, fieldSignature, true);
		Object cleanObject = UtilHook.prepareValueForExchange(runtimeValue);
		cRequest.setRuntimeValueOfReturn(cleanObject);
		ServerResponse response = sc.getResultForRequest(cRequest);
		newValueAvailable = response.doesResponseExist();
		if(newValueAvailable)
			newValue = response.getReturnValue();
	}	
	
	@Override
	public boolean isValueReplacementNecessary() {
		return newValueAvailable;
	}

	@Override
	public Object getNewValue() {
		return newValue;
	}

}
