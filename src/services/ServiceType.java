package services;

/**
 * All the services types
 */
public enum ServiceType {
    SERVICE_TIME,
    SERVICE_SUM,
    SERVICE_REPLY;

    public final byte getType() {
        return (byte) this.ordinal();
    }
}
