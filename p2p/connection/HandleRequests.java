package p2p.connection;

import java.net.Socket;

import p2p.RemotePojo;
import p2p.peerProcess;
import p2p.peer.PeerInteraction;

public class HandleRequests {

}

class HandlerOutgoingReq implements Runnable {
	private RemotePojo informationRemotePeer;
	private Socket socketOfClient;

	HandlerOutgoingReq(RemotePojo rem) {
		this.informationRemotePeer = rem;
		this.socketOfClient = null;
	}

	@Override
	public void run() {
		PeerInteraction peerInteraction = null;
		try {
			peerInteraction = new PeerInteraction(this.socketOfClient, this.informationRemotePeer);
		} catch (ClassNotFoundException e1) {
			// e1.printStackTrace();
		}
		System.out.println("Created Outgoing handler thread for remote peer " + this.informationRemotePeer.getidOfPeer());
		peerProcess.log.logConnection(this.informationRemotePeer.getidOfPeer(), true);
		try {
			peerInteraction.beginMessageInterchange();
		} catch (Exception e) {

		}
	}
}

class HandlerIncomingReq implements Runnable {
	
	private RemotePojo infoRemotePeer;
	private Socket socketOfClient;

	HandlerIncomingReq(RemotePojo remotePojo, Socket clientSocket) {
		this.socketOfClient = clientSocket;
		this.infoRemotePeer = remotePojo;
	}

	@Override
	public void run() {
		PeerInteraction peerInteraction = null;
		try {
			peerInteraction = new PeerInteraction(this.socketOfClient, this.infoRemotePeer);
			System.out.println("Created incoming request thread for remote peer " + this.infoRemotePeer.getidOfPeer());
			peerProcess.log.logConnection(this.infoRemotePeer.getidOfPeer(), false);
		} catch (ClassNotFoundException e) {
			// e.printStackTrace();
		}
		try {
			peerInteraction.beginMessageInterchange();
		} catch (Exception e) {
		}
	}
}
