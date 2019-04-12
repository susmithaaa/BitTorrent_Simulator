package p2p;

import java.util.BitSet;

import p2p.peer.Peer;

import java.io.ObjectOutputStream;

public class RemotePojo implements Comparable<RemotePojo> {
	private String peerHostName;
    private MessageType currState;
	private int idOfPeer;
	public ObjectOutputStream oOS;
	private BitSet remotePeerBitfield;
	private int remotePeerPortNo;
	private int remotePeerHasFile;
	private long consumptionRate;


	public MessageType getcurrState() {
		return currState;
	}

	public void setCurrState(MessageType state) {
		this.currState = state;
	}

	public void setConsumptionRate(long download_rate) {
		this.consumptionRate = download_rate;
	}

	public BitSet getremotePeerBitfield() {
		return remotePeerBitfield;
	}

	public void setremotePeerBitfield(BitSet bf) {
		this.remotePeerBitfield = bf;
	}

	public int getidOfPeer() {
		return idOfPeer;
	}

	public void setIdOfPeer(int pId) {
		this.idOfPeer = pId;
	}
	
	public void setRemotePeerPortNo(int pNo) {
		this.remotePeerPortNo = pNo;
	}

	public int getRemotePeerHasFile() {
		return remotePeerHasFile;
	}

	public void setRemotePeerHasFile(int hf) {
		this.remotePeerHasFile = hf;
	}

	public String getpeerHostName() {
		return peerHostName;
	}

	public void setPeerhostName(String hN) {
		this.peerHostName = hN;
	}

	public int getRemotePeerPortNo() {
		return remotePeerPortNo;
	}
	
	@Override
	public int compareTo(RemotePojo o) {
		long x = this.consumptionRate - o.consumptionRate;
		return Math.toIntExact(x);
	}

	public RemotePojo(int _peerID, String _hostName, int _portNo, int _hasFile, int k, int l) {
		this.peerHostName = _hostName;
		this.remotePeerPortNo = _portNo;
		this.remotePeerHasFile = _hasFile;
		MessageType msg = MessageType.choke;
		this.idOfPeer = _peerID;
		this.remotePeerBitfield = new BitSet(Peer.getPeerObj().peerCountPieces);
		this.currState = msg;
		this.oOS = null;

		if (this.getRemotePeerHasFile() == l) {
			int i = 0;
			int len = this.remotePeerBitfield.size();
			while(i<len)
			{
				this.remotePeerBitfield.set(i);
				i++;
			}
		}
	}
}