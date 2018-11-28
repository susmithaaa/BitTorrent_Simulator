package com;

import com.FileProcessor.FileManagerExecutor;
import com.messages.Handshake;
import com.messages.Message;
import com.messages.MessageUtil;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.BitSet;

public class PeerCommunication {

	PeerCommunicationHelper peerCommunicationHelper;
	FileManagerExecutor fileProcessor;
	RemotePeerInfo remotePeerReference;
	Socket socketReference;
	Handshake handshake;
	public ObjectOutputStream outputStreamReference;
	boolean flag;
	boolean trackTermination = true;
	ObjectInputStream inputStreamReference;
	// int latestPieceReceived;
	Long downloadStartTime;
	Long trackdownloadEndTime;

	public PeerCommunication(RemotePeerInfo remotePeerInfo) {
		fileProcessor = new FileManagerExecutor();
		this.remotePeerReference = remotePeerInfo;
		peerCommunicationHelper = new PeerCommunicationHelper();
		this.socketReference = null;
		try {
			initializeSocket();
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		}
	}

	public PeerCommunication(RemotePeerInfo remotePeerInfo, Socket socket) throws ClassNotFoundException {
		peerCommunicationHelper = new PeerCommunicationHelper();
		this.socketReference = socket;
		fileProcessor = new FileManagerExecutor();
		this.remotePeerReference = remotePeerInfo;
		try {
			initializeSocket();
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		}
	}

	private void initializeSocket() throws ClassNotFoundException {
		try {
			if (socketReference != null) {

			} else {
				InetAddress temp = InetAddress.getByName(this.remotePeerReference.get_hostName());
				int port_no = this.remotePeerReference.get_portNo();
				this.socketReference = new Socket(temp, port_no);
			}
			this.outputStreamReference = new ObjectOutputStream(this.socketReference.getOutputStream());
			this.remotePeerReference.objectOutputStream = this.outputStreamReference;
			this.inputStreamReference = new ObjectInputStream(this.socketReference.getInputStream());
			this.outputStreamReference.flush();
			int peer_id = Peer.getPeerInstance().get_peerID();
			this.handshake = new Handshake(peer_id);
			// Peer.getPeerInstance().handShakeCount++;
			this.handshake.sendHandshakeMessageToPeer(this.handshake, this.outputStreamReference);
			this.handshake.receiveHandshakeFromPeer(this.inputStreamReference);
			Peer.getPeerInstance().connectedPeers.add(this.remotePeerReference);
		} catch (IOException e) {
			throw new RuntimeException("Could not open client socket", e);
		}

	}

	public void startExchangingMessages() throws Exception {
		// System.out.println(Peer.getPeerInstance().peersInterested.size());
		byte[] pieceIndexField = null;
		if (!Peer.getPeerInstance().getBitSet().isEmpty()) {
			peerCommunicationHelper.sendBitSetMsg(this.outputStreamReference);
		}
		while (trackTermination) {
			Message message = peerCommunicationHelper.getActualObjectMessage(this.inputStreamReference,
					this.remotePeerReference);
			byte msgType = message.getTypeOfMessage();
			byte[] msgPayloadReceived = message.getPayloadOfMessage();
			// byte[] msgLength = message.getLengthOfMessage();

			if (this.flag && msgType != (byte) 7) {
				this.downloadStartTime = 0L;
			}

			if (msgType == (byte) 7 || msgType == (byte) 4) {
				pieceIndexField = new byte[4];
				for (int i = 0; i < 4; i++) {
					pieceIndexField[i] = msgPayloadReceived[i];
				}
			}

			switch (msgType) {
			case (byte) 0: {
				break;
			}
			case (byte) 1: {
				/*
				 * System.out.println("Unchoke received from " +
				 * this.remotePeerReference.get_peerID() + " to " +
				 * Peer.getPeerInstance().get_peerID()); int pieceIndex =
				 * peerCommunicationHelper.getPieceIndex(this.remotePeerReference.getBitfield(),
				 * Peer.getPeerInstance().getBitSet()); if (pieceIndex != -1) {
				 * peerCommunicationHelper.sendRequestMsg(this.outputStreamReference,
				 * MessageUtil.intToByteArray(pieceIndex)); this.downloadStartTime =
				 * System.nanoTime(); this.flag = true; } if (pieceIndex == -1) {
				 * peerCommunicationHelper.sendMessage(this.outputStreamReference,
				 * MessageType.notinterested); }
				 */
				handleUnchoke();
				break;
			}

			case (byte) 2: {
				Peer.getPeerInstance().peersInterested.putIfAbsent(this.remotePeerReference.get_peerID(),
						this.remotePeerReference);
				break;
			}

			case (byte) 3: {
				// if (this.remote.getBitfield().equals(Peer.getPeerInstance().idealBitset)) {
				// terminateFlag = false;
				// }
				Peer.getPeerInstance().peersInterested.remove(this.remotePeerReference.get_peerID());
				break;
			}

			case (byte) 4: {
				this.remotePeerReference.getBitfield().set(MessageUtil.byteArrayToInt(msgPayloadReceived));
				if (!Peer.getPeerInstance().getBitSet().get(MessageUtil.byteArrayToInt(msgPayloadReceived))) {
					peerCommunicationHelper.sendMessage(this.outputStreamReference, MessageType.interested);
					// PeerCommunicationHelper.sendRequestMsg(this.out,msgPayloadReceived);
				}
				break;
			}

			case (byte) 5: {
				/*
				 * BitSet bitset = MessageUtil.fromByteArray(msgPayloadReceived);
				 * this.remotePeerReference.setBitfield(bitset); if
				 * (peerCommunicationHelper.isInterseted(this.remotePeerReference.getBitfield(),
				 * Peer.getPeerInstance().getBitSet())) {
				 * peerCommunicationHelper.sendMessage(this.outputStreamReference,
				 * MessageType.interested); // PeerCommunicationHelper.sendRequestMsg(this.out,
				 * this.remote); } else {
				 * peerCommunicationHelper.sendMessage(this.outputStreamReference,
				 * MessageType.notinterested); }
				 */
				handleBitsetMessage(msgPayloadReceived);
				break;
			}

			case (byte) 6: {
				peerCommunicationHelper.sendPieceMsg(this.outputStreamReference,
						MessageUtil.byteArrayToInt(msgPayloadReceived), this.fileProcessor);
				break;
			}

			case (byte) 7: {
				/*
				 * if (!Peer.getPeerInstance().getBitSet().get(MessageUtil.byteArrayToInt(
				 * pieceIndexField))) { int numberOfPieces =
				 * Peer.getPeerInstance().getBitSet().cardinality();
				 * fileProcessor.acceptFilePart(MessageUtil.byteArrayToInt(pieceIndexField),
				 * message); Peer.getPeerInstance().getBitSet().set(MessageUtil.byteArrayToInt(
				 * pieceIndexField));
				 * peerProcess.log.downloadAPiece(this.remotePeerReference.get_peerID(),
				 * MessageUtil.byteArrayToInt(pieceIndexField), numberOfPieces); if
				 * (this.downloadStartTime != 0L) { this.trackdownloadEndTime =
				 * System.nanoTime();
				 * this.remotePeerReference.setDownload_rate(MessageUtil.byteArrayToInt(
				 * msgLength) / (int) (this.trackdownloadEndTime - this.downloadStartTime)); }
				 * Peer.getPeerInstance().sendHaveToAll(MessageUtil.byteArrayToInt(
				 * pieceIndexField)); } int pieceIndex =
				 * peerCommunicationHelper.getPieceIndex(this.remotePeerReference.getBitfield(),
				 * Peer.getPeerInstance().getBitSet()); ;
				 * peerCommunicationHelper.sendRequestMsg(this.outputStreamReference,
				 * MessageUtil.intToByteArray(pieceIndex)); this.downloadStartTime =
				 * System.nanoTime(); this.flag = true;
				 */
				handlePieceMessage(pieceIndexField, message);
				break;
			}
			}

			/*
			 * if (Peer.getPeerInstance().get_hasFile() != 1 &&
			 * Peer.getPeerInstance().getBitSet().equals(Peer.getPeerInstance().idealBitset)
			 * ) { if (this.remotePeerReference.getBitfield().equals(Peer.getPeerInstance().
			 * idealBitset)) { System.out.println("files merged by thread : " +
			 * this.remotePeerReference.get_peerID()); fileProcessor.filesmerge();
			 * Peer.getPeerInstance().set_hasFile(1);
			 * peerProcess.log.completionOfDownload(); trackTermination = false; } }
			 * 
			 * if (Peer.getPeerInstance().get_hasFile() == 1 &&
			 * this.remotePeerReference.getBitfield().equals(Peer.getPeerInstance().
			 * idealBitset)) { trackTermination = false; }
			 */

			checkCompleteDownload();
		}
	}

	void handleUnchoke() throws Exception {
		System.out.println("Unchoke received from " + this.remotePeerReference.get_peerID() + " to "
				+ Peer.getPeerInstance().get_peerID());
		int pieceIndex = peerCommunicationHelper.getPieceIndex(Peer.getPeerInstance().getBitSet(),
				this.remotePeerReference.getBitfield());
		if (pieceIndex != -1) {
			peerCommunicationHelper.sendRequestMsg(this.outputStreamReference, MessageUtil.intToByteArray(pieceIndex));
			this.downloadStartTime = System.nanoTime();
			this.flag = true;
		}
		if (pieceIndex == -1) {
			peerCommunicationHelper.sendMessage(this.outputStreamReference, MessageType.notinterested);
		}
	}

	void handleBitsetMessage(byte[] payload) throws Exception {
		BitSet bitset = MessageUtil.fromByteArray(payload);
		this.remotePeerReference.setBitfield(bitset);
		if (peerCommunicationHelper.isInterseted(this.remotePeerReference.getBitfield(),
				Peer.getPeerInstance().getBitSet())) {
			peerCommunicationHelper.sendMessage(this.outputStreamReference, MessageType.interested);
			// PeerCommunicationHelper.sendRequestMsg(this.out, this.remote);
		} else {
			peerCommunicationHelper.sendMessage(this.outputStreamReference, MessageType.notinterested);
		}
	}

	void checkCompleteDownload() throws IOException {
		if (Peer.getPeerInstance().get_hasFile() != 1
				&& Peer.getPeerInstance().getBitSet().equals(Peer.getPeerInstance().idealBitset)) {
			if (this.remotePeerReference.getBitfield().equals(Peer.getPeerInstance().idealBitset)) {
				System.out.println("Files merged by thread : " + this.remotePeerReference.get_peerID());
				fileProcessor.filesmerge();
				Peer.getPeerInstance().set_hasFile(1);
				// peerProcess.check_exit();
				peerProcess.log.completionOfDownload();
				trackTermination = false;
			}
		}

		if (Peer.getPeerInstance().get_hasFile() == 1
				&& this.remotePeerReference.getBitfield().equals(Peer.getPeerInstance().idealBitset)) {
			trackTermination = false;
			// System.exit(0);
		}
	}

	void handlePieceMessage(byte[] pieceIndexField, Message message) throws Exception {
		if (!Peer.getPeerInstance().getBitSet().get(MessageUtil.byteArrayToInt(pieceIndexField))) {
			int numberOfPieces = Peer.getPeerInstance().getBitSet().cardinality();
			fileProcessor.acceptFilePart(MessageUtil.byteArrayToInt(pieceIndexField), message);
			Peer.getPeerInstance().getBitSet().set(MessageUtil.byteArrayToInt(pieceIndexField));
			peerProcess.log.downloadAPiece(this.remotePeerReference.get_peerID(),
					MessageUtil.byteArrayToInt(pieceIndexField), numberOfPieces);
			if (this.downloadStartTime != 0L) {
				this.trackdownloadEndTime = System.nanoTime();
				this.remotePeerReference.setDownload_rate(MessageUtil.byteArrayToInt(message.getLengthOfMessage())
						/ (int) (this.trackdownloadEndTime - this.downloadStartTime));
			}
			Peer.getPeerInstance().sendHaveToAll(MessageUtil.byteArrayToInt(pieceIndexField));
		}
		int pieceIndex = peerCommunicationHelper.getPieceIndex(Peer.getPeerInstance().getBitSet(),
				this.remotePeerReference.getBitfield());
		;
		peerCommunicationHelper.sendRequestMsg(this.outputStreamReference, MessageUtil.intToByteArray(pieceIndex));
		this.downloadStartTime = System.nanoTime();
		this.flag = true;
	}
}
