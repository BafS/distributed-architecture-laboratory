package messages;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Message to communicate between instances on the network.
 *
 * Types [WIP]
 *  1
 *  2
 */
public class Message {
    private byte type;
    private byte[] message;

    public static final byte REGISTER = 0x0;

    public static final byte TIME = 0x10;
//    public static final byte SUM = 0x11;

    public Message(byte messageType, byte[] message) {
        this.type = messageType;
        this.message = message;
    }

    public Message(byte messageType) {
        this.type = messageType;
        this.message = null;
    }

    public byte getType() {
        return type;
    }

//    public MessageType getMessageType() {
//        return type;
//    }

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
}
