package p2p.logPackage;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import p2p.Defaults;
import p2p.RemotePojo;

public class Logger {

	int peerID;
	protected final String logFile;

	SimpleDateFormat simpleFormate = new SimpleDateFormat("MM/dd : HH:mm:ss");

	public Logger(int peerId) {
		this.peerID = peerId;
		this.logFile = Defaults.root + "/" + "log_peer_" + peerId + ".log";
	}

	public void logConnection(int peerId, boolean isConnected) {
		String message;
		if (isConnected) {
			message = String.format("%s: Peer %d makes a connection to %d.\n", simpleFormate.format(new Date()), peerID, peerId);
		} else {
			message = String.format("%s: Peer %d is connected from %d.\n", simpleFormate.format(new Date()), peerID, peerId);
		}
		loggerWrite(logFile, message);
	}

	public void logPreferredNeighbors(Map<RemotePojo, BitSet> PreferredNeighborsMap) {
		String neighborsString = "";
		for (Map.Entry<RemotePojo, BitSet> entry : PreferredNeighborsMap.entrySet()) {
			try {
				neighborsString += entry.getKey().getidOfPeer() + ",";
			}
			catch(Exception e) {
				//e.printStackTrace();
			}
		}
		logPreferredNeighbors(neighborsString.substring(0, neighborsString.length() - 1));
	}

	private void logPreferredNeighbors(String prefNeigh) {
		String message = String.format("%s: Peer %d has the Preferred Neighbors %s.\n", simpleFormate.format(new Date()),
				peerID,prefNeigh);
		loggerWrite(logFile, message);
	}

	public void logOptimisticallyUnchokedNeig(int unchokeId) {
		String message = String.format("%s: Peer %d has the Optimistically Unchoked Neighbor %d.\n",
				simpleFormate.format(new Date()), peerID, unchokeId);
		loggerWrite(logFile, message);
	}

	public void logUnchoked(int peerId) {
		String message = String.format("%s: Peer %d is Unchoked by %d.\n", simpleFormate.format(new Date()), peerID, peerId);
		loggerWrite(logFile, message);
	}

	public void logChoked(int peerId) {
		String message = String.format("%s: Peer %d is Choked by %d.\n", simpleFormate.format(new Date()), peerID, peerId);
		loggerWrite(logFile, message);
	}

	public void logHave(int peerId, int pieceId) {
		String message = String.format("%s: Peer %d received the Have message from %d for piece %d.\n",
				simpleFormate.format(new Date()), peerID, peerId, pieceId);
		loggerWrite(logFile, message);
	}

	public void logInterested(int peerId) {
		String message = String.format("%s: Peer %d received the Interested message from %d.\n",
				simpleFormate.format(new Date()), peerID, peerId);
		loggerWrite(logFile, message);
	}

	public void logNotInterested(int peerId) {
		String message = String.format("%s: Peer %d received the Not Interested message from %d.\n",
				simpleFormate.format(new Date()), peerID, peerId);
		loggerWrite(logFile, message);
	}

	public void logPieceDownloaded(int peer_ID, int pieceId, int numPiece) {
		String message = String.format(
				"%s: Peer %d downloaded the piece %d from %d. Current number of pieces it has is %d.\n",
				simpleFormate.format(new Date()), peerID, pieceId, peer_ID, numPiece);
		loggerWrite(logFile, message);
	}

	public void logDownloadCompleted() {
		String message = String.format("%s: Peer %d has downloaded the complete file.\n", simpleFormate.format(new Date()),
				peerID);
		loggerWrite(logFile, message);
	}

	private void loggerWrite(String logfile, String message) {
		try {
			File logger = new File(logfile);
			if (!logger.exists()) {
				logger.createNewFile();
			}

			FileOutputStream fout = new FileOutputStream(logger, true);
			fout.write(message.getBytes());
			fout.flush();
			fout.close();
		} catch (IOException exc) {
			exc.printStackTrace();
		}
	}

}
