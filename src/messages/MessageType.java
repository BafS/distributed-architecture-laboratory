package messages;

/**
 * Type of possible messages
 */
public enum MessageType {
    PING,
    PONG,
    REGISTER_SERVICE,
    REQUEST_SERVICE,
    REQUEST,
    ACK,
    RESPONSE,
    SERVICE_DOWN,
    REMOVE_SERVICE,
    REGISTER_SERVICE_FROM_LINKER,
    REQUEST_LINKERS_TABLE,
    LINKERS_TABLE
}
