package com;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.OutgoingRequestHandler;

/**
 * Author: @DilipKunderu
 */
public class Client implements Runnable {
	private ExecutorService outgoingPeersThread;
	private Map<Integer, RemotePeerInfo> connectToAbovePeers;
	private Thread activeThread;

	Client(Map<Integer, RemotePeerInfo> peersToConnectTo) {
		this.connectToAbovePeers = peersToConnectTo;
		this.outgoingPeersThread = Executors.newFixedThreadPool(this.connectToAbovePeers.size());
	}

	@Override
	public void run() {
		synchronized (this) {
			this.activeThread = Thread.currentThread();
		}
		for (Map.Entry<Integer, RemotePeerInfo> e : this.connectToAbovePeers.entrySet()) {
			RemotePeerInfo rem = e.getValue();
			try {
				this.outgoingPeersThread.execute(new OutgoingRequestHandler(rem));
			} catch (Exception ex) {
				// throw new RuntimeException("Thread pool size exceeded", ex);
			}
		}
		this.outgoingPeersThread.shutdown();
		// System.out.println("client stopped");
	}
}
