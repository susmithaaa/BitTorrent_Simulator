package com;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Peer {
	private PeerCommunicationHelper helperObj;
	private Timer time_prefNeighbour, timer_optNeighbour;
	private static Peer peerObj;
	private volatile BitSet bitSetField;

	private volatile RemotePeerInfo optUnchNeighbour; // timer based

	Map<Integer, RemotePeerInfo> peersToConnectTo; // set in peerProcess and then not changed
	Map<Integer, RemotePeerInfo> peersToExpectConnectionsFrom; // set in peerProcess and then not changed

	volatile List<RemotePeerInfo> connectedPeers; // set right after heach handshake
	// List<RemotePeerInfo> connectedPeersAux;

	volatile List<RemotePeerInfo> chokedPeers; // just a reference, being manipulated in this class
	volatile List<RemotePeerInfo> unchokedPeers;

	volatile Map<RemotePeerInfo, BitSet> preferredNeighbours; // timer based
	volatile Map<Integer, RemotePeerInfo> peersInterested; // set by threads

	/**
	 * This map will be used to index running threads of the peer
	 */

	private int _peerID;
	private String _hostName;
	private int _port;
	public int _hasFile;
	private int _pieceCount;

	// public int handShakeCount;

	/**
	 * This Bitset is used for terminating conditions
	 */
	BitSet idealBitset;
	public int expected;

	public RemotePeerInfo getOptimisticallyUnchokedNeighbour() {
		return this.optUnchNeighbour;
	}

	public Map<Integer, RemotePeerInfo> getPeersToConnectTo() {
		return peersToConnectTo;
	}

	public Map<Integer, RemotePeerInfo> getPeersToExpectConnectionsFrom() {
		return peersToExpectConnectionsFrom;
	}

	public Map<Integer, RemotePeerInfo> getPeersInterested() {
		return peersInterested;
	}

	BitSet getBitSet() {
		return bitSetField;
	}

	public int get_peerID() {
		return _peerID;
	}

	void set_peerID(int _peerID) {
		this._peerID = _peerID;
	}

	String get_hostName() {
		return _hostName;
	}

	int get_port() {
		return _port;
	}

	void set_hostName(String _hostName) {
		this._hostName = _hostName;
	}

	void set_port(int _port) {
		this._port = _port;
	}

	int get_hasFile() {
		return _hasFile;
	}

	public boolean get_bitField(int i) {
		return bitSetField.get(i);
	}

	void set_hasFile(int _hasFile) {
		this._hasFile = _hasFile;
	}

	int get_pieceCount() {
		return _pieceCount;
	}

	private void set_bitField(int i) {
		this.bitSetField.set(i);
	}

	public void set_pieceCount() {
		int f = Constants.getFileSize();
		int p = Constants.getPieceSize();

		this._pieceCount = (int) Math.ceil((double) f / p);
	}

	public void setBitset() {
		for (int i = 0; i < get_pieceCount(); i++) {
			peerObj.set_bitField(i);
		}
	}

	private Peer() {
		helperObj = new PeerCommunicationHelper();
		this.bitSetField = new BitSet(this.get_pieceCount());
		this.expected = 0;
		this.peersToConnectTo = Collections.synchronizedMap(new TreeMap<>());
		this.peersToExpectConnectionsFrom = Collections.synchronizedMap(new TreeMap<>());
		this.connectedPeers = Collections.synchronizedList(new ArrayList<>());
		// this.connectedPeersAux = Collections.synchronizedList(new ArrayList<>());
		this.chokedPeers = Collections.synchronizedList(new ArrayList<>());
		this.unchokedPeers = Collections.synchronizedList(new ArrayList<>());
		this.peersInterested = Collections.synchronizedMap(new HashMap<>());
		this.idealBitset = new BitSet(this.get_pieceCount());

		for (int i = 0; i < this.idealBitset.length(); i++)
			this.idealBitset.set(i);
	}

	public static Peer getPeerInstance() {
		if (peerObj == null) {
			synchronized (Peer.class) {
				if (peerObj == null)
					peerObj = new Peer();
			}
		}
		return peerObj;
	}

	public void sendHaveToAll(int receivedId) {
		for (int i = 0; i < this.connectedPeers.size(); i++) {
			try {
				RemotePeerInfo rp = this.connectedPeers.get(i);
				helperObj.sendHaveMsg(rp.objectOutputStream, receivedId);
				rp.objectOutputStream.flush();
			} catch (Exception e) {
				// e.printStackTrace();
			}
		}
	}

	/***************************************
	 * Timer Based Tasks
	 ************************************************************/

	void OptimisticallyUnchokedNeighbour() {
		// this.optimisticallyUnchokedNeighbour =
		// this.connectedPeers.get(ThreadLocalRandom.current().nextInt(this.connectedPeers.size()));

		TimerTask recursion = new TimerTask() {
			@Override
			public void run() {
				if (Peer.getPeerInstance().checkKill()) {
					Peer.getPeerInstance().timer_optNeighbour.cancel();
					Peer.getPeerInstance().timer_optNeighbour.purge();
					System.out.println("inside optimistically unchoked neighbour came to system exit 0");
				//	System.exit(0);
				} else {
					setOptimisticallyUnchokedNeighbour();
				}
			}
		};

		timeCal_Opt(Constants.getOptimisticUnchokingInterval(), recursion);
	}

	private void setOptimisticallyUnchokedNeighbour() {
		this.optUnchNeighbour = this.connectedPeers
				.get(ThreadLocalRandom.current().nextInt(this.connectedPeers.size()));
		List<RemotePeerInfo> willingPeers = new ArrayList<>(this.peersInterested.values());

		// willingPeers.removeIf(r -> !this.chokedPeers.contains(r)); // Check once
		// *************************************************
		List<RemotePeerInfo> templi = willingPeers;
		for (RemotePeerInfo rp : templi) {
			if (this.chokedPeers.indexOf(rp) != -1)
				willingPeers.remove(rp);
		}

		int willingPeersLen = willingPeers.size();
		if (willingPeersLen > 0) {
			RemotePeerInfo pOptimistic = willingPeers.get(ThreadLocalRandom.current().nextInt(willingPeersLen));
			// System.out.println("There are no choked Peers currently");
			modifyChoke_UnChoke(pOptimistic);
			
			while (willingPeers.size() != 0) {
				willingPeers.remove(0);
			}

			try {
				List<RemotePeerInfo> remotePeerKeySet = new ArrayList<>(this.preferredNeighbours.keySet());
				if (remotePeerKeySet.indexOf(this.optUnchNeighbour) == -1) {
					helperObj.sendMessage(this.optUnchNeighbour.objectOutputStream, MessageType.choke);
					this.optUnchNeighbour.setState(MessageType.choke);
				}

			} catch (Exception e) {
				// e.printStackTrace();
			}

			try {
				helperObj.sendMessage(pOptimistic.objectOutputStream, MessageType.unchoke);
				pOptimistic.setState(MessageType.unchoke);

			} catch (Exception e) {
				// e.printStackTrace();
			}

			this.optUnchNeighbour = pOptimistic;
			// System.out.println(this.optUnchNeighbour.get_peerID());
			peerProcess.log.changeOfOptimisticallyUnchokedNeighbor(this.optUnchNeighbour.get_peerID());
		}
	}

	private void modifyChoke_UnChoke(RemotePeerInfo rp) {
		this.chokedPeers.remove(rp);
		this.unchokedPeers.add(rp);
	}

	void PreferredNeighbours() {
		preferredNeighbours = Collections.synchronizedMap(new HashMap<>());

		TimerTask recursion = new TimerTask() {
			@Override
			public void run() {
				if (Peer.getPeerInstance().checkKill()) {
					Peer.getPeerInstance().time_prefNeighbour.cancel();
					Peer.getPeerInstance().time_prefNeighbour.purge();
					System.out.println("inside preferred neighbours system exit 0");
				} else {
					setPreferredNeighbours();
				}
			}
		};

		timeCal_Pref(Constants.getUnchokingInterval(), recursion);
	}

	private void timeCal_Pref(int interval, TimerTask timer) {
		this.time_prefNeighbour = new Timer();
		long late = (long) interval * 1000;
		long span = (long) interval * 1000;
		this.time_prefNeighbour.scheduleAtFixedRate(timer, late, span);
	}

	private void timeCal_Opt(int interval, TimerTask timer) {
		this.timer_optNeighbour = new Timer();
		long late = (long) interval * 1000;
		long span = (long) interval * 1000;
		this.timer_optNeighbour.scheduleAtFixedRate(timer, late, span);
	}

	private void setPreferredNeighbours() {
		/**
		 * This list gets populated whenever there is a file transfer going on between
		 * the local peer and the corresponding remote peer. For that cycle, the state
		 * for the remote peer remains unchoked.
		 */
		List<RemotePeerInfo> remotePeerLi = new ArrayList<>(this.peersInterested.values());
		Map<RemotePeerInfo, BitSet> preferedTempMap = new HashMap<>();

		/**
		 * This queue is used to add remote peer objects into the preferred neighbours
		 * map, going by the associated download rate.
		 **/

		int remotePeerLen = remotePeerLi.size();
		if (remotePeerLen > 0) {
			// this.preferredNeighbours.clear();

			if (this._hasFile == 1) { // randomly choose preferred
				int tempcount = 0;
				while (tempcount < remotePeerLen)
					tempcount++;
				tempcount = 0;
				while (remotePeerLen > 0 && tempcount < Constants.getNumberOfPreferredNeighbors()) {
					tempcount++;
					// System.out.println("BOUND =
					// "+ThreadLocalRandom.current().nextInt(remotePeerLi.size()));
					RemotePeerInfo remotePeerData = null;
					try {
						int tempNum = ThreadLocalRandom.current().nextInt(remotePeerLen);
						remotePeerData = remotePeerLi.get(tempNum);
						decider(remotePeerData);
					} catch (Exception ex) {
						// ex.printStackTrace();
					}

					BitSet remoteBitset = null;
					remoteBitset = remotePeerData.getBitfield();
					preferedTempMap.put(remotePeerData, remoteBitset);
					if (this.unchokedPeers.indexOf(remotePeerData) == -1)
						this.unchokedPeers.add(remotePeerData);
					this.chokedPeers.remove(remotePeerData);
					remotePeerLi.remove(remotePeerData);
				}

				for (RemotePeerInfo rp : remotePeerLi) {
					choker(rp);
					remotePeerLi.remove(0);
				}
				
			} else {
				Collections.sort(remotePeerLi);

				RemotePeerInfo remotePeerData;

				int count = 0;

				while (!remotePeerLi.isEmpty() && count < Constants.getNumberOfPreferredNeighbors()) {
					int z = 0;
					remotePeerData = remotePeerLi.get(z);
					decider(remotePeerData);
					
					BitSet remoteBitset = null;
					// preferedTempMap.getOrDefault(remotePeerData, new BitSet());
					remoteBitset = remotePeerData.getBitfield();
					preferedTempMap.put(remotePeerData, remoteBitset);
					// this.preferredNeighbours.put(remote, remote.getBitfield());
					modifyChoke_UnChoke(remotePeerData);
					count++;
					remotePeerLi.remove(0);
				}

				for (RemotePeerInfo r : remotePeerLi) {
					choker(r);
				}
			}
		}

		this.preferredNeighbours = preferedTempMap;

		if (preferredNeighbours.size() > 0)
			peerProcess.log.changeOfPreferredNeighbors(this.preferredNeighbours);
	}

	private void decider(RemotePeerInfo remoteOne) {
		if (remoteOne != null) {
			if (remoteOne.getState() == MessageType.choke) {
				int count = 0, j = 20;
				for (int i = 0; i < j; i++)
					count++;
				try {
					helperObj.sendMessage(remoteOne.objectOutputStream, MessageType.unchoke);
					remoteOne.setState(MessageType.unchoke);
					System.out.println("unchoke message sent to " + remoteOne.get_peerID() + " from the peer "
							+ Peer.getPeerInstance().get_peerID());
				} catch (Exception ex) {
					throw new RuntimeException("Unable to send unchoke message from the required peer class", ex);
				}
			}
		}
	}

	private void choker(RemotePeerInfo remoteOne) {
		try {
			helperObj.sendMessage(remoteOne.objectOutputStream, MessageType.choke);
			remoteOne.setState(MessageType.choke);
			System.out.println("choke sent to" + remoteOne.get_peerID() + "from" + Peer.getPeerInstance().get_peerID());
			if (this.chokedPeers.indexOf(remoteOne) == -1)
				this.chokedPeers.add(remoteOne);
			this.unchokedPeers.remove(remoteOne);
		} catch (Exception e) {
			// throw new RuntimeException(e);
		}
	}

	public boolean checkKill() {

		BitSet finalbitset = this.idealBitset;
		int connectedpeersize = 0;
		while (connectedpeersize < this.connectedPeers.size()) {
			connectedpeersize++;
		}
		if (Peer.getPeerInstance().getBitSet().equals(finalbitset)) {
			int count = 0;
			for (RemotePeerInfo remotePeerInfo : this.connectedPeers) {
				if (remotePeerInfo.getBitfield().equals(finalbitset))
					count++;// Kill the peer once all the connected and itself are having idealbitset.
			}
			return this.connectedPeers.size() != 0 && count == this.connectedPeers.size();
		} else
			return false;
	}
}