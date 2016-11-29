package services;

/**
 * @author Henrik Akesson
 * @author Fabien Salathe
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
