package messages;

public enum MessageType {

    // [service type]
    REGISTER_SERVICE((byte) 0x02, Byte.BYTES),

    // [service type]
    REQUEST_SERVICE((byte) 0x3, Byte.BYTES),

    REQUEST_TIME((byte) 0x10, Byte.BYTES),

    REQUEST((byte) 0x11, Byte.BYTES),

    ACK((byte) 0x5),

    RESPONSE((byte) 0x6),

    RESPONSE_TIME((byte) 0x20, Byte.BYTES),

    RESPONSE_REPLY((byte) 0x21)
    ;

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
