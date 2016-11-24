package messages;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Message to communicate between instances on the network.
 */
public class Message {
    private byte type;
    private byte[] message;

    public Message(byte messageType, byte[] message) {
        this.type = messageType;
        this.message = message;
    }

    public Message(byte messageType) {
        this.type = messageType;
        this.message = null;
    }

    public Message(MessageType messageType, byte[] message) {
        this(messageType.getType(), message);
    }

    public Message(MessageType messageType) {
        this(messageType.getType());
    }

    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(type);
        if (message != null)
            outputStream.write(message);

        return outputStream.toByteArray();
    }

    public byte[] getMessage() {
        return message;
    }

    public MessageType getMessageType() {
        return MessageType.fromByte(type);
    }
}
