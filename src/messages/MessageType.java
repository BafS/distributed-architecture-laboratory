package messages;

public enum MessageType {

    REGISTER_SERVICE((byte) 0x00),
    REGISTER_CLIENT((byte) 0x01),
    ACK_TIME((byte) 0x10),
    RESPONSE_TIME((byte) 0x20),
    ;
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
