package de.tu_darmstadt.sse.sharedclasses.networkconnection;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import android.util.Log;
import de.tu_darmstadt.sse.sharedclasses.SharedClassesSettings;
import de.tu_darmstadt.sse.sharedclasses.util.NetworkSettings;


public class ServerCommunicator{
	
	
	public final Queue<ServerResponse> serverAnswers = new ConcurrentLinkedQueue<ServerResponse>();
	
	private final Object syncToken;
	
	public ServerCommunicator(Object syncToken) {
		this.syncToken = syncToken;
	}
	
	
	public ServerResponse getResultForRequest(IClientRequest request) {
		synchronized (syncToken) {
			ClientThread client = new ClientThread(syncToken, request);
			Thread thread = new Thread(client);
			thread.start();
			try {
				syncToken.wait();
				ServerResponse response = serverAnswers.poll();
				return response;
			} catch (InterruptedException e) {
				e.printStackTrace();
				return null;
			}	
		}
	}
	
	
	public void send(final Collection<IClientRequest> request,
			final boolean waitForResponse) {
		Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				// No need to send empty requests
				if (request.isEmpty())
					return;
				
				Socket socket = null;
				ObjectOutputStream oos = null;
				ObjectInputStream ois = null;
				try {
					try{
						socket = new Socket(NetworkSettings.SERVER_IP, NetworkSettings.SERVERPORT_OBJECT_TRANSFER);	
						if(!socket.isConnected()) {
							socket.close();
							throw new RuntimeException("Socket is not established");
						}
						
						// Create the streams
						oos = new ObjectOutputStream(socket.getOutputStream());
						ois = new ObjectInputStream(socket.getInputStream());
						
						// Send the requests to the server
						Log.i(SharedClassesSettings.TAG, String.format("Sending %d events to server...",
								request.size()));
						for (IClientRequest icr : request)
							oos.writeObject(icr);
						oos.flush();
						
						// Wait for all objects to be acknowledged before closing
						// the connection
						Log.i(SharedClassesSettings.TAG, "Waiting for server confirmation ("
								+ Thread.currentThread().getId() + ")...");
						for (int i = 0; i < request.size(); i++) {
							ois.readObject();
//							Log.i(SharedClassesSettings.TAG, String.format("Received %d/%d confirmation responses", i+1, request.size()));
						}
						
						// Tell the server that we're ready to close the connection
						Log.i(SharedClassesSettings.TAG, "All objects confirmed, closing connection...");
						oos.writeObject(new CloseConnectionRequest());
						oos.flush();
						socket.shutdownOutput();
						
						// Make sure that the server isn't already dead as a doornail
						ois.mark(1);
						if (ois.read() != -1) {
						    ois.reset();
						    
							// Wait for the server to acknowledge that it's going away
							Log.i(SharedClassesSettings.TAG, "Waiting for server shutdown confirmation...");
							ois.readObject();
							Log.i(SharedClassesSettings.TAG, "Confirmation received.");
							// We close the socket anyway
	//						if (socket.isConnected() && !socket.isClosed() && !socket.isInputShutdown())
	//							socket.shutdownInput();
							
							Log.i(SharedClassesSettings.TAG, "OK, request handling done");
						}
						Log.i(SharedClassesSettings.TAG, "Connection closed.");
					}finally {
						socket.close();
					}
				}
				catch (IOException | ClassNotFoundException e) {
					e.printStackTrace();
				}
				
				if (syncToken != null && waitForResponse) {
					synchronized (syncToken) {
						syncToken.notify();
					}
				}
				
				Log.i(SharedClassesSettings.TAG, "End of SEND thread (" + Thread.currentThread().getId() + ").");
			}
			
		});
		thread.start();
		
		// Wait for completion if we have to
		try {
			if (syncToken != null && waitForResponse)
				synchronized (syncToken) {
					syncToken.wait();					
				}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	
	private class ClientThread implements Runnable {
		
		private final Object syncToken;
		private final IClientRequest request;
		
		public ClientThread(Object syncToken, IClientRequest request) {
			this.syncToken = syncToken;
			this.request = request;
		}
		
		
		@Override
		public void run() {
			if(request == null)
				throw new RuntimeException("Server-Request should not be null!");
			Socket socket = null;
			ObjectOutputStream oos = null;
			ObjectInputStream ois = null;
			try {
				try{
					socket = new Socket(NetworkSettings.SERVER_IP, NetworkSettings.SERVERPORT_OBJECT_TRANSFER);	
					if(!socket.isConnected()) {
						socket.close();
						throw new RuntimeException("Socket is not established");
					}
					
					// Create our streams
					oos = new ObjectOutputStream(socket.getOutputStream());
					ois = new ObjectInputStream(socket.getInputStream());
					
					// Send the request
					Log.i(SharedClassesSettings.TAG, "Sending single event to server...");
					sendRequest(oos);
					
					// Wait for the response
					Log.i(SharedClassesSettings.TAG, "Waiting for single server response...");										
					
					ServerResponse response = getResponse(ois);
					
					// Even in case the connection dies, take what we have and run.
					Log.i(SharedClassesSettings.TAG, "OK, done.");
					Log.i(SharedClassesSettings.TAG, response.toString());
					serverAnswers.add(response);
					
					// Tell the server that we're ready to close the connection
					Log.i(SharedClassesSettings.TAG, "All objects confirmed, closing connection...");
					oos.writeObject(new CloseConnectionRequest());
					oos.flush();
					socket.shutdownOutput();
					
					// Wait for the server to acknowledge that it's going away
					// Make sure that the server isn't already dead as a doornail
					ois.mark(1);
					if (ois.read() != -1) {
					    ois.reset();
						
					    Log.i(SharedClassesSettings.TAG, "Waiting for server shutdown confirmation...");
						ois.readObject();
						
						// We close the socket anyway
	//					if (socket.isConnected() && !socket.isClosed() && !socket.isInputShutdown())
	//						socket.shutdownInput();
					}
				} finally {
					socket.close();
				}
			}
			catch (IOException | ClassNotFoundException e) {			
				e.printStackTrace();
			}
			
			if (syncToken != null) {
				synchronized(syncToken) {
					syncToken.notify();
				}
			}
			
			Log.i(SharedClassesSettings.TAG, "End of CLIENT thread.");
		}
		
		
		private void sendRequest(ObjectOutputStream out) throws IOException {
			out.writeObject(request);
		}
		
		
		private ServerResponse getResponse(ObjectInputStream input) throws ClassNotFoundException, IOException {
			ServerResponse response = (ServerResponse)input.readObject();
			return response;			 
		}
	}


}
