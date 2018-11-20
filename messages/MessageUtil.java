package com.messages;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.BitSet;

public class MessageUtil {

	public static byte[] concatenateByteArrays(byte[] a, byte[] b) {
		byte[] result = new byte[a.length + b.length];
		System.arraycopy(a, 0, result, 0, a.length);
		System.arraycopy(b, 0, result, a.length, b.length);
		return result;
	}

	public static byte[] concatenateByteArrays(byte[] a, int aLength, byte[] b, int bLength) {
		byte[] result = new byte[aLength + bLength];
		System.arraycopy(a, 0, result, 0, aLength);
		System.arraycopy(b, 0, result, aLength, bLength);
		return result;
	}

	public static byte[] intToByteArray(int a) {
		byte[] ret = new byte[4];
		ret[3] = (byte) (a & 0xFF);
		ret[2] = (byte) ((a >> 8) & 0xFF);
		ret[1] = (byte) ((a >> 16) & 0xFF);
		ret[0] = (byte) ((a >> 24) & 0xFF);
		return ret;
	}

	public static int byteArrayToInt(byte[] b) {
		int value = 0;
		for (int i = 0; i < 4; i++) {
			int shift = (4 - 1 - i) * 8;
			value += (b[i] & 0x000000FF) << shift;
		}
		return value;
	}

	public static byte[] concatenateByte(byte[] a, byte b) {
		byte[] result = new byte[a.length + 1];
		System.arraycopy(a, 0, result, 0, a.length);
		result[a.length] = b;
		return result;
	}

	public static byte[] readBytes(BufferedInputStream in, byte[] byteArray, int length) throws IOException {
		int len = length;
		int idx = 0;
		while (len != 0) {
			int dataAvailableLength = in.available();
			int read = Math.min(len, dataAvailableLength);
			byte[] dataRead = new byte[read];
			if (read != 0) {
				in.read(dataRead);
				byteArray = MessageUtil.concatenateByteArrays(byteArray, idx, dataRead, read);
				idx += read;
				len -= read;
			}
		}
		return byteArray;
	}

	public static BitSet fromByteArray(byte[] bytes) {
		BitSet bits = new BitSet();
		for (int i = 0; i < bytes.length * 8; i++) {
			if ((bytes[bytes.length - i / 8 - 1] & (1 << (i % 8))) > 0) {
				bits.set(i);
			}
		}
		return bits;
	}

	public static byte[] toByteArray(BitSet bits) {
		byte[] bytes = new byte[bits.length() / 8 + 1];
		for (int i = 0; i < bits.length(); i++) {
			if (bits.get(i)) {
				bytes[bytes.length - i / 8 - 1] |= 1 << (i % 8);
			}
		}
		return bytes;
	}

	public static byte[] removeFourBytes(byte[] a) {
		byte[] actualPayload = new byte[a.length - 4];
		for (int i = 4; i < a.length; i++) {
			actualPayload[i - 4] = a[i];
			a[i - 4] = a[i];
		}
		return actualPayload;
	}

	public static byte[] getPieceIndexFromPayload(byte[] a){
		byte[] pieceIndex = new byte[4];
		for(int i=0;i<4;i++){
			pieceIndex[i] = a[i];
		}
		return pieceIndex;
	}
}
