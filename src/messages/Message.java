package messages;

import util.MachineType;

import java.io.*;
import java.util.Arrays;

/**
 * Message to communicate between instances on the network.
 */
public class Message implements Serializable {

    private MessageType messageType;
    private MachineType machineType; // sender
    private byte[] payload;

    public Message(MessageType messageType, MachineType machineType, byte[] message) {
        this.messageType = messageType;
        this.machineType = machineType;
        this.payload = message;
    }

    public byte[] toByteArray() throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.flush();
                oos.writeObject(this);
                oos.flush();
            }

            return baos.toByteArray();
        }
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
        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
            try (ObjectInputStream ois = new ObjectInputStream(bais)) {
                Message o1 = (Message) ois.readObject();
                ois.close();
                return o1;
            }
        }
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
