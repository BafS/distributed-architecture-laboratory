package messages;

public enum MessageType {

    REGISTER((byte) 0x00),
    TIME((byte) 0x10);
    //ACK((byte) 3, Byte.BYTES);

    private byte type;

    MessageType(final byte b) {
        this.type = b;
    }

    public byte getType() {
        return type;
    }

    public static MessageType fromByte(byte type) {
        for (MessageType messageType : MessageType.values()) {
            if (type == messageType.getType()) {
                return messageType;
            }
        }

        return null;
    }
}
