package com;

public class OutgoingRequestsHandler implements Runnable {
	private RemotePeerInfo remotePeerInfo;

	OutgoingRequestsHandler(RemotePeerInfo remote) {
		this.remotePeerInfo = remote;
	}

	@Override
	public void run() {
		PeerCommunication peerCommunication = null;
		try {
			peerCommunication = new PeerCommunication(this.remotePeerInfo);
			System.out.println("outgoing handler thread spawned for remote peer " + this.remotePeerInfo.get_peerID());
            peerProcess.log.TCPConnection(this.remotePeerInfo.get_peerID(), true);
		} catch (ClassNotFoundException e) {
		//	e.printStackTrace();
		}
		try {
			peerCommunication.startMessageExchange();
		} catch (Exception e) {
			/*throw new RuntimeException("Error starting message exchange in outgoing request handler for "
					+ this.remotePeerInfo.get_peerID(), e);*/
		}
	}
}
