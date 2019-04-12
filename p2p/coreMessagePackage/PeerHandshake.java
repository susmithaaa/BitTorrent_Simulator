package p2p.coreMessagePackage;

import java.io.*;

public class PeerHandshake implements Serializable {
	private static final long serialVersionUID = 7690652637509426786L;

	private String header;

	public PeerHandshake() {
		this.header = "P2PFILESHARINGPROJ";
	}

	public void sendHandshakeMessageToPeer(PeerHandshake hs, ObjectOutputStream o) {

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
		PeerHandshake messageReceived = convertObjectToHandShake(temp);
		return messageReceived.header.equals("P2PFILESHARINGPROJ") ? true : false;
	}

	private PeerHandshake convertObjectToHandShake(Object temp) {
		return (PeerHandshake) temp;
	}
}