package com.FileProcessor;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

import com.Constants;
import com.Peer;
import com.messages.Message;
import com.messages.MessageUtil;

public class FileManagerExecutor {

	public static Map<Integer, byte[]> wholeFileSplitIntoMap = Collections.synchronizedMap(new HashMap<>());;
	static Map<Integer, byte[]> trackReceivedFilePieces = Collections.synchronizedMap(new TreeMap<>());

	public byte[] getFilePart(int filePieceIndex) {
		/*if (trackReceivedFilePieces.get(filePartNumber) == null)
			return wholeFileSplitIntoMap.get(filePartNumber);
		else
			return trackReceivedFilePieces.get(filePartNumber);
		*/
		byte[] resultPart = trackReceivedFilePieces.get(filePieceIndex) != null ? trackReceivedFilePieces.get(filePieceIndex) : 
															   wholeFileSplitIntoMap.get(filePieceIndex);
		
		return resultPart;
	}

	public void acceptFilePart(int filePart, Message message) {
		byte[] payLoadWithIndex = message.getPayloadOfMessage();
		byte[] payLoad = MessageUtil.removeFourBytes(payLoadWithIndex);
		trackReceivedFilePieces.put(filePart, payLoad);
	}

	public void filesmerge() throws IOException {
		FileOutputStream fOS;
		// File mergeFile = new File(Constants.root + "/peer_" + String.valueOf(Peer.getPeerInstance().get_peerID()) + "/"
				//+ Constants.getFileName());
		byte[] combinedFile = new byte[Constants.getFileSize()];
		int count= 0;
		for (Map.Entry<Integer, byte[]> e : trackReceivedFilePieces.entrySet()) {
			byte temp[] = e.getValue();
			int len = temp.length;
			for(int i=0;i<len;i++){
				combinedFile[count] = temp[i];
				count++;
			}
		}
		/*int temp = 0;
		while(temp<trackReceivedFilePieces.entrySet().size())
		{
			Map.Entry<Integer, byte[]> ex = (Entry<Integer, byte[]>) trackReceivedFilePieces.entrySet();
			for(int i=0;i<ex.getValue().length;i++){
				combinedFile[count] = ex.getValue()[i];
				count++;
			}
		}*/
		
		fOS = new FileOutputStream(new File(Constants.root + "/peer_" + String.valueOf(Peer.getPeerInstance().get_peerID()) + "/"
				+ Constants.getFileName()));
		fOS.write(combinedFile);
		fOS.flush();
		fOS.close();
	}
}
