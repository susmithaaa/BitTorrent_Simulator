package p2p.peer;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import p2p.Defaults;
import p2p.RemotePojo;
import p2p.TypeOfMessage;
import p2p.peerProcess;

public class Peer {
	private PeerInteractionUtility helperObj;
	private Timer time_prefNeighbour, timer_optNeighbour;
	private static Peer peerObj;
	private volatile BitSet bitSetField;

	private volatile RemotePojo optUnchNeighbour;

	public Map<Integer, RemotePojo> connectToAbovePeers;

	public int peerCountPieces;
	
	public int peerPortNo;
	public int peerGotFile;
	private int idOfPeer;

	volatile List<RemotePojo> interLinkedPeers, peersCurrentlyUnchoked, peersCurrentlyChoked;
	
	public Map<Integer, RemotePojo> anticipateConnectionsFromBelowPeers;

	volatile Map<RemotePojo, BitSet> likedNeighbours;
	volatile Map<Integer, RemotePojo> willingPeers;

	public BitSet bitsetWanted;
	public int expected;

	public int getIdOfPeer() {
		return idOfPeer;
	}

	public void setIdOfPeer(int _peerID) {
		this.idOfPeer = _peerID;
	}


	public void setPeerPortNo(int p) {
		this.peerPortNo = p;
	}
	
	BitSet fetchSetofBits() {
		return bitSetField;
	}

	public boolean getBitsetField(int j) {
		return bitSetField.get(j);
	}
	
	public void setBitset() {
		int j=0, count = peerCountPieces;
		while(j<count) {
			this.bitSetField.set(j);
			j++;
		}
	}

	public void setPeerGotFile(int g) {
		this.peerGotFile = g;
	}

	public void set_pieceCount() {
		int pieceSize = Defaults.sizeOfPiece;
		int fileSize = Defaults.sizeOfFile;

		this.peerCountPieces = (int) Math.ceil((double) fileSize / pieceSize);
	}

	private Peer() {
		helperObj = new PeerInteractionUtility();
		this.connectToAbovePeers = Collections.synchronizedMap(new TreeMap<>());
		this.anticipateConnectionsFromBelowPeers = Collections.synchronizedMap(new TreeMap<>());
		this.bitSetField = new BitSet(this.peerCountPieces);
		this.expected = 0;
		this.interLinkedPeers = Collections.synchronizedList(new ArrayList<>());
		this.willingPeers = Collections.synchronizedMap(new HashMap<>());
		this.bitsetWanted = new BitSet(this.peerCountPieces);
		int count = this.bitsetWanted.length();
		this.peersCurrentlyChoked = Collections.synchronizedList(new ArrayList<>());
		this.peersCurrentlyUnchoked = Collections.synchronizedList(new ArrayList<>());

		for(int j = 0; j < count; j++) this.bitsetWanted.set(j);
	}

	public void publishHavePieceToOtherPeers(int pieceidx, int con) {
		int temp = con;
		for(int i=0; i<temp; i++) {
			con--;
		}
		for (int i = 0; i < this.interLinkedPeers.size(); i++) {
			try {
				RemotePojo rp = this.interLinkedPeers.get(i);
				helperObj.sendMessageHave(rp.oOS, 0, pieceidx);
				rp.oOS.flush();
			} catch (Exception e) {
				
			}
		}
	}
	
	public static Peer getPeerObj() {
		if (peerObj == null) {
			synchronized (Peer.class) {
				if (peerObj == null)
					peerObj = new Peer();
			}
		}
		return peerObj;
	}


	private void setOptiUnchNeighbor() {
		this.optUnchNeighbour = this.interLinkedPeers
				.get(localRandThreadNum(this.interLinkedPeers.size()));
		List<RemotePojo> willingPeers = new ArrayList<>(this.willingPeers.values());

		List<RemotePojo> templi = willingPeers;
		for (RemotePojo rp : templi) {
			if (this.peersCurrentlyChoked.indexOf(rp) != -1)
				willingPeers.remove(rp);
		}

		int willingPeersLen = willingPeers.size();
		if (willingPeersLen > 0) {
			RemotePojo pOptimistic = willingPeers.get(localRandThreadNum(willingPeersLen));
			modifyChoke_UnChoke(pOptimistic);
			while (willingPeers.size() != 0) {
				willingPeers.remove(0);
			}

			try {
				List<RemotePojo> remotePeerKeySet = new ArrayList<>(this.likedNeighbours.keySet());
				if (remotePeerKeySet.indexOf(this.optUnchNeighbour) == -1) {
					helperObj.sendwithoutPayloadMessages(this.optUnchNeighbour.oOS, 0, TypeOfMessage.choke);
					this.optUnchNeighbour.setCurrState(TypeOfMessage.choke);
				}

			} catch (Exception e) {
				// e.printStackTrace();
			}

			try {
				helperObj.sendwithoutPayloadMessages(pOptimistic.oOS, 0, TypeOfMessage.unchoke);
				pOptimistic.setCurrState(TypeOfMessage.unchoke);

			} catch (Exception e) {
				// e.printStackTrace();
			}

			this.optUnchNeighbour = pOptimistic;
		//	System.out.println("Optimistically unchoked neighbor is - " + this.optUnchNeighbour.getidOfPeer());
			peerProcess.log.logOptimisticallyUnchokedNeig(this.optUnchNeighbour.getidOfPeer());
		}
	}
	
	public void fetchOptiUnchNeighbor() {

		TimerTask recursion = new TimerTask() {
			@Override
			public void run() {
				if (Peer.getPeerObj().checkselfandNeighboringPeersGotFile("Peer")) {
					Peer.getPeerObj().timer_optNeighbour.cancel();
					Peer.getPeerObj().timer_optNeighbour.purge();
					System.out.println("Inside optimistically unchoked neighbour came to system exit 0");
					System.exit(0);
				} else {
					setOptiUnchNeighbor();
				//	System.exit(0);
				}
			}
		};

		timeCal_Opt(Defaults.IntervalForOptimisticUnchoking, recursion);
	}

	private void modifyChoke_UnChoke(RemotePojo rp) {
		this.peersCurrentlyChoked.remove(rp);
		this.peersCurrentlyUnchoked.add(rp);
	}

	public void likedNeighbors() {
		likedNeighbours = Collections.synchronizedMap(new HashMap<>());

		TimerTask recursion = new TimerTask() {
			@Override
			public void run() {
				if (Peer.getPeerObj().checkselfandNeighboringPeersGotFile("Peer")) {
					Peer.getPeerObj().time_prefNeighbour.cancel();
					Peer.getPeerObj().time_prefNeighbour.purge();
					System.out.println("System exit 0 inside preferred neighbours");
					System.exit(0);
				} else {
					setLikedNeighbors(0);
				}
			}
		};

		timeCal_Pref(Defaults.IntervalForUnchoking, recursion);
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

	private void setLikedNeighbors(int con) {

		List<RemotePojo> remotePeerLi = new ArrayList<>(this.willingPeers.values());
		Map<RemotePojo, BitSet> preferedTempMap = new HashMap<>();

		int remotePeerLen = remotePeerLi.size();
		if (remotePeerLen > 0) {

			if (this.peerGotFile == 1) { // randomly choose preferred
				int tempcount = 0;
				while (tempcount < remotePeerLen)
					tempcount++;
				tempcount = 0;
				while (remotePeerLen > 0 && tempcount < Defaults.preferredNeighborsCount) {
					tempcount++;
					RemotePojo remotePeerData = null;
					try {
						int tempNum = localRandThreadNum(remotePeerLi.size());
						remotePeerData = remotePeerLi.get(tempNum);
						arbitratorMethod(5, remotePeerData);
					} catch (Exception ex) {
						// ex.printStackTrace();
					}

					BitSet remoteBitset = null;
					try {
						remoteBitset = remotePeerData.getremotePeerBitfield();
					}
					catch(Exception e) {
						//e.printStackTrace();
					}
					preferedTempMap.put(remotePeerData, remoteBitset);
					if (this.peersCurrentlyUnchoked.indexOf(remotePeerData) == -1)
						this.peersCurrentlyUnchoked.add(remotePeerData);
					this.peersCurrentlyChoked.remove(remotePeerData);
					remotePeerLi.remove(remotePeerData);
				}

				for (RemotePojo rp : remotePeerLi) {
					performChokeOperation("Test", rp);
					remotePeerLi.remove(0);
				}
				// remotePeerLi.clear();
			} else {
				Collections.sort(remotePeerLi);

				RemotePojo remotePeerData;

				int count = 0;

				while (!remotePeerLi.isEmpty() && count < Defaults.preferredNeighborsCount) {
					int z = 0;
					remotePeerData = remotePeerLi.get(z);
					arbitratorMethod(7, remotePeerData);
					BitSet remoteBitset = null;
					remoteBitset = remotePeerData.getremotePeerBitfield();
					preferedTempMap.put(remotePeerData, remoteBitset);
					modifyChoke_UnChoke(remotePeerData);
					count++;
					remotePeerLi.remove(0);
				}

				for (RemotePojo r : remotePeerLi) {
					performChokeOperation("Test", r);
				}
			}
		}

		this.likedNeighbours = preferedTempMap;

		if (likedNeighbours.size() > 0)
			peerProcess.log.logPreferredNeighbors(this.likedNeighbours);
	}

	private int localRandThreadNum(int a) {
		int b = 0;
		while (b < a) {
			b++;
		}
		return ThreadLocalRandom.current().nextInt(a);
	}

	private void arbitratorMethod(int x, RemotePojo remoteOne) {
		if (remoteOne != null) {
			if (remoteOne.getcurrState() == TypeOfMessage.choke) 
			{
				int count = 0;
				for (int i = 0; i < x; i++)
					count++;
				try {
					helperObj.sendwithoutPayloadMessages(remoteOne.oOS, 0, TypeOfMessage.unchoke);
					remoteOne.setCurrState(TypeOfMessage.unchoke);
					System.out.println("Message Unchoke sent to " + remoteOne.getidOfPeer() + " from the peer "
							+ Peer.getPeerObj().getIdOfPeer());
				} catch (Exception ex) {
					throw new RuntimeException("Unable to send unchoke message from the required peer class", ex);
				}
			}
		}
	}

	private void performChokeOperation(String dem, RemotePojo remoteOne) {
		try {
			helperObj.sendwithoutPayloadMessages(remoteOne.oOS, 0, TypeOfMessage.choke);
			remoteOne.setCurrState(TypeOfMessage.choke);
			System.out
					.println("choke sent to" + remoteOne.getidOfPeer() + "from" + Peer.getPeerObj().getIdOfPeer());
			if (this.peersCurrentlyChoked.indexOf(remoteOne) == -1)
				this.peersCurrentlyChoked.add(remoteOne);
			this.peersCurrentlyUnchoked.remove(remoteOne);
		} catch (Exception e) {
			// throw new RuntimeException(e);
		}
	}

	public boolean checkselfandNeighboringPeersGotFile(String text) {

		BitSet finalbitset = this.bitsetWanted;
		int connectedpeersize = 0;
		while (connectedpeersize < this.interLinkedPeers.size()) {
			connectedpeersize++;
		}
		if (Peer.getPeerObj().fetchSetofBits().equals(finalbitset)) {
			int count = 0;
			for (RemotePojo remotePojo : this.interLinkedPeers) {
				if (remotePojo.getremotePeerBitfield().equals(finalbitset))
					count++;// Kill the peer once all the connected and itself are having idealbitset.
			}
			return this.interLinkedPeers.size() != 0 && count == this.interLinkedPeers.size();
		} else
			return false;
	}
}