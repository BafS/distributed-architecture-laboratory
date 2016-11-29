package util;

/**
 * @author Henrik Akesson
 * @author Fabien Salathe
 *
 * Enum specifying the machine from which a message is sent
 */
public enum MachineType {

    CLIENT((byte) 0x00),
    LINKER((byte) 0x01),
    SERVICE((byte) 0x02),
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
