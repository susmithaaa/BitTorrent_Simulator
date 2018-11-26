/*package com.logger;

import java.io.IOException;
import java.util.BitSet;
import java.util.Map;
import com.RemotePeerInfo;

public class EventLogger {
	public Logger logWriter;

	public EventLogger(int peer_ID) {
		logWriter = new Logger(peer_ID);
	}

	public void TCPConnection(int peer_ID, boolean isConnectionMakingPeer) {
		String msg = (isConnectionMakingPeer) ? " makes a connection to Peer " + peer_ID + "."
				: " is connected from Peer " + peer_ID + ".";
		write(msg);
	}

	public void changeOfPreferredNeighbors(Map<RemotePeerInfo, BitSet> PreferredNeighbors) {
		String prefNeighbors = "";
		for (Map.Entry<RemotePeerInfo, BitSet> entry : PreferredNeighbors.entrySet()) {
			prefNeighbors += entry.getKey().get_peerID() + ",";
		}
		changeOfPreferredNeighbors(prefNeighbors.substring(0, prefNeighbors.length() - 1));
	}

	private void changeOfPreferredNeighbors(String preferredNeighbors) {
		String msg = " has the preferred neighbors " + preferredNeighbors + ".";
		write(msg);
	}

	public void changeOfOptimisticallyUnchokedNeighbor(int unchockedNeighborID) {
		String msg = " has the optimistically unchoked neighbor " + unchockedNeighborID + ".";
		write(msg);
	}

	public void unchoking(int peer_ID) {
		String msg = " is unchoked by " + peer_ID + ".";
		write(msg);
	}

	public void choking(int peer_ID) {
		String msg = " is choked by " + peer_ID + ".";
		write(msg);
	}

	public void have(int peer_ID, int pieceIndex) {
		String msg = " received the ‘have’ message from " + peer_ID + " for the piece " + pieceIndex + ".";
		write(msg);
	}

	public void interested(int peer_ID) {
		String msg = " received the ‘interested’ message from " + peer_ID + ".";
		write(msg);
	}

	public void notInterested(int peer_ID) {
		String msg = " received the ‘not interested’ message from " + peer_ID + ".";
		write(msg);
	}

	public void downloadAPiece(int peer_ID, int pieceIndex, int numberOfPieces) {
		String msg = " has downloaded the piece " + pieceIndex + " from " + peer_ID
				+ ". Now the number of pieces it has is " + numberOfPieces + ".";
		write(msg);
	}

	public void completionOfDownload() {
		String msg = " has downloaded the complete file.";
		write(msg);
	}

	private void write(String msg) {
		try {
			logWriter.log(msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}*/


package com.logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.BitSet;
import java.util.Date;
import java.util.Map;

import com.Constants;
import com.RemotePeerInfo;

public class EventLogger
{
    // public Logger logWriter;
    
    int peerID;
    protected final String logFile;
    
    SimpleDateFormat simpleFormate = new SimpleDateFormat ("MM/dd -- HH:mm:ss");

    public EventLogger(int peerID){
        //logWriter = new Logger(peer_ID);
    	this.peerID = peerID;
        this.logFile = Constants.root + "/"+"log_peer_"+peerID+".log";
    }

    public void TCPConnection(int peer_ID, boolean isConnectionMakingPeer){
    	String message;
        if(isConnectionMakingPeer) {
        	message= String.format("%s: Connected to %d.\n", simpleFormate.format(new Date()), peer_ID);
        }
        else {
        	message= String.format("%s: Connected from %d.\n", simpleFormate.format(new Date()), peer_ID);
        }
        loggerWrite(logFile, message);
    }

    public void changeOfPreferredNeighbors(Map<RemotePeerInfo, BitSet> PreferredNeighborsMap){
        String neighborsString = "";
        for(Map.Entry<RemotePeerInfo, BitSet> entry: PreferredNeighborsMap.entrySet()){
        	neighborsString += entry.getKey().get_peerID() + ",";
        }
        changeOfPreferredNeighbors(neighborsString.substring(0,neighborsString.length()-1));
    }
    private void changeOfPreferredNeighbors(String preferredNeighbors){
        String message= String.format("%s: Current preferred Neighbors %s.\n", simpleFormate.format(new Date()), preferredNeighbors);
        loggerWrite(logFile, message);
    }

    public void changeOfOptimisticallyUnchokedNeighbor(int unchockedNeighborID){
        String message= String.format("%s: Added with Optimistcally UN_CHOKED neighbor %d.\n", simpleFormate.format(new Date()), unchockedNeighborID);
        loggerWrite(logFile, message);
    }

    public void unchoking(int peer_ID){
        String message= String.format("%s: Is UN_CHOKED by %d.\n", simpleFormate.format(new Date()), peer_ID);
        loggerWrite(logFile, message);
    }

    public void choking(int peer_ID){
        String message= String.format("%s: Is CHOKED by %d.\n", simpleFormate.format(new Date()), peer_ID);
        loggerWrite(logFile, message);
    }

    public void have(int peer_ID, int pieceIndex){
        String message= String.format("%s: Received the HAVE message from %d for piece %d.\n", simpleFormate.format(new Date()), peer_ID, pieceIndex);
        loggerWrite(logFile, message);
    }

    public void interested(int peer_ID){
        String message= String.format("%s: Received the INTERESTED message from %d.\n", simpleFormate.format(new Date()), peer_ID);
        loggerWrite(logFile, message);
    }

    public void notInterested(int peer_ID){
        String message= String.format("%s: Received the NOT INTERESTED message from %d.\n", simpleFormate.format(new Date()), peer_ID);
        loggerWrite(logFile, message);
    }

    public void downloadAPiece(int peer_ID, int pieceIndex, int numberOfPieces){
        String message= String.format("%s: Peer %d downloaded the piece %d from %d. Current number of pieces downloaded are: %d.\n", simpleFormate.format(new Date()), peerID, pieceIndex, peer_ID, numberOfPieces);
        loggerWrite(logFile, message);
    }

    public void completionOfDownload(){
    	String message= String.format("%s: Peer %d downloaded the complete file.\n", simpleFormate.format(new Date()), peerID);
        loggerWrite(logFile, message);
    }

    private void loggerWrite(String logfile, String message) {
        try {
            //logWriter.log(null, msg);
        	File logger=new File(logfile);
        	if(!logger.exists()) {
        		logger.createNewFile();
        	}
        	
        	FileOutputStream fout= new FileOutputStream(logger, true);
        	fout.write(message.getBytes());
        	fout.flush();
        	fout.close();
        	/*Date newdate = new Date( );
            SimpleDateFormat simpleFormate = new SimpleDateFormat ("HH:mm:ss");
            String curTime = simpleFormate.format(newdate);
            FileWriter fileWrite = new FileWriter(logFile, true);
            fileWrite.write(curTime + ": Peer " + peerID + message + "\n");
            fileWrite.flush();
            fileWrite.close();*/
        } catch (IOException exc) {
            exc.printStackTrace();
        }
    }

}


