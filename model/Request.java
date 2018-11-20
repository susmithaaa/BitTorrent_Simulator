package com.model;

import com.messages.Message;

/**
 * Author: @DilipKunderu
 */
public class Request extends Message {

   public Request(byte[] pieceIndex) {
        super((byte) 6,pieceIndex);
    }



}
