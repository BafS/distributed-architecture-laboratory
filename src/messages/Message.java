package messages;

import util.ByteArrayable;
import util.MachineType;

import java.io.*;
import java.util.Arrays;

/**
 * Message to communicate between instances on the network.
 */
public class Message implements Serializable, ByteArrayable {

    private MessageType messageType;
    private MachineType machineType; // sender
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
}
