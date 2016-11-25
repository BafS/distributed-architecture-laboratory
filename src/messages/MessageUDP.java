package messages;

import util.MachineType;

import java.io.*;
import java.util.Arrays;

/**
 * Message to communicate between instances on the network.
 */
public class MessageUDP implements Serializable {
    private static final long serialVersionUID = -5399605122490343339L;

    private MessageType messageType;
    private MachineType machineType;
    private byte[] message;

    public MessageUDP(MessageType messageType, MachineType machineType, byte[] message) {
        this.messageType = messageType;
        this.machineType = machineType;
        this.message = message;
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

    public byte[] getMessage() {
        return message;
    }

    public MachineType getMachineType() {
        return machineType;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public static MessageUDP fromByteArray(final byte[] bytes) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
            try (ObjectInputStream ois = new ObjectInputStream(bais)) {
                MessageUDP o1 = (MessageUDP) ois.readObject();
                ois.close();
                return o1;
            }
        }
    }

    @Override
    public String toString() {
        return "MessageUDP{" +
                "messageType=" + messageType +
                ", machineType=" + machineType +
                ", message=" + Arrays.toString(message) +
                '}';
    }
}
