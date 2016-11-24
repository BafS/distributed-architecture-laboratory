package util;

import java.net.DatagramPacket;

/**
 * Represent a network point with a specific host and port
 */
public class Machine {

    private MachineType type;
    private final String host;
    private final int port;

    public Machine(MachineType type, final String host, final int port) {
        this.type = type;
        this.host = host;
        this.port = port;
    }

    public Machine(MachineType type, DatagramPacket packet) {
        this.type = type;
        this.host = packet.getAddress().getHostName();
        this.port = packet.getPort();
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public MachineType getType() {
        return type;
    }

    @Override
    public String toString() {
        return "Machine{" +
                "type=" + type +
                ", host='" + host + '\'' +
                ", port=" + port +
                '}';
    }
}
