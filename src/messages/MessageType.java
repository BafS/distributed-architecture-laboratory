package messages;

public enum MessageType {

    //    REGISTER_SERVICE((byte) 0x00, Byte.BYTES),
//    REGISTER_CLIENT((byte) 0x01),
    REGISTER_SERVICE((byte) 0x02, Byte.BYTES),
    ASK_SERVICE((byte) 0x3, Byte.BYTES),
    ACK((byte) 0x5),
    RESPONSE((byte) 0x6),
    //    ACK_SERVICE((byte) 0x8),
//    ACK_TIME((byte) 0x10),
//    ACK_REPLY((byte) 0x11),
    RESPONSE_TIME((byte) 0x20, Byte.BYTES),
    RESPONSE_REPLY((byte) 0x20)
    ;
    //ACK((byte) 3, Byte.BYTES);

    private byte type;
    private final int size;

    MessageType(final byte b, final int size) {
        this.type = b;
        this.size = size;
    }

    MessageType(final byte b) {
        this(b, 0);
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
