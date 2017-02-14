package de.tu_darmstadt.sse.sharedclasses.networkconnection.serializables;

import java.io.Serializable;


public class SignatureSerializableObject implements Serializable{

	private static final long serialVersionUID = -1089353033691760402L;
	
	private final byte[] encodedCertificate;
	
	public SignatureSerializableObject(byte[] encodedCertificate) {
		this.encodedCertificate = encodedCertificate;
	}

	public byte[] getEncodedCertificate() {
		return encodedCertificate;
	}
}
