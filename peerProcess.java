package com;

// import java.io.BufferedReader;
import java.io.*;
// import java.util.LinkedList;
// import java.util.List;
import java.util.Scanner;
import java.util.concurrent.*;

import com.FileProcessor.FileManagerExecutor;
import com.logger.EventLogger;

public class peerProcess {
	private static Peer peerReference;
	private static boolean transferDone;
	public static EventLogger log;
	static int fileSize, sizeOfPiece;
	static int check_exit = 0;
	public static void main(String[] args) throws IOException {
		if (args.length == 0) {
			// do nothing
		} else {
			parseInput(Integer.parseInt(args[0]));
		}
	}

	// Saifil added
	static void parseInput(int temp) throws IOException, FileNotFoundException {
		peerReference = Peer.getPeerInstance();
		readCommonConfigFile();
		readPeerConfigFile(temp);
		if (peerReference.get_hasFile() != 1) {
			createNewFolder();
		} else {
			peerReference.setBitset();
			if (!verifyFilePresent(peerReference.get_peerID())) {
				throw new RuntimeException("No file found in directory which is supposed to have the file");
			}
			fileSplit(new File(Constants.root + "/peer_" + String.valueOf(peerReference.get_peerID()) + "/"
					+ Constants.getFileName()), sizeOfPiece);
		}

		startServerThread();
		startClientThreads();

		peerReference.PreferredNeighbours();
		peerReference.OptimisticallyUnchokedNeighbour();
	}

	static void startServerThread() {
		ScheduledThreadPoolExecutor InstantiateIncomingServerThreads = new ScheduledThreadPoolExecutor(1);
		InstantiateIncomingServerThreads.schedule(() -> {
			Server server = new Server();
			new Thread(server).start();
		}, 0, TimeUnit.MILLISECONDS);
	}

	static void startClientThreads() {
		ScheduledThreadPoolExecutor InstantiateOutgoingClientThreads = new ScheduledThreadPoolExecutor(1);
		InstantiateOutgoingClientThreads.schedule(() -> {
			Client client = new Client(peerReference.peersToConnectTo);
			new Thread(client).start();
		}, 1, TimeUnit.MILLISECONDS);
	}

	static boolean isFileTransferDone() {
		return transferDone;
	}

	static synchronized void setFileTransferDone(boolean completed) {
		peerProcess.transferDone = completed;
	}

	private static void readCommonConfigFile() throws IOException {
		File file = new File(Constants.common);
		try {
			Scanner s = new Scanner(file);
			while (s.hasNextLine()) {
				// System.out.println("in while loop");
				String parameterName = s.next();
				// System.out.println(parameterName);
				if (parameterName.equals("NumberOfPreferredNeighbors")) {
					// System.out.println("NumberOfPreferredNeighbors");
					int noofneighbors = s.nextInt();
					Constants.setNumberOfPreferredNeighbors(noofneighbors);
				} else if (parameterName.equals("UnchokingInterval")) {
					int UnchokingInterval = s.nextInt();
					Constants.setUnchokingInterval(UnchokingInterval);
				} else if (parameterName.equals("OptimisticUnchokingInterval")) {
					int OptimisticUnchokingInterval = s.nextInt();
					Constants.setOptimisticUnchokingInterval(OptimisticUnchokingInterval);
				} else if (parameterName.equals("FileName")) {
					String nameoffile = s.next();
					Constants.setFileName(nameoffile);
				} else if (parameterName.equals("FileSize")) {
					fileSize = s.nextInt();
					Constants.setFileSize(fileSize);
				} else if (parameterName.equals("PieceSize")) {
					sizeOfPiece = s.nextInt();
					Constants.setPieceSize(sizeOfPiece);
				}

			}
			peerReference.set_pieceCount();
			for (int i = 0; i < peerReference.get_pieceCount(); i++)
				peerReference.idealBitset.set(i);
			s.close();
		} catch (RuntimeException exc) {
			System.out.println("There are errors in given file");
		}
	}

	private static void readPeerConfigFile(int curr) throws IOException {
		RemotePeerInfo rem;
		Scanner curr_line = new Scanner(new File(Constants.peers));
		
		while(curr_line.hasNextLine())
		{
			int activePeerId = curr_line.nextInt();
			String host_name = curr_line.next();
			int port_no = curr_line.nextInt();
			int has_fi = curr_line.nextInt();
			if(curr != activePeerId)
			{
				int rid = activePeerId;
				rem = new RemotePeerInfo(rid, host_name, port_no, has_fi);
				if(curr>activePeerId)
				{
					peerReference.peersToConnectTo.put(activePeerId, rem);
				}
				else
				{
					// peerReference.peersToConnectTo.put(activePeerId, remote);
					peerReference.peersToExpectConnectionsFrom.put(activePeerId, rem);
				}
				peerReference.expected++;	
			}
			else
			{
				peerReference.set_peerID(curr);
				peerReference.set_hostName(host_name);
				peerReference.set_port(port_no);
				peerReference.set_hasFile(has_fi);
				log = new EventLogger(peerReference.get_peerID());
			}
		}
		curr_line.close();

		// bufferedReader.close();
	}

	private static boolean verifyFilePresent(int peerID) throws FileNotFoundException {
		File f = new File(Constants.root + "/peer_" + String.valueOf(peerID) + "/" + Constants.getFileName());
		boolean res = false;
		if (!f.exists()) {
			// throw new FileNotFoundException("Required File not found");
		} else {
			res = true;
		}
		return res;
	}

	private static void createNewFolder() {
		File file = new File(Constants.root + "/peer_" + String.valueOf(peerReference.get_peerID()));
		if (!file.exists()) {
			file.mkdir();
		}
	}

	private static void fileSplit(File inputFile, int pieceSize) {
		// wholeFileSplitIntoMap = Collections.synchronizedMap(new HashMap<>());
		FileInputStream inputStream;
		int SizeOfFile, remainingSizeOfFile, writePieceIntoByteArray, count = 0;
		byte[] filePiece;
		try {
			inputStream = new FileInputStream(inputFile);
			SizeOfFile = fileSize;
			remainingSizeOfFile = SizeOfFile;
			while (SizeOfFile > 0) {
				/*
				 * if (remainingSizeOfFile < pieceSize) { filePiece = new
				 * byte[remainingSizeOfFile]; } else { filePiece = new byte[pieceSize]; }
				 */

				filePiece = remainingSizeOfFile < pieceSize ? new byte[remainingSizeOfFile] : new byte[pieceSize];

				writePieceIntoByteArray = inputStream.read(filePiece);
				SizeOfFile = SizeOfFile - writePieceIntoByteArray;
				remainingSizeOfFile = remainingSizeOfFile - pieceSize;
				FileManagerExecutor.wholeFileSplitIntoMap.put(count, filePiece);
				count++;
			}
			inputStream.close();
		} catch (IOException exec) {
			exec.printStackTrace();
		}

	}
	
	/*static void check_exit()
	{
		check_exit++;
		System.out.println(check_exit);
		if(check_exit==2)
			System.exit(0);
	}*/
}
