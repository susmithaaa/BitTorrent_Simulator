package p2p.peer;

import java.io.*;
import java.util.BitSet;

import p2p.RemotePojo;
import p2p.MessageType;
import p2p.peerProcess;
import p2p.FileProcessor.FileProcessor;
import p2p.coreMessagePackage.MessageContent;
import p2p.coreMessagePackage.MessageHelper;
import p2p.coreMessagePackage.PeerHandshake;

import java.net.Socket;

import java.net.InetAddress;

public class PeerInteraction {

	PeerInteractionUtility helperforPeerCommunication;
	FileProcessor managerFile;
	RemotePojo remotePeerReference;
	Socket socketReference;
	PeerHandshake peerHandshake;
	public ObjectOutputStream outputStreamReference;
	boolean flag;
	boolean trackTermination = true;
	ObjectInputStream inputStreamReference;
	Long downloadStartTime;
	Long trackdownloadEndTime;

	public PeerInteraction(Socket sock, RemotePojo 	remotePojo) throws ClassNotFoundException {
		helperforPeerCommunication = new PeerInteractionUtility();
		if(sock == null) this.socketReference = null;
		else this.socketReference = sock;
		this.socketReference = sock;
		managerFile = new FileProcessor();
		this.remotePeerReference = remotePojo;
		try {
			socketInitialization();
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		}
	}

	private void socketInitialization() throws ClassNotFoundException {
		try {
			if (socketReference != null) {
				// do Nothing
			} else {
				InetAddress temp = InetAddress.getByName(this.remotePeerReference.getpeerHostName());
				int port_no = this.remotePeerReference.getRemotePeerPortNo();
				this.socketReference = new Socket(temp, port_no);
			}
			this.outputStreamReference = new ObjectOutputStream(this.socketReference.getOutputStream());
			this.remotePeerReference.oOS = this.outputStreamReference;
			this.inputStreamReference = new ObjectInputStream(this.socketReference.getInputStream());
			this.outputStreamReference.flush();
			this.peerHandshake = new PeerHandshake();
			this.peerHandshake.sendHandshakeMessageToPeer(this.peerHandshake, this.outputStreamReference);
			this.peerHandshake.receiveHandshakeFromPeer(this.inputStreamReference);
			Peer.getPeerObj().interLinkedPeers.add(this.remotePeerReference);
		} catch (IOException e) {
			throw new RuntimeException("Unable to open client socket", e);
		}

	}

	public void beginMessageInterchange() throws Exception {
		byte[] idxPiece = null;
		if (!Peer.getPeerObj().fetchSetofBits().isEmpty()) {
			helperforPeerCommunication.sendMessageBitSet(this.outputStreamReference, 0, 8);
		}
		while (trackTermination) {
			MessageContent mess = helperforPeerCommunication.getMessageObjectDefault(this.remotePeerReference,
					this.inputStreamReference);
			byte[] msgPayloadReceived = mess.getPayloadOfMessage();
			byte msgType = mess.typeOfMessage;
			int _constant7 = 7;
			int _constant4 = 4;

			if (msgType == (byte) _constant4 || msgType == (byte) _constant7) {
				idxPiece = new byte[4];
				int j = 0;
				int temp = 4;
				while(j<temp)
				{
					idxPiece[j] = msgPayloadReceived[j];
					j++;
				}
			}
			
			if (msgType != (byte) _constant7 && this.flag) {
				this.downloadStartTime = 0L;
			}
			RemotePojo r = this.remotePeerReference;
			Peer refPeer = Peer.getPeerObj();
			switch (msgType) {
			case (byte) 0: {
				break;
			}
			case (byte) 1: {
				handleUnchoke(r, refPeer);
				break;
			}

			case (byte) 2: {
				
				int rem_id = r.getidOfPeer();
				refPeer.willingPeers.putIfAbsent(rem_id, r);
				break;
			}

			case (byte) 3: {
				int id= r.getidOfPeer();
				refPeer.willingPeers.remove(id);
				break;
			}

			case (byte) 4: {
				int convertedVal = MessageHelper.convByteArrayInt(msgPayloadReceived);
				r.getremotePeerBitfield().set(convertedVal);
				if (!refPeer.fetchSetofBits().get(convertedVal)) {
					helperforPeerCommunication.sendwithoutPayloadMessages(this.outputStreamReference, 0, MessageType.interested);
				}
				break;
			}

			case (byte) 5: {
				handleBitsetMessage(msgPayloadReceived, r, refPeer);
				break;
			}

			case (byte) 6: {
				helperforPeerCommunication.sendMessagePiece(this.outputStreamReference, MessageType.piece,
						MessageHelper.convByteArrayInt(msgPayloadReceived), 5, this.managerFile);
				break;
			}

			case (byte) 7: {
				handlePieceMessage(idxPiece, mess, r, refPeer, 0);
				break;
			}
			}
			checkCompleteDownload(r, refPeer, 1);
		}
		return;
	}

	void handleUnchoke(RemotePojo r, Peer ref) throws Exception {
		System.out.println("Peer " + r.getidOfPeer() + " Unchoked by "
				+ ref.getIdOfPeer());
		int pidx = helperforPeerCommunication.extractIndexofPiece(ref.fetchSetofBits(),
				r.getremotePeerBitfield(), -1);
		if (pidx != -1) {
			helperforPeerCommunication.sendMessageRequest(this.outputStreamReference, 0, MessageHelper.convIntByteArray(pidx));
			this.downloadStartTime = System.nanoTime();
			this.flag = true;
		}
		if (pidx != -1) {
		}
		else
		{
			helperforPeerCommunication.sendwithoutPayloadMessages(this.outputStreamReference, 0, MessageType.notinterested);
		}
	}

	void handleBitsetMessage(byte[] payload, RemotePojo r, Peer pRef) throws Exception {
		BitSet bs = MessageHelper.covertByteToBitset(payload, 8);
		r.setremotePeerBitfield(bs);
		boolean res = helperforPeerCommunication.checkIfInterested(r.getremotePeerBitfield(),
				pRef.fetchSetofBits(), 0);
		if (!res) {
			helperforPeerCommunication.sendwithoutPayloadMessages(this.outputStreamReference, 0, MessageType.notinterested);
		} else {
			helperforPeerCommunication.sendwithoutPayloadMessages(this.outputStreamReference, 0, MessageType.interested);
		}
	}

	void checkCompleteDownload(RemotePojo r, Peer pref, int i) throws IOException {
		if (pref.peerGotFile != i
				&& pref.fetchSetofBits().equals(pref.bitsetWanted)) {
			if (r.getremotePeerBitfield().equals(pref.bitsetWanted)) {
				System.out.println("Files combined by remote thread - " + r.getidOfPeer());
				managerFile.combineFile();
				pref.setPeerGotFile(i);
				peerProcess.log.logDownloadCompleted();
				trackTermination = false;
				System.out.println("Set to false cond 1");
			}
		}

		if (pref.peerGotFile == i
				&& r.getremotePeerBitfield().equals(pref.bitsetWanted)) {
			trackTermination = false;
			// if(Defaults.check_exit_id == pref.getIdOfPeer())
				
				System.out.println("Set to false cond 2");
		}
	}

	void handlePieceMessage(byte[] piIdx, MessageContent mess, RemotePojo r, Peer refP, int con) throws Exception {
		if (!refP.fetchSetofBits().get(MessageHelper.convByteArrayInt(piIdx))) { 
			managerFile.receivePartOfFile(mess, MessageHelper.convByteArrayInt(piIdx));
			refP.fetchSetofBits().set(MessageHelper.convByteArrayInt(piIdx));
			peerProcess.log.logPieceDownloaded(r.getidOfPeer(),
					MessageHelper.convByteArrayInt(piIdx), refP.fetchSetofBits().cardinality());
			if (this.downloadStartTime != 0L && con == 0) {
				this.trackdownloadEndTime = System.nanoTime();
				r.setConsumptionRate(MessageHelper.convByteArrayInt(mess.lengthOfMessage)
						/ (int) (this.trackdownloadEndTime - this.downloadStartTime));
			}
			refP.publishHavePieceToOtherPeers(MessageHelper.convByteArrayInt(piIdx), con);
		}
		int IdxPiece = helperforPeerCommunication.extractIndexofPiece(refP.fetchSetofBits(),
				r.getremotePeerBitfield(), -1);
		;
		helperforPeerCommunication.sendMessageRequest(this.outputStreamReference, con, MessageHelper.convIntByteArray(IdxPiece));
		this.flag = true;
		this.downloadStartTime = System.nanoTime();
	}
}
