package com;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable {
	private ExecutorService incomingPeersThread;
	private int portOfServer;
	private ServerSocket socketofServerReference;
	private Thread activeThread;

	Server() {
		this.activeThread = null;
		this.portOfServer = Peer.getPeerInstance().get_port();
		incomingPeersThread = Executors.newFixedThreadPool(Peer.getPeerInstance().peersToExpectConnectionsFrom.size());
	}

	@Override
	public void run() {
		System.out.println("Spawned the SERVER super thread");
		synchronized (this) {
			this.activeThread = Thread.currentThread();
		}
		try {
			this.socketofServerReference = new ServerSocket(this.portOfServer);

			for (Map.Entry<Integer, RemotePeerInfo> integerRemotePeerInfoEntry : Peer.getPeerInstance()
					.getPeersToExpectConnectionsFrom().entrySet()) {
				Socket clientSocket;

				try {
					// System.out.println("in iterator");
					clientSocket = socketofServerReference.accept();
					this.incomingPeersThread
							.execute(new IncomingReqHandler(clientSocket, integerRemotePeerInfoEntry.getValue()));
				} catch (IOException e) {
					throw new RuntimeException("Error accepting client connection", e);
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
		// System.out.println("Server stopped");
	}
}