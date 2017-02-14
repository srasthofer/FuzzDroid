package de.tu_darmstadt.sse.sharedclasses.networkconnection.serializables;

import java.io.Serializable;

import de.tu_darmstadt.sse.sharedclasses.networkconnection.IClientRequest;


public class BinarySerializableObject implements Serializable, IClientRequest {
	
	
	private static final long serialVersionUID = -1043817079853486666L;
	
	private final byte[] binaryData;
	
	
	public BinarySerializableObject(byte[] binaryData) {
		this.binaryData = binaryData;
	}
	
	
	public byte[] getBinaryData() {
		return this.binaryData;
	}

}
