package com;
import java.net.Socket;

public class IncomingRequestsHandler implements Runnable {
    /**
     * need to pass this socket to the peerCommunication method;
     * this is the serverSocket that accepts connections from peers with greater Peer IDs*/
    private Socket clientSocket;
    private RemotePeerInfo remotePeerInfo;

    IncomingRequestsHandler(Socket clientSocket, RemotePeerInfo remotePeerInfo) {
        this.clientSocket = clientSocket;
        this.remotePeerInfo = remotePeerInfo;
    }
    

    @Override
    public void run() {
        PeerCommunication peerCommunication = null;
        try {
            peerCommunication = new PeerCommunication(this.remotePeerInfo, this.clientSocket);
            System.out.println("incoming request thread spawned for remote peer " + this.remotePeerInfo.get_peerID());
            peerProcess.log.TCPConnection(this.remotePeerInfo.get_peerID(), false);
        } catch (ClassNotFoundException e) {
    //        e.printStackTrace();
        }
        try {
			peerCommunication.startMessageExchange();
		} catch (Exception e) {
	//		throw new RuntimeException("Error starting message exchange in incoming request handler for " + this.remotePeerInfo.get_peerID(), e);
		}
    }
}
