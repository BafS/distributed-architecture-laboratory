package messages;

import util.ByteArrayable;
import util.MachineType;

import java.io.*;
import java.util.Arrays;

/**
 * Message used to communicate between instances on the network.
 * Messages are serializable in order to be able to get a binary buffer to send to the network
 */
public class Message implements Serializable, ByteArrayable {

    private static final long serialVersionUID = 8073741970285089526L;

    /**
     * Type of message
     */
    private MessageType messageType;

    /**
     * Type of machine (service, linker or client)
     */
    private MachineType machineType; // sender

    /**
     * Message payload
     */
    private byte[] payload;

    public Message(MessageType messageType, MachineType machineType, byte[] message) {
        this.messageType = messageType;
        this.machineType = machineType;
        this.payload = message;
    }

    public byte[] getPayload() {
        return payload;
    }

    public MachineType getMachineType() {
        return machineType;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public static Message fromByteArray(final byte[] bytes) throws IOException, ClassNotFoundException {
        return (Message) ByteArrayable.fromByteArray(bytes);
    }

    @Override
    public String toString() {
        return "Message{" +
                "messageType=" + messageType +
                ", machineType=" + machineType +
                ", payload=" + Arrays.toString(payload) +
                '}';
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }
}
