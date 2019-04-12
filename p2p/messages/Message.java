package p2p.messages;

import java.io.Serializable;
// import java.util.Arrays;

public class Message implements Serializable {
	private static final long serialVersionUID = 6529685098267757690L;

	private byte[] lengthOfMessage;
	private byte typeOfMessage;
	private byte[] payloadOfMessage;


	public Message(byte message_type, byte[] messagePayload) {
		this.typeOfMessage = message_type;
		this.payloadOfMessage = messagePayload;
		this.lengthOfMessage = messagePayload == null ? MessageUtil.intToByteArray(1)
				: MessageUtil.intToByteArray(messagePayload.length + 1);
		// this.message_length = MessageUtil.intToByteArray(messagePayload.length + 1);

	}

	public byte[] getLengthOfMessage() {
		return lengthOfMessage;
	}

	public byte getTypeOfMessage() {
		return typeOfMessage;
	}

	public byte[] getPayloadOfMessage() {
		return payloadOfMessage;
	}

}
