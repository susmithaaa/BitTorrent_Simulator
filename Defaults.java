package p2p;

public class Defaults {

	public static Integer preferredNeighborsCount;
	public static Integer sizeOfFile;
	public static Integer sizeOfPiece;
	
	public static final String root = System.getProperty("user.dir");
	
	public static Integer IntervalForUnchoking;
	public static Integer IntervalForOptimisticUnchoking;
	public static String nameofFile;
	
//	public static int check_exit_id = 0;

	
	public static void setPreferredNeighborsCount(int n) {
		preferredNeighborsCount = n;
	}

	public static void setIntervalForUnchoking(int u) {
		IntervalForUnchoking = u;
	}

	public static void setIntervalForOptimisticUnchoking(int o) {
		IntervalForOptimisticUnchoking = o;
	}

	public static String getNameofFile() {
		return nameofFile;
	}

	public static void setNameOfFile(String fn) {
		nameofFile = fn;
	}


	public static void setSizeOfFile(int fs) {
		sizeOfFile = fs;
	}


	public static void setSizeOfPiece(int s) {
		sizeOfPiece = s;
	}
}
