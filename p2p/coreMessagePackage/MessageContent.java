package p2p.coreMessagePackage;

import java.io.Serializable;

public class MessageContent implements Serializable {
	private static final long serialVersionUID = 7690652637509426786L; // Check on this

	public byte[] lengthOfMessage;
	public byte typeOfMessage;
	private byte[] payloadOfMessage;

	public MessageContent(byte msgtype, byte[] msgPayload) {
		this.typeOfMessage = msgtype;
		this.payloadOfMessage = msgPayload;
		this.lengthOfMessage = msgPayload == null ? MessageHelper.convIntByteArray(1)
				: MessageHelper.convIntByteArray(msgPayload.length + 1);

	}
	
	public void setLengthOfMessage(byte[] lengthOfMessage) {
		this.lengthOfMessage= lengthOfMessage;
	}
	
	public void setTypeOfMessage(byte typeOfMessage) {
		this.typeOfMessage = typeOfMessage;
	}

	public byte[] getPayloadOfMessage() {
		return payloadOfMessage;
	}
	
	public void setPayloadOfMessage(byte[] payloadOfMessage) {
		this.payloadOfMessage= payloadOfMessage;
	}

}