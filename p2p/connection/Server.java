package p2p.connection;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import p2p.RemotePojo;
import p2p.peer.Peer;

public class Server implements Runnable {
	private ExecutorService incomingPeersThread;
	private ServerSocket socketofServerReference;
	private Thread activeThread;
	private int portOfServer;
	

	@Override
	public void run() {
		System.out.println("Server super thread Spawned");
		synchronized (this) {
			this.setActiveThread(Thread.currentThread());
		}
		try {
			this.socketofServerReference = new ServerSocket(this.portOfServer);

			for (Map.Entry<Integer, RemotePojo> remotePojoEntrySet : Peer.getPeerObj()
					.anticipateConnectionsFromBelowPeers.entrySet()) {
				Socket clientSocket;

				try {
					clientSocket = socketofServerReference.accept();
					this.incomingPeersThread
							.execute(new HandlerIncomingReq(remotePojoEntrySet.getValue(), clientSocket));
				} catch (IOException e) {
					throw new RuntimeException("Error in accepting client connection", e);
				}
			}
			this.incomingPeersThread.shutdown();
		} catch (IOException e) {
			if (socketofServerReference != null && !socketofServerReference.isClosed()) {
				try {
					socketofServerReference.close();
				} catch (IOException e1) {
					e1.printStackTrace(System.err);
				}
			}
		}
	}
	
	
	public Server() {
		this.setActiveThread(null);
		this.portOfServer = Peer.getPeerObj().peerPortNo;
		incomingPeersThread = Executors.newFixedThreadPool(Peer.getPeerObj().anticipateConnectionsFromBelowPeers.size());
	}


	public Thread getActiveThread() {
		return activeThread;
	}


	public void setActiveThread(Thread activeThread) {
		this.activeThread = activeThread;
	}
}