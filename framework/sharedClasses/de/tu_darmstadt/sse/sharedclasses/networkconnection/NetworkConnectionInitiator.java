package de.tu_darmstadt.sse.sharedclasses.networkconnection;



public class NetworkConnectionInitiator {
	
	public static Object syncToken = new Object();
	private static ServerCommunicator sc = null;
	
	public static void initNetworkConnection() {
		sc = new ServerCommunicator(syncToken);
	}
	
	public static ServerCommunicator getServerCommunicator() {
		return sc;
	}
}
