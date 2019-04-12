package p2p.connection;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import p2p.RemotePojo;

public class Client implements Runnable {
	private ExecutorService outgoingPeersThread;
	private Thread activeThread;
	private Map<Integer, RemotePojo> connectToAbovePeers;
	

	@Override
	public void run() {
		synchronized (this) {
			this.setActiveThread(Thread.currentThread());
		}
		for (Map.Entry<Integer, RemotePojo> e : this.connectToAbovePeers.entrySet()) {
			RemotePojo rem = e.getValue();
			try {
				this.outgoingPeersThread.execute(new HandlerOutgoingReq(rem));
			} catch (Exception ex) {
				// ex.printStackTrace();
			}
		}
		this.outgoingPeersThread.shutdown();
	}
	
	public Client(Map<Integer, RemotePojo> peersToConnectTo) {
		this.connectToAbovePeers = peersToConnectTo;
		this.outgoingPeersThread = Executors.newFixedThreadPool(this.connectToAbovePeers.size());
	}

	public Thread getActiveThread() {
		return activeThread;
	}

	public void setActiveThread(Thread activeThread) {
		this.activeThread = activeThread;
	}
}