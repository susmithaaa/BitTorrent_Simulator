package com.model;

import com.messages.Message;

/**
 * Author: @DilipKunderu
 */
public class Have extends Message {
	
    public Have(byte[] piece_index) {
        super((byte) 4,piece_index);
    }
}
