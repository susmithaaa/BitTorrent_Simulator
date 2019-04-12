package p2p;

import java.io.*;
import java.util.Scanner;
import java.util.concurrent.*;

import p2p.FileProcessor.FileProcessor;
import p2p.connection.Client;
import p2p.connection.Server;
import p2p.logPackage.Logger;
import p2p.peer.Peer;

public class peerProcess {
	private static boolean transferDone;
	public static Logger log;
	static int fileSize, sizeOfPiece;
	private static Peer peerReference;
	static int check_exit = 0;
	public static void main(String[] args) throws IOException {
		if (args.length == 0) {
			// do nothing
		} else {
			parseInput(Integer.parseInt(args[0]));
		}
	}

	
	static void parseInput(int temp) throws IOException, FileNotFoundException {
		peerReference = Peer.getPeerObj();
		readCommonConfigFile();
		readPeerConfigFile(temp);
		if (peerReference.peerGotFile != 1) {
			createNewFolder();
		} else {
		//	Defaults.check_exit_id = temp;
			peerReference.setBitset();
			if (!verifyFilePresent(peerReference.getIdOfPeer())) {
				throw new RuntimeException("No file found in directory which is supposed to have the file");
			}
			fileSplit(new File(Defaults.root + "/peer_" + String.valueOf(peerReference.getIdOfPeer()) + "/"
					+ Defaults.getNameofFile()), sizeOfPiece);
		}

		startServerThread();
		startClientThreads();

		peerReference.likedNeighbors();
		peerReference.fetchOptiUnchNeighbor();
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
			Client client = new Client(peerReference.connectToAbovePeers);
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
		File file = new File(Defaults.root + "/p2p/Common.cfg");
		try {
			Scanner s = new Scanner(file);
			while (s.hasNextLine()) {
				// System.out.println("in while loop");
				String parameterName = s.next();
				// System.out.println(parameterName);
				if (parameterName.equals("NumberOfPreferredNeighbors")) {
					// System.out.println("NumberOfPreferredNeighbors");
					int noofneighbors = s.nextInt();
					Defaults.setPreferredNeighborsCount(noofneighbors);
				} else if (parameterName.equals("UnchokingInterval")) {
					int UnchokingInterval = s.nextInt();
					Defaults.setIntervalForUnchoking(UnchokingInterval);
				} else if (parameterName.equals("OptimisticUnchokingInterval")) {
					int OptimisticUnchokingInterval = s.nextInt();
					Defaults.setIntervalForOptimisticUnchoking(OptimisticUnchokingInterval);
				} else if (parameterName.equals("FileName")) {
					String nameoffile = s.next();
					Defaults.setNameOfFile(nameoffile);
				} else if (parameterName.equals("FileSize")) {
					fileSize = s.nextInt();
					Defaults.setSizeOfFile(fileSize);
				} else if (parameterName.equals("PieceSize")) {
					sizeOfPiece = s.nextInt();
					Defaults.setSizeOfPiece(sizeOfPiece);
				}

			}
			peerReference.set_pieceCount();
			for (int i = 0; i < peerReference.peerCountPieces; i++)
				peerReference.bitsetWanted.set(i);
			s.close();
		} catch (RuntimeException exc) {
			System.out.println("There are errors in given file");
		}
	}

	private static void readPeerConfigFile(int curr) throws IOException {
		RemotePojo rem;
		Scanner curr_line = new Scanner(new File(Defaults.root+"/p2p/PeerInfo.cfg"));
		
		while(curr_line.hasNextLine())
		{
			int activePeerId = curr_line.nextInt();
			String host_name = curr_line.next();
			int port_no = curr_line.nextInt();
			int has_fi = curr_line.nextInt();
			if(curr != activePeerId)
			{
				int rid = activePeerId;
				rem = new RemotePojo(rid, host_name, port_no, has_fi, 0, 1);
				if(curr>activePeerId)
				{
					peerReference.connectToAbovePeers.put(activePeerId, rem);
				}
				else
				{
					// peerReference.peersToConnectTo.put(activePeerId, remote);
					peerReference.anticipateConnectionsFromBelowPeers.put(activePeerId, rem);
				}
				peerReference.expected++;	
			}
			else
			{
				peerReference.setIdOfPeer(curr);
			//	peerReference.setPeerHostName(host_name);
				peerReference.setPeerPortNo(port_no);
				peerReference.setPeerGotFile(has_fi);
				log = new Logger(peerReference.getIdOfPeer());
			}
		}
		curr_line.close();

		// bufferedReader.close();
	}

	private static boolean verifyFilePresent(int peerID) throws FileNotFoundException {
		File f = new File(Defaults.root + "/peer_" + String.valueOf(peerID) + "/" + Defaults.getNameofFile());
		boolean res = false;
		if (!f.exists()) {
			// throw new FileNotFoundException("Required File not found");
		} else {
			res = true;
		}
		return res;
	}

	private static void createNewFolder() {
		File file = new File(Defaults.root + "/peer_" + String.valueOf(peerReference.getIdOfPeer()));
		if (!file.exists()) {
			file.mkdir();
		}
	}

	private static void fileSplit(File inputFile, int pieceSize) {
		FileInputStream inputStream;
		int SizeOfFile, remainingSizeOfFile, writePieceIntoByteArray, count = 0;
		byte[] filePiece;
		try {
			inputStream = new FileInputStream(inputFile);
			SizeOfFile = fileSize;
			remainingSizeOfFile = SizeOfFile;
			while (SizeOfFile > 0) {
				
				filePiece = remainingSizeOfFile < pieceSize ? new byte[remainingSizeOfFile] : new byte[pieceSize];

				writePieceIntoByteArray = inputStream.read(filePiece);
				SizeOfFile = SizeOfFile - writePieceIntoByteArray;
				remainingSizeOfFile = remainingSizeOfFile - pieceSize;
				FileProcessor.wholeFileSplitIntoMap.put(count, filePiece);
				count++;
			}
			inputStream.close();
		} catch (IOException exec) {
			exec.printStackTrace();
		}

	}
	
	static void check_exit()
	{
		check_exit++;
		System.out.println(check_exit);
		if(check_exit==2)
			System.exit(0);
	}
}