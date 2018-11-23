package com.messages;

import java.io.*;
// import com.Constants;

public class Handshake implements Serializable {
	private static final long serialVersionUID = 6529685098267857690L;

	private String header;
	// private String zero_bits;
	// private int peer_ID;

	public Handshake(int peer_ID) {
		this.header = "P2PFILESHARINGPROJ";
		// this.zero_bits = Constants.ZERO_BITS;
		// this.peer_ID = peer_ID;
	}

	/*
	 * @Override public String toString() { return this.header + this.zero_bits +
	 * String.valueOf(this.peer_ID); }
	 */

	public void sendHandshakeMessageToPeer(Handshake hs, ObjectOutputStream o) {

		try {
			o.writeObject(hs);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public boolean receiveHandshakeFromPeer(ObjectInputStream inputStreamRef) {
		Object temp = null;
		try {
			temp = inputStreamRef.readObject();
		} catch (IOException t) {
			t.printStackTrace();
		} catch (ClassNotFoundException t) {
			t.printStackTrace();
		}
		Handshake messageReceived = convertObjectToHandShake(temp);
		return messageReceived.header.equals("P2PFILESHARINGPROJ") ? true : false;
	}

	private Handshake convertObjectToHandShake(Object temp) {
		return (Handshake) temp;
	}
}
