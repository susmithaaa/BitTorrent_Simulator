package com.model;

import com.messages.Message;

/**
 * Author: @DilipKunderu
 */
public class BitField extends Message {
    private byte[] bitField;

    public BitField(byte[] bitField) {
        super((byte) 5, bitField);
        this.bitField = bitField;
    }
}
