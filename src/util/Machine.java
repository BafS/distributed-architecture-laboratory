package util;

import java.net.DatagramPacket;

/**
 * Represent a network point with a specific host and port
 */
public class Machine {
    private String host;
    private int port;

    public Machine(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public Machine(DatagramPacket packet) {
        this.host = packet.getAddress().getHostName();
        this.port = packet.getPort();
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        return "Machine{" +
                "host='" + host + '\'' +
                ", port=" + port +
                '}';
    }
}
