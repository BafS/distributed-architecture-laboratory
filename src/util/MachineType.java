package util;

public enum MachineType {

    CLIENT("client"),
    LINKER("linker"),
    SERVICE_TIME("service_time"),
    SERVICE_REPLY("service_reply")
    ;

    private final String type;

    MachineType(final String serviceType) {
        this.type = serviceType;
    }

    public String getStringType() {
        return type;
    }

    public static MachineType fromString(final String type) {
        for (MachineType messageType : MachineType.values()) {
            if (type == messageType.type) {
                return messageType;
            }
        }

        return null;
    }

    @Override
    public String toString() {
        return "MachineType{" +
                "type='" + type + '\'' +
                '}';
    }
}
