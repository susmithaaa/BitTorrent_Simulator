package com;

import java.io.*;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import com.FileProcessor.FileManagerExecutor;
import com.messages.Message;
import com.messages.MessageHandler;
import com.messages.MessageUtil;

public class PeerCommunicationHelper {

	public Message sendMessage(MessageType messageType, ObjectOutputStream out) throws Exception {
		MessageHandler messageHandler = new MessageHandler(messageType);
		// Message message = messageHandler.buildMessage();
		Message message = buildCoreMessage(messageHandler);
		/*out.writeObject(message);
		out.flush();*/
		writeObj(out, message);
		
		return message;
	}

	public Message sendBitSetMsg(ObjectOutputStream out) throws Exception {
		MessageHandler messageHandler = new MessageHandler(MessageType.bitfield,
				MessageUtil.toByteArray(Peer.getPeerInstance().getBitSet()));
		// Message message = messageHandler.buildMessage();
		Message message = buildCoreMessage(messageHandler);
		/*out.writeObject(message);
		out.flush();*/
		writeObj(out, message);
		return message;
	}

	public Message sendRequestMsg(ObjectOutputStream out, byte[] pieceIndex) throws Exception {
		MessageHandler messageHandler = new MessageHandler(MessageType.request, pieceIndex);
		// Message message = messageHandler.buildMessage();
		Message message = buildCoreMessage(messageHandler);
		/*out.writeObject(message);
		out.flush();*/
		writeObj(out, message);
		return message;
	}

	public Message sendHaveMsg(ObjectOutputStream out, int recentReceivedPieceIndex) throws Exception {
		MessageHandler messageHandler = new MessageHandler(MessageType.have,
				MessageUtil.intToByteArray(recentReceivedPieceIndex));
		// Message message = messageHandler.buildMessage();
		Message message = buildCoreMessage(messageHandler);
		/*out.writeObject(message);
		out.flush();*/
		writeObj(out, message);
		return message;
	}

	public Message sendPieceMsg(ObjectOutputStream out, int pieceIndex, FileManagerExecutor fileManagerExecutor)
			throws Exception {
		byte[] index = MessageUtil.intToByteArray(pieceIndex);
		byte[] payload = fileManagerExecutor.getFilePart(pieceIndex);
		byte[] payloadWithIndex = MessageUtil.concatenateByteArrays(index, payload);
		MessageHandler messageHandler = new MessageHandler(MessageType.piece, payloadWithIndex);
		// Message message = messageHandler.buildMessage();
		Message message = buildCoreMessage(messageHandler);
		/*out.writeObject(message);
		out.flush();*/
		writeObj(out, message);
		return message;
	}

	public Message getActualObjectMessage(ObjectInputStream in, RemotePeerInfo remote) throws ClassNotFoundException {
		try {
			Message received = (Message) in.readObject();
			logHelper(remote, received);
			// if (received == null) System.out.println("received null");
			// else System.out.println("object received");
			// System.out.println( remote.get_peerID());
			// System.out.println(received.toString());
			return received;
		} catch (IOException e) {
			// e.printStackTrace();
		} 
		return null;
	}
	
	
	void writeObj(ObjectOutputStream o, Message m) throws IOException
	{
		o.writeObject(m);
		o.flush();
	}
	
	Message buildCoreMessage(MessageHandler mh) throws Exception
	{
		return mh.buildMessage();
	}
	

	private static void logHelper(RemotePeerInfo remote, Message received) {
		switch (received.getTypeOfMessage()) {
		case 0: {
			peerProcess.log.choking(remote.get_peerID());
			break;
		}
		case 1: {
			peerProcess.log.unchoking(remote.get_peerID());
			break;
		}
		case 2: {
			peerProcess.log.interested(remote.get_peerID());
			break;
		}
		case 3: {
			peerProcess.log.notInterested(remote.get_peerID());
			break;
		}
		case 4: {
			peerProcess.log.have(remote.get_peerID(), MessageUtil.byteArrayToInt(received.getPayloadOfMessage()));
			break;
		}
		}
	}

	/*
	 * public static byte getMessageType(BufferedInputStream in) throws IOException{
	 * byte[] lengthBytePlusMsgType = new byte[5]; in.read(lengthBytePlusMsgType);
	 * return lengthBytePlusMsgType[4]; }
	 */

	public synchronized boolean isInterseted(BitSet remotePeer, BitSet currentPeer) {
		if (Peer.getPeerInstance()._hasFile == 1)
			return false;
		else
			return checkIfRemotePeerHasValidPiece(remotePeer, currentPeer);	// return false;
	}
	
	boolean checkIfRemotePeerHasValidPiece(BitSet r, BitSet c)
	{
	for (int i = 0; i < r.length(); i++) {
		if (r.get(i) && !c.get(i)) 
		//{
			// if (!b2.get(i))
				return true;
		// }
	}
	return false;
	}

	public synchronized int getPieceIndex(BitSet current, BitSet remote) {
		// below condition checks remote and current peer for either they both are empty
		// or they both have the same pieces
		if (checkEmptyOrSameRemoteandCurrentPeerBitset(remote, current))
			return -1;
		
		/*if (remote.equals(local))
			return -1;*/

		List<Integer> temp = new ArrayList<>();
		int len = remote.length();
		// for (int i = 0; i < remote.length(); i++) {
		int k = 0;
		while(k<len)
		{
			if (!current.get(k) && remote.get(k)) 
					temp.add(k);
			k++;
		}
		if (temp.size() == 0)
			return -1;

		int index = ThreadLocalRandom.current().nextInt(0, temp.size());
		return temp.get(index);
	}
	
	synchronized boolean checkEmptyOrSameRemoteandCurrentPeerBitset(BitSet r, BitSet c)
	{
		 return ((r.isEmpty() && c.isEmpty()) || r.equals(c));
		// no need to check if current is empty or not, coz we are looking for pieces 
		// which remote has and current does not
		// return (r.isEmpty() || r.equals(c));
	}
}
