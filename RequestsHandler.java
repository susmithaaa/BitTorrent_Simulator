package com;

import java.net.Socket;

public class RequestsHandler {

}

class OutgoingRequestHandler implements Runnable {
	private RemotePeerInfo remotePeerInfo;

	OutgoingRequestHandler(RemotePeerInfo remote) {
		this.remotePeerInfo = remote;
	}

	@Override
	public void run() {
		// System.out.println("OUTGOING REQUESTS HANDLER");
		PeerCommunication peerCommunication = null;
		// try {
		peerCommunication = new PeerCommunication(this.remotePeerInfo);
		System.out.println("outgoing handler thread spawned for remote peer " + this.remotePeerInfo.get_peerID());
		peerProcess.log.TCPConnection(this.remotePeerInfo.get_peerID(), true);
		// } catch (ClassNotFoundException e) {
		// e.printStackTrace();
		// }
		try {
			peerCommunication.startExchangingMessages();
		} catch (Exception e) {
			/*
			 * throw new
			 * RuntimeException("Error starting message exchange in outgoing request handler for "
			 * + this.remotePeerInfo.get_peerID(), e);
			 */
		}
	}
}

class IncomingReqHandler implements Runnable {
	private Socket clientSocket;
	private RemotePeerInfo remotePeerInfo;

	IncomingReqHandler(Socket clientSocket, RemotePeerInfo remotePeerInfo) {
		this.clientSocket = clientSocket;
		this.remotePeerInfo = remotePeerInfo;
	}

	@Override
	public void run() {
		// System.out.println("INCOMING REQUESTS HANDLER");
		PeerCommunication peerCommunication = null;
		try {
			peerCommunication = new PeerCommunication(this.remotePeerInfo, this.clientSocket);
			System.out.println("incoming request thread spawned for remote peer " + this.remotePeerInfo.get_peerID());
			peerProcess.log.TCPConnection(this.remotePeerInfo.get_peerID(), false);
		} catch (ClassNotFoundException e) {
			// e.printStackTrace();
		}
		try {
			peerCommunication.startExchangingMessages();
		} catch (Exception e) {
			// throw new RuntimeException("Error starting message exchange in incoming
			// request handler for " + this.remotePeerInfo.get_peerID(), e);
		}
	}
}
