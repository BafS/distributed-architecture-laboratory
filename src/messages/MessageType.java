package messages;

public enum MessageType {

    REGISTER_SERVICE_REPLY((byte) 0x00),
    REGISTER_SERVICE_TIME((byte) 0x01),
    REGISTER_CLIENT((byte) 0x01),
    ACK_SERVICE((byte) 0x8),
    ACK_TIME((byte) 0x10),
    ACK_REPLY((byte) 0x11),
    RESPONSE_TIME((byte) 0x20),
    RESPONSE_REPLY((byte) 0x20),
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
            if (type == messageType.type) {
                return messageType;
            }
        }

        return null;
    }
}
