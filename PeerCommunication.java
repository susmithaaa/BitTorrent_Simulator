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
    public ObjectOutputStream out;
    RemotePeerInfo remote;
    Socket socket;
    Handshake handshake;
    ObjectInputStream in;
    int recentReceievdPiece;
    Long downloadStart;
    Long downloadEnd;
    boolean flag;
    boolean terminateFlag = true;
    PeerCommunicationHelper peerCommunicationHelper;
    FileManagerExecutor fileManagerExecutor;

    public PeerCommunication(RemotePeerInfo remotePeerInfo) throws ClassNotFoundException {
        peerCommunicationHelper = new PeerCommunicationHelper();
        fileManagerExecutor = new FileManagerExecutor();
        this.remote = remotePeerInfo;
        this.socket = null;
        initSocket();
    }

    public PeerCommunication(RemotePeerInfo remotePeerInfo, Socket socket) throws ClassNotFoundException {
        peerCommunicationHelper = new PeerCommunicationHelper();
        fileManagerExecutor = new FileManagerExecutor();
        this.remote = remotePeerInfo;
        this.socket = socket;
        initSocket();
    }

    private void initSocket() throws ClassNotFoundException {
        try {
            if (socket == null) {
                this.socket = new Socket(InetAddress.getByName(this.remote.get_hostName()), this.remote.get_portNo());
            }
            this.out = new ObjectOutputStream(this.socket.getOutputStream());
            this.in = new ObjectInputStream(this.socket.getInputStream());
            this.remote.objectOutputStream = this.out;
            this.out.flush();

            this.handshake = new Handshake(Peer.getPeerInstance().get_peerID());
//            Peer.getPeerInstance().handShakeCount++;
            this.handshake.sendHandshakeMessageToPeer(this.handshake, this.out);
            this.handshake.receiveHandshakeFromPeer(this.in);
            Peer.getPeerInstance().connectedPeers.add(this.remote);
        } catch (IOException e) {
            throw new RuntimeException("Could not open client socket", e);
        }

    }

    public void startMessageExchange() throws Exception {
//		System.out.println(Peer.getPeerInstance().peersInterested.size());
        byte[] pieceIndexField = null;
        if (!Peer.getPeerInstance().getBitSet().isEmpty()) {
            peerCommunicationHelper.sendBitSetMsg(this.out);
        }
        while (terminateFlag) {
            Message message = peerCommunicationHelper.getActualObjectMessage(this.in, this.remote);
            byte msgType = message.getTypeOfMessage();
            byte[] msgPayloadReceived = message.getPayloadOfMessage();
            byte[] msgLength = message.getLengthOfMessage();

            if (this.flag && msgType != (byte) 7) {
                this.downloadStart = 0L;
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
                    System.out.println("Unchoke received from " + this.remote.get_peerID() + " to " + Peer.getPeerInstance().get_peerID());
                    int pieceIndex = peerCommunicationHelper.getPieceIndex(this.remote.getBitfield(),Peer.getPeerInstance().getBitSet());
                    if (pieceIndex != -1) {
                        peerCommunicationHelper.sendRequestMsg(this.out, MessageUtil.intToByteArray(pieceIndex));
                        this.downloadStart = System.nanoTime();
                        this.flag = true;
                    }
				if (pieceIndex == -1) {
					peerCommunicationHelper.sendMessage(this.out, MessageType.notinterested);
				}
                    break;
                }

                case (byte) 2: {
                    Peer.getPeerInstance().peersInterested.putIfAbsent(this.remote.get_peerID(), this.remote);
                    break;
                }

                case (byte) 3: {
//                    if (this.remote.getBitfield().equals(Peer.getPeerInstance().idealBitset)) {
//                        terminateFlag = false;
//                    }
                    Peer.getPeerInstance().peersInterested.remove(this.remote.get_peerID());
                    break;
                }

                case (byte) 4: {
                        this.remote.getBitfield().set(MessageUtil.byteArrayToInt(msgPayloadReceived));
                    
//				if (!Peer.getPeerInstance().getBitSet().get(MessageUtil.byteArrayToInt(msgPayloadReceived))) {
//					if (Peer.getPeerInstance().preferredNeighbours.containsKey(this.remote)
//							|| Peer.getPeerInstance().getOptimisticallyUnchokedNeighbour() == this.remote)
//						PeerCommunicationHelper.sendRequestWhenHave(this.out, msgPayloadReceived);
//				}

                    /*if (!Peer.getPeerInstance().getBitSet().get(MessageUtil.byteArrayToInt(msgPayloadReceived))) {
                        PeerCommunicationHelper.sendMessage(this.out, MessageType.interested);
					PeerCommunicationHelper.sendRequestMsg(this.out,msgPayloadReceived);
*/
//						if (Peer.getPeerInstance().preferredNeighbours.containsKey(this.remote)
//								|| Peer.getPeerInstance().getOptimisticallyUnchokedNeighbour() == this.remote)
//							PeerCommunicationHelper.sendRequestWhenHave(this.out, msgPayloadReceived);
//						this.downloadStart = System.nanoTime();
//						this.flag = true;
                        if (!Peer.getPeerInstance().getBitSet().get(MessageUtil.byteArrayToInt(msgPayloadReceived))) {
                            peerCommunicationHelper.sendMessage(this.out, MessageType.interested);
//        					PeerCommunicationHelper.sendRequestMsg(this.out,msgPayloadReceived);
                        }
                    /* else {
                        PeerCommunicationHelper.sendMessage(this.out, MessageType.notinterested);
                    }*/
                    break;
                }

                case (byte) 5: {
                    BitSet bitset = MessageUtil.fromByteArray(msgPayloadReceived);
                    this.remote.setBitfield(bitset);
                    if (peerCommunicationHelper.isInterseted(this.remote.getBitfield(), Peer.getPeerInstance().getBitSet())) {
                        peerCommunicationHelper.sendMessage(this.out, MessageType.interested);
                        //PeerCommunicationHelper.sendRequestMsg(this.out, this.remote);
                    } else {
                        peerCommunicationHelper.sendMessage(this.out, MessageType.notinterested);
                    }
                    break;
                }

                case (byte) 6: {
                   /* if (Peer.getPeerInstance().preferredNeighbours.containsKey(this.remote)
                            || Peer.getPeerInstance().getOptimisticallyUnchokedNeighbour().equals(this.remote))*/
                        peerCommunicationHelper.sendPieceMsg(this.out, MessageUtil.byteArrayToInt(msgPayloadReceived), this.fileManagerExecutor);
                    break;
                }

                case (byte) 7: {
                    if (!Peer.getPeerInstance().getBitSet().get(MessageUtil.byteArrayToInt(pieceIndexField))) {
                        int numberOfPieces = Peer.getPeerInstance().getBitSet().cardinality();
                        fileManagerExecutor.acceptFilePart(MessageUtil.byteArrayToInt(pieceIndexField), message);
                        Peer.getPeerInstance().getBitSet().set(MessageUtil.byteArrayToInt(pieceIndexField));
                        peerProcess.log.downloadAPiece(this.remote.get_peerID(), MessageUtil.byteArrayToInt(pieceIndexField), numberOfPieces);
                        if (this.downloadStart != 0L) {
                            this.downloadEnd = System.nanoTime();
                            this.remote.setDownload_rate(MessageUtil.byteArrayToInt(msgLength) / (int) (this.downloadEnd - this.downloadStart));
                        }
                        Peer.getPeerInstance().sendHaveToAll(MessageUtil.byteArrayToInt(pieceIndexField));
                    }
                    int pieceIndex = peerCommunicationHelper.getPieceIndex(this.remote.getBitfield(),Peer.getPeerInstance().getBitSet());;
                    peerCommunicationHelper.sendRequestMsg(this.out,  MessageUtil.intToByteArray(pieceIndex));
                    this.downloadStart = System.nanoTime();
                    this.flag = true;
                    break;
                }
            }

            if (Peer.getPeerInstance().get_hasFile() != 1
                    && Peer.getPeerInstance().getBitSet().equals(Peer.getPeerInstance().idealBitset)) {
                if (this.remote.getBitfield().equals(Peer.getPeerInstance().idealBitset)) {
                    System.out.println("files merged by thread : " + this.remote.get_peerID());
                    fileManagerExecutor.filesmerge();
                    Peer.getPeerInstance().set_hasFile(1);
                    peerProcess.log.completionOfDownload();
                    terminateFlag = false;
                }
            }

            if (Peer.getPeerInstance().get_hasFile() == 1 &&
                    this.remote.getBitfield().equals(Peer.getPeerInstance().idealBitset)) {
                terminateFlag = false;
            }
        }
    }
}
