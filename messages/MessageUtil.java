package com.messages;

//import java.io.BufferedInputStream;
// import java.io.IOException;
import java.util.BitSet;

public class MessageUtil {

	public static byte[] concatenateByteArrays(byte[] a, byte[] b) {
		byte[] result = new byte[a.length + b.length];
		copyArrayFixed(result, a);
		// System.arraycopy(a, 0, result, 0, a.length);
		// System.arraycopy(b, 0, result, a.length, b.length);
		copyArrayVariable(result, b, a.length);
		return result;
	}

	public static byte[] concatenateByteArrays(byte[] a, int aLength, byte[] b, int bLength) {
		byte[] result = new byte[aLength + bLength];
		copyArrayFixed(result, a);
		// System.arraycopy(a, 0, result, 0, aLength);
		// System.arraycopy(b, 0, result, aLength, bLength);
		copyArrayVariable(result, b, aLength);
		return result;
	}

	public static byte[] intToByteArray(int a) {
		byte[] ret = new byte[4];
		int _2 =  (255 & (a >> 8));
		int _1 =  (255 & (a >> 16));
		int _3 =  (255 & a);
		int _0 =  (255 & (a >> 24));
		
		ret[2] = (byte) _2;
		ret[1] = (byte) _1;
		ret[3] = (byte) _3;
		ret[0] = (byte) _0;
		return ret;
	}

	public static int byteArrayToInt(byte[] b) {
		int value = 0;
		for (int i = 0; i < 4; i++) {
			int shift = (4 - i -1) * 8;
			value += (255 & b[i]) << shift;
		}
		return value;
	}

	public static byte[] concatenateByte(byte[] a, byte b) {
		byte[] result = new byte[a.length + 1];
		// System.arraycopy(a, 0, result, 0, a.length);
		copyArrayFixed(result, a);
		result[a.length] = b;
		return result;
	}

	/*
	 * public static byte[] readBytes(BufferedInputStream in, byte[] byteArray, int
	 * length) throws IOException { int len = length; int idx = 0; while (len != 0)
	 * { int dataAvailableLength = in.available(); int read = Math.min(len,
	 * dataAvailableLength); byte[] dataRead = new byte[read]; if (read != 0) {
	 * in.read(dataRead); byteArray = MessageUtil.concatenateByteArrays(byteArray,
	 * idx, dataRead, read); idx += read; len -= read; } } return byteArray; }
	 */

	public static BitSet fromByteArray(byte[] bytes) {
		BitSet bits = new BitSet();
		int temp = 8;
		int len = bytes.length;
		int mul = temp * len;
		int _1 = 1;
		for (int i = 0; i < mul; i++) {
			if (((1 << (i % temp)) & (bytes[len - i / temp - _1])) > 0) {
				bits.set(i);
			}
		}
		return bits;
	}

	public static byte[] toByteArray(BitSet bits) {
		int temp = 8;
		int _1 = 1;
		byte[] bytes = new byte[bits.length() / temp + _1];
		for (int i = 0; i < bits.length(); i++) {
			if (bits.get(i)) {
				bytes[bytes.length - i / temp - _1] |= 1 << (i % temp);
			}
		}
		return bytes;
	}

	public static byte[] removeFourBytes(byte[] a) {
		byte[] actualPayload = new byte[a.length - 4];
		/*for (int i = 4; i < a.length; i++) {
			actualPayload[i - 4] = a[i];
			a[i - 4] = a[i];
		}*/
		
		 System.arraycopy(a, 4, actualPayload, 0, a.length-4);
		
		return actualPayload;
	}

	static void copyArrayFixed(byte[] res, byte[] first)
	{
		// return new byte[5];
		System.arraycopy(first, 0, res, 0, first.length);
	}
	
	static void copyArrayVariable(byte[] res, byte[] second, int first_length)
	{
		System.arraycopy(second, 0, res, first_length, second.length);
	}
	
	/*public static byte[] getPieceIndexFromPayload(byte[] a) {
		byte[] pieceIndex = new byte[4];
		for (int i = 0; i < 4; i++) {
			pieceIndex[i] = a[i];
		}
		return pieceIndex;
	}*/
}
