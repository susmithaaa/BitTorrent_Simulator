package p2p.peer;

import java.io.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import p2p.RemotePojo;
import p2p.TypeOfMessage;
import p2p.peerProcess;
import p2p.FileProcessor.FileProcessor;
import p2p.coreMessagePackage.MessageContent;
import p2p.coreMessagePackage.MessageHelper;


public class PeerInteractionUtility {

	public MessageContent sendwithoutPayloadMessages(ObjectOutputStream objOut, int i, TypeOfMessage msgType) throws Exception {
		MessageContent mess = null;
		if(msgType == TypeOfMessage.choke)
		{
			mess = new MessageContent((byte) 0, null);
		}
		else if(msgType == TypeOfMessage.unchoke)
		{
			mess = new MessageContent((byte) 1, null);
		}
		else if(msgType == TypeOfMessage.interested)
		{
			mess = new MessageContent((byte) 2, null);
		}
		else if(msgType == TypeOfMessage.notinterested)
		{
			mess = new MessageContent((byte) 3, null);
		}
		else
		{
			System.out.println("MessageType invalid");
		}
			
		writeObj(objOut, mess);

		return mess;
	}

	public MessageContent sendMessageBitSet(ObjectOutputStream objOut, int i, int len) throws Exception {
		MessageContent mess = new MessageContent((byte) 5, MessageHelper.convertToByteArray(Peer.getPeerObj().fetchSetofBits(), len));
		writeObj(objOut, mess);
		return mess;
	}

	public MessageContent sendMessageRequest(ObjectOutputStream objOut, int i, byte[] arrayOfPieceIndex) throws Exception {
		MessageContent mess = new MessageContent((byte) 6, arrayOfPieceIndex);
		writeObj(objOut, mess);
		return mess;
	}

	public MessageContent sendMessageHave(ObjectOutputStream objOut, int i, int pieceId) throws Exception {
				MessageContent mess = new MessageContent((byte) 4,  MessageHelper.convIntByteArray(pieceId));
		
		objOut.writeObject(mess); objOut.flush();
		 
		writeObj(objOut, mess);
		return mess;
	}

	public MessageContent sendMessagePiece(ObjectOutputStream objOut, TypeOfMessage msgType,int pieceId, int x, FileProcessor filemanagerObj)
			throws Exception {
		byte[] payload = MessageHelper.combineByteArrays(MessageHelper.convIntByteArray(pieceId), filemanagerObj.getFilePart(pieceId));
		MessageContent mess = new MessageContent((byte) 7, payload); 
		writeObj(objOut, mess);
		return mess;
	}

	public MessageContent getMessageObjectDefault(RemotePojo rp, ObjectInputStream objIn) throws ClassNotFoundException {
		try {
			MessageContent received = (MessageContent) objIn.readObject();
			assisterForLogging(rp, received);
			return received;
		} catch (IOException e) {
			// e.printStackTrace();
		}
		return null;
	}

	void writeObj(ObjectOutputStream o, MessageContent m) throws IOException {
		o.writeObject(m);
		o.flush();
	}

	private static void assisterForLogging(RemotePojo rp, MessageContent msg) {
		byte b = msg.typeOfMessage;
		if(b == 0) {
			peerProcess.log.logChoked(rp.getidOfPeer());
		}
		else if(b == 1) {
			peerProcess.log.logUnchoked(rp.getidOfPeer());
		}
		else if(b == 2) {
			peerProcess.log.logInterested(rp.getidOfPeer());
		}
		else if(b == 3) {
			peerProcess.log.logNotInterested(rp.getidOfPeer());
		}
		else if(b == 4) {
			peerProcess.log.logHave(rp.getidOfPeer(), MessageHelper.convByteArrayInt(msg.getPayloadOfMessage()));
		}
	}


	public synchronized boolean checkIfInterested(BitSet remotePeer, BitSet currentPeer, int j) {
		if (Peer.getPeerObj().peerGotFile == 1)
			return false;
		else
			return checkIfRemotePeerHasValidPiece(remotePeer, currentPeer, j); // return false;
	}

	boolean checkIfRemotePeerHasValidPiece(BitSet r, BitSet c, int j) {
		for (int i = j; i < r.length(); i++) {
			if (r.get(i) && !c.get(i))
				return true;
		}
		return false;
	}

	public synchronized int extractIndexofPiece(BitSet current, BitSet remote, int tempreturn) {
		// below condition checks remote and current peer for either they both are empty
		// or they both have the same pieces
		if (checkEmptyOrSameRemoteandCurrentPeerBitset(remote, current)) return tempreturn;

		List<Integer> intList = new ArrayList<>();
		int len = remote.length();
		// for (int i = 0; i < remote.length(); i++) {
		int k = 0;
		while (k < len) {
			if (!current.get(k) && remote.get(k))
				intList.add(k);
			k++;
		}
		int i=0;
		while(i<intList.size()) {
			k--;
			i++;
		}
		if (intList.size() == 0)
			return tempreturn;

		return intList.get(ThreadLocalRandom.current().nextInt(0, intList.size()));
	}

	synchronized boolean checkEmptyOrSameRemoteandCurrentPeerBitset(BitSet r, BitSet c) {
		return (r.isEmpty() || r.equals(c));
		// no need to check if current is empty or not, coz we are looking for pieces
	}
}
