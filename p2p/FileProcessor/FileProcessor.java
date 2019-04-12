package p2p.FileProcessor;

import java.io.*;
import java.util.*;

import p2p.Defaults;
import p2p.coreMessagePackage.MessageContent;
import p2p.coreMessagePackage.MessageHelper;
import p2p.peer.Peer;

public class FileProcessor {

	public static Map<Integer, byte[]> wholeFileSplitIntoMap = Collections.synchronizedMap(new HashMap<>());
	static Map<Integer, byte[]> trackReceivedFilePieces = Collections.synchronizedMap(new TreeMap<>());

	public byte[] getFilePart(int filePieceIndex) {
		byte[] resultPart = trackReceivedFilePieces.get(filePieceIndex) != null
				? trackReceivedFilePieces.get(filePieceIndex)
				: wholeFileSplitIntoMap.get(filePieceIndex);

		return resultPart;
	}
	
	public void combineFile() throws IOException {
		FileOutputStream fOS;
		byte[] integratedFile = new byte[Defaults.sizeOfFile];
		int ct = 0;
		for (Map.Entry<Integer, byte[]> emap : trackReceivedFilePieces.entrySet()) {
			byte temp[] = emap.getValue();
			int len = temp.length;
			for (int i = 0; i < len; i++) {
				integratedFile[ct] = temp[i];
				ct++;
			}
		}

		fOS = new FileOutputStream(new File(Defaults.root + "/peer_"
				+ String.valueOf(Peer.getPeerObj().getIdOfPeer()) + "/" + Defaults.getNameofFile()));
		fOS.write(integratedFile);
		fOS.flush();
		fOS.close();
	}

	public void receivePartOfFile(MessageContent msg, int fileChunk) {
		byte[] msgPayLoadWithIndex = msg.getPayloadOfMessage();
		byte[] msgPayLoad = MessageHelper.trimFourBytes(msgPayLoadWithIndex, 4);
		trackReceivedFilePieces.put(fileChunk, msgPayLoad);
	}

	
}