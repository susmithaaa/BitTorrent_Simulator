package p2p.messages;

import p2p.MessageType;
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
		
		return message;
	}

}
