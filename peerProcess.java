package com;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
// import java.util.LinkedList;
// import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.FileProcessor.FileManagerExecutor;
import com.logger.EventLogger;

public class peerProcess {
	private static Peer peerReference;
	private static boolean transferDone;
	public static EventLogger log;
	static int fileSize, sizeOfPiece;

	public static void main(String[] args) throws IOException {
		
		// transferDone = false;
		
		// transferDone = false;
				if (args.length == 0) 
				{
					// do nothing
				}
				else
				{
					parseInput(Integer.parseInt(args[0]));
				}
			}

			// saifil added
			static void parseInput(int temp) throws IOException
			{
				// FileManagerExecutor fileManagerExecutor = new FileManagerExecutor();
				peerReference = Peer.getPeerInstance();
					try {
						readCommonConfigFile();
					} catch (FileNotFoundException fileNotfoundException) {
						// TODO Log
						fileNotfoundException.printStackTrace();
					} 
					/* saifil -> no need of below finally
					finally {
						// TODO Log successful setting of vars
					}*/
					try {
						// saifil added temp variable to read input parameter
						// int temp = Integer.parseInt(args[0]);
						readPeerConfigFile(temp);
						if (peerReference.get_hasFile() != 1) {
							createNewFolder();
						} else {
							peerReference.setBitset();
							if (!verifyFilePresent(peerReference.get_peerID())) {
								throw new RuntimeException("No file found in directory which is supposed to have the file");
							}
							fileSplit(new File(Constants.root + "/peer_" + String.valueOf(peerReference.get_peerID())
							+ "/" + Constants.getFileName()), sizeOfPiece);
						}
					} catch (FileNotFoundException fileNotfoundException) {
						// TODO Log
						fileNotfoundException.printStackTrace();
					}  
					/*
					saifil -> no need of below finally
					finally {
						// TODO Log successful setting of vars
					}*/

					ScheduledThreadPoolExecutor InstantiateIncomingServerThreads = new ScheduledThreadPoolExecutor(1);

					InstantiateIncomingServerThreads.schedule(() -> {
						Server server = new Server();
						new Thread(server).start();
					}, 0, TimeUnit.MILLISECONDS);

					ScheduledThreadPoolExecutor InstantiateOutgoingClientThreads = new ScheduledThreadPoolExecutor(1);

					InstantiateOutgoingClientThreads.schedule(() -> {
						Client client = new Client(peerReference.peersToConnectTo);
						new Thread(client).start();
					}, 1, TimeUnit.MILLISECONDS);

					peerReference.PreferredNeighbours();
					peerReference.OptimisticallyUnchokedNeighbour();
			}

		
		/* if (args.length > 0) {
			peerReference = Peer.getPeerInstance();
			try {
				setCommonConfigVars();
			} catch (FileNotFoundException fileNotfoundException) {
				// TODO Log
				fileNotfoundException.printStackTrace();
			} finally {
				// TODO Log successful setting of vars
			}
			try {
				buildRemotePeersList(Integer.parseInt(args[0]));
				if (peerReference.get_hasFile() == 1) {
					peerReference.setBitset();
					if (!checkFileExists(peerReference.get_peerID())) {
						throw new RuntimeException("No file found in peer which is supposed to have the file");
					}
					fileManagerExecutor.fileSplit(new File(Constants.root + "/peer_" + String.valueOf(peerReference.get_peerID())
					+ "/" + Constants.getFileName()), Constants.getPieceSize());
				} else {
					createDirectory();
				}
			} catch (FileNotFoundException fileNotfoundException) {
				// TODO Log
		//		fileNotfoundException.printStackTrace();
			} finally {
				// TODO Log successful setting of vars
			}

			ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(2);
			executor.schedule(() -> {
				Server server = new Server();
				new Thread(server).start();
			}, 0, TimeUnit.MILLISECONDS);

//			executor = new ScheduledThreadPoolExecutor(1);
			executor.schedule(() -> {
				Client client = new Client(peerReference.peersToConnectTo);
				new Thread(client).start();
			}, 1, TimeUnit.MILLISECONDS);

			 Peer.getPeerInstance().PreferredNeighbours();
	         Peer.getPeerInstance().OptimisticallyUnchokedNeighbour();
		}*/


	static boolean isFileTransferDone() {
		return transferDone;
	}

	static synchronized void setFileTransferDone(boolean completed) {
		peerProcess.transferDone = completed;
	}

	/*private static void readCommonConfigFile() throws IOException {
		BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(Constants.common)));

		String s;
		String[] t;

		List<String> commonList = new LinkedList<>();

		while ((s = bufferedReader.readLine()) != null) {
			t = s.split(" ");
			commonList.add(t[1]);
		}

		Constants.setNumberOfPreferredNeighbors(Integer.parseInt(commonList.get(0)));
		Constants.setUnchokingInterval(Integer.parseInt(commonList.get(1)));
		Constants.setOptimisticUnchokingInterval(Integer.parseInt(commonList.get(2)));
		Constants.setFileName(commonList.get(3));
		Constants.setFileSize(Integer.parseInt(commonList.get(4)));
		Constants.setPieceSize(Integer.parseInt(commonList.get(5)));

		peerReference.set_pieceCount();

		for (int i = 0; i < peerReference.get_pieceCount(); i++)
			peerReference.idealBitset.set(i);

		bufferedReader.close();
	}*/
	
	private static void readCommonConfigFile() throws IOException {
		File file = new File(Constants.common);
    	try
    	{
    		Scanner s = new Scanner(file);	
	    while(s.hasNextLine())
	    {
			//System.out.println("in while loop");
	    	String parameterName = s.next();
			//System.out.println(parameterName);
	    	if(parameterName.equals("NumberOfPreferredNeighbors"))
	    	{
				//System.out.println("NumberOfPreferredNeighbors");
	    		int noofneighbors = s.nextInt();
	    		Constants.setNumberOfPreferredNeighbors(noofneighbors);
	    	}
	    	else if(parameterName.equals("UnchokingInterval"))
	    	{
	    		int UnchokingInterval = s.nextInt();
	    		Constants.setUnchokingInterval(UnchokingInterval);		
	    	}
	    	else if(parameterName.equals("OptimisticUnchokingInterval"))
	    	{
	    		int OptimisticUnchokingInterval = s.nextInt();
	    		Constants.setOptimisticUnchokingInterval(OptimisticUnchokingInterval);
	    	}
	    	else if(parameterName.equals("FileName"))
	    	{
	    		String nameoffile = s.next();
	    		Constants.setFileName(nameoffile);
	    	}
	    	else if(parameterName.equals("FileSize"))
	    	{
	    		fileSize = s.nextInt();
	    		Constants.setFileSize(fileSize);
	    	}
	    	else if(parameterName.equals("PieceSize"))
	    	{
	    		sizeOfPiece = s.nextInt();
	    		Constants.setPieceSize(sizeOfPiece);
	    	}
    	
    }
    peerReference.set_pieceCount();
    for (int i = 0; i < peerReference.get_pieceCount(); i++)
        peerReference.idealBitset.set(i);
	    s.close();
    }
    catch(RuntimeException exc)
    	{
    		System.out.println("There are errors in given file");
    	}
	}

	private static void readPeerConfigFile(int current) throws IOException {
		BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(Constants.peers)));
		String s;
		String[] t;

		RemotePeerInfo remote;
		while ((s = bufferedReader.readLine()) != null) {
			t = s.split("\\s+");
			int currPeerID = Integer.parseInt(t[0]);
			if (current == Integer.parseInt(t[0])) {
				peerReference.set_peerID(current);
				peerReference.set_hostName(t[1]);
				peerReference.set_port(Integer.parseInt(t[2]));
				peerReference.set_hasFile(Integer.parseInt(t[3]));
				log = new EventLogger(peerReference.get_peerID());
			} else {
				remote = new RemotePeerInfo(Integer.parseInt(t[0]), t[1], Integer.parseInt(t[2]),
						Integer.parseInt(t[3]));
				if (current < currPeerID) {
					peerReference.peersToExpectConnectionsFrom.put(currPeerID, remote);
				} else {
					peerReference.peersToConnectTo.put(currPeerID, remote);
				}
				
				// current<currPeerID ? peerReference.peersToExpectConnectionsFrom.put(currPeerID, remote):peerReference.peersToConnectTo.put(currPeerID, remote);
				
				peerReference.expected++;
//				peer.connectedPeers.add(remote);
			}
		}

		bufferedReader.close();
	}

	private static boolean verifyFilePresent(int peerID) throws FileNotFoundException {
		File f = new File(Constants.root + "/peer_" + String.valueOf(peerID) + "/" + Constants.getFileName());
		boolean res = false;
		if (!f.exists()) {
	//		throw new FileNotFoundException("Required File not found");
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
				/*if (remainingSizeOfFile < pieceSize) {
					filePiece = new byte[remainingSizeOfFile];
				} else {
					filePiece = new byte[pieceSize];
				}*/
				
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
}
