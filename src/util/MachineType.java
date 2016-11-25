package util;

public enum MachineType {

    CLIENT((byte) 0x00),
    LINKER((byte) 0x01),
    SERVICE_TIME((byte) 0x10),
    SERVICE_REPLY((byte) 0x11)
    ;

    private final byte type;

    MachineType(final byte serviceType) {
        this.type = serviceType;
    }

    public final byte getType() {
        return type;
    }

    public static MachineType fromByte(final byte type) {
        for (MachineType messageType : MachineType.values()) {
            if (type == messageType.type) {
                return messageType;
            }
        }

        System.out.println("FAIL");

        return null;
    }

    public final boolean equals(MachineType other) {
        return type == other.type;
    }
}
