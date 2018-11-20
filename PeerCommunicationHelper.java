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
	
	public Message sendMessage(ObjectOutputStream out,MessageType messageType) throws Exception{
		MessageHandler messageHandler = new MessageHandler(messageType);
		Message message = messageHandler.buildMessage();
		out.writeObject(message);
		out.flush();
		return message;
	}
	
	public Message sendBitSetMsg(ObjectOutputStream out) throws Exception{
		MessageHandler messageHandler = new MessageHandler(MessageType.bitfield, MessageUtil.toByteArray(Peer.getPeerInstance().getBitSet()));
		Message message = messageHandler.buildMessage();
		out.writeObject(message);
		out.flush();
		return message;
	}

	public Message sendRequestMsg(ObjectOutputStream out, byte[] pieceIndex) throws Exception{
		MessageHandler messageHandler = new MessageHandler(MessageType.request,pieceIndex);
		Message message = messageHandler.buildMessage();
		out.writeObject(message);
		out.flush();
		return message;
	}

	public Message sendHaveMsg(ObjectOutputStream out, int recentReceivedPieceIndex) throws Exception{
		MessageHandler messageHandler = new MessageHandler(MessageType.have,MessageUtil.intToByteArray(recentReceivedPieceIndex));
		Message message = messageHandler.buildMessage();
		out.writeObject(message);
		out.flush();
		return message;
	}
	
	public Message sendPieceMsg(ObjectOutputStream out, int pieceIndex, FileManagerExecutor fileManagerExecutor) throws Exception{
		byte[] index = MessageUtil.intToByteArray(pieceIndex);
		byte[] payload = fileManagerExecutor.getFilePart(pieceIndex);
		byte[] payloadWithIndex = MessageUtil.concatenateByteArrays(index, payload);
		MessageHandler messageHandler = new MessageHandler(MessageType.piece,payloadWithIndex );
		Message message = messageHandler.buildMessage();
		out.writeObject(message);
		out.flush();
		return message;
	}

	public Message getActualObjectMessage(ObjectInputStream in, RemotePeerInfo remote) {
		try {
			Message received = (Message) in.readObject();
			logHelper(received, remote);
//			if (received == null) System.out.println("received null");
//			else System.out.println("object received");
//			System.out.println( remote.get_peerID());
//			System.out.println(received.toString());
			return received;
		} catch (IOException e) {
//			e.printStackTrace();
		} catch (ClassNotFoundException e) {
//			e.printStackTrace();
		}
		return null;
	}

    private static void logHelper(Message received, RemotePeerInfo remote) {
	    switch (received.getTypeOfMessage()){
            case 0:{
                peerProcess.log.choking(remote.get_peerID());
                break;
            }
            case 1:{
                peerProcess.log.unchoking(remote.get_peerID());
                break;
            }
            case 2:{
                peerProcess.log.interested(remote.get_peerID());
                break;
            }
            case 3:{
                peerProcess.log.notInterested(remote.get_peerID());
                break;
            }
            case 4:{
                peerProcess.log.have(remote.get_peerID(), MessageUtil.byteArrayToInt(received.getPayloadOfMessage()));
                break;
            }
        }
    }
    
    /*public static  byte getMessageType(BufferedInputStream in) throws IOException{
    	byte[] lengthBytePlusMsgType = new byte[5];
    	in.read(lengthBytePlusMsgType);
    	return lengthBytePlusMsgType[4];
    }*/

	public synchronized boolean isInterseted(BitSet b1, BitSet b2){
		if(Peer.getPeerInstance()._hasFile == 1) return false;
		for(int i=0;i<b1.length();i++){
			if(b1.get(i)){
				if(!b2.get(i)) return true;
			}
		}
		return false;
	}
    
    public synchronized int getPieceIndex(BitSet remote,BitSet local){
    	if(remote.isEmpty() && local.isEmpty()){
            return -1;
        }
         if (remote.equals(local)) return -1;

        List<Integer> temp = new ArrayList<>(); 
        for(int i=0; i < remote.length(); i++)
        {
        	if(!local.get(i))
        	{
        		if(remote.get(i))
        		temp.add(i);
        	}     	
        }
        if(temp.size() == 0) return -1;

        int index = ThreadLocalRandom.current().nextInt(0, temp.size());
        return temp.get(index);
    }
}
