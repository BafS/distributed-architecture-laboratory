package services;

public enum ServiceType {

    SERVICE_TIME("time"),
    SERVICE_REPLY("reply");

    private final String type;

    ServiceType(final String type) {
        this.type = type;
    }

    public final String getType() {
        return type;
    }

    public static ServiceType fromString(String type) {
        for (ServiceType messageType : ServiceType.values()) {
            if (type.equals(messageType.type)) {
                return messageType;
            }
        }

        return null;
    }
}
