package com.messages;

import com.MessageType;
//import com.model.*;

public class MessageHandler {
	private MessageType message_type;
	private byte[] messagePayload;

	public MessageHandler(MessageType message_type) {
		this.message_type = message_type;
	}

	public MessageHandler(MessageType message_type, byte[] messagePayload) {
		this.message_type = message_type;
		this.messagePayload = messagePayload;
	}

	public Message buildMessage() throws Exception {
		Message message = null;

		if (MessageType.choke == this.message_type) {
			message = new Message((byte) 0, null);
		} else if (MessageType.unchoke == this.message_type) {
			message = new Message((byte) 1, null);
		} else if (MessageType.interested == this.message_type) {
			message = new Message((byte) 2, null);
		} else if (MessageType.notinterested == this.message_type) {
			message = new Message((byte) 3, null);
		} else if (MessageType.have == this.message_type) {
			message = new Message((byte) 4, this.messagePayload);
		} else if (MessageType.bitfield == this.message_type) {
			message = new Message((byte) 5, this.messagePayload);
		} else if (MessageType.request == this.message_type) {
			message = new Message((byte) 6, this.messagePayload);
		} else if (MessageType.piece == this.message_type) {
			message = new Message((byte) 7, this.messagePayload);
		} else {
			throw new Exception("Invalid message " + message_type);
		}

		/*
		 * switch (this.message_type) { // if(MessageType.choke==this.message_type)
		 * 
		 * 
		 * case choke: { message = new Choke(); break; } case unchoke: { message = new
		 * UnChoke(); break; } case interested: { message = new Interested(); break; }
		 * case notinterested: { message = new NotInterested(); break; } case have: {
		 * message = new Have(this.messagePayload); break; } case bitfield: { message =
		 * new BitField(this.messagePayload); break; } case request: { message = new
		 * Request(this.messagePayload); break; } case piece: { message = new
		 * Piece(this.messagePayload); break; } default: { throw new
		 * Exception("Not a valid message type: " + message_type); } }
		 */
		return message;
	}

}
