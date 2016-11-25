package util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

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

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public MachineType getMachineType() {
        return type;
    }

    public static Machine fromByteArray(final byte[] machineBytes) {
        byte port = machineBytes[0];
        byte[] data = Arrays.copyOfRange(machineBytes, 1, machineBytes.length);

        return new Machine(null, data.toString(), (int) port);
    }

    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(port);
        outputStream.write(host.getBytes());


        return outputStream.toByteArray();
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
