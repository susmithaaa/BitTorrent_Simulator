package p2p.coreMessagePackage;

import java.util.BitSet;

public class MessageHelper {

	public static byte[] combineByteArrays(byte[] first, byte[] second) {
		byte[] outcome = new byte[first.length + second.length];
		copyArrayFixed(outcome, first);
		copyArrayVariable(outcome, second, first.length);
		return outcome;
	}

	public static byte[] combineByteArrays(byte[] first, int firstLength, byte[] second, int secondLength) {
		byte[] outcome = new byte[firstLength + secondLength];
		copyArrayFixed(outcome, first);
		copyArrayVariable(outcome, second, firstLength);
		return outcome;
	}

	public static byte[] convIntByteArray(int value) {
		byte[] outcome = new byte[4];
		int _2 = (255 & (value >> 8));
		int _1 = (255 & (value >> 16));
		int _3 = (255 & value);
		int _0 = (255 & (value >> 24));

		outcome[2] = (byte) _2;
		outcome[1] = (byte) _1;
		outcome[3] = (byte) _3;
		outcome[0] = (byte) _0;
		return outcome;
	}

	public static int convByteArrayInt(byte[] value) {
		int outcome = 0;
		for (int i = 0; i < 4; i++) {
			int a = (4 - i - 1) * 8;
			outcome += (255 & value[i]) << a;
		}
		return outcome;
	}

	public static byte[] combineByteFields(byte[] first, byte second) {
		byte[] outcome = new byte[first.length + 1];
		copyArrayFixed(outcome, first);
		outcome[first.length] = second;
		return outcome;
	}


	public static BitSet covertByteToBitset(byte[] bytearray, int temp) {
		BitSet bitset = new BitSet();
		int len = bytearray.length;
		int product = temp * len;
		int _1 = 1;
		for (int i = 0; i < product; i++) {
			if (((1 << (i % temp)) & (bytearray[len - i / temp - _1])) > 0) {
				bitset.set(i);
			}
		}
		return bitset;
	}

	public static byte[] convertToByteArray(BitSet bitset, int temp) {
		int _1 = 1;
		byte[] bytearray = new byte[bitset.length() / temp + _1];
		for (int i = 0; i < bitset.length(); i++) {
			if (bitset.get(i)) {
				bytearray[bytearray.length - i / temp - _1] |= 1 << (i % temp);
			}
		}
		return bytearray;
	}

	public static byte[] trimFourBytes(byte[] bytearray, int constant) {
		byte[] resultpayLoad = new byte[bytearray.length - constant];
		System.arraycopy(bytearray, constant, resultpayLoad, 0, bytearray.length - constant);

		return resultpayLoad;
	}

	static void copyArrayFixed(byte[] res, byte[] first) {
		System.arraycopy(first, 0, res, 0, first.length);
	}

	static void copyArrayVariable(byte[] res, byte[] second, int first_length) {
		System.arraycopy(second, 0, res, first_length, second.length);
	}

}