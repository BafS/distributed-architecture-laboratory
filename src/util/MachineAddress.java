package util;

import java.io.*;
import java.net.InetSocketAddress;

// Wrapper
public class MachineAddress implements Serializable {
    private InetSocketAddress address;

    public MachineAddress(InetSocketAddress address) {
        this.address = address;
    }

    public MachineAddress(String host, int port) {
        this.address = new InetSocketAddress(host, port);
    }

    public int getPort() {
        return address.getPort();
    }

    public String getHostName() {
        return address.getHostName();
    }

    @Override
    public String toString() {
        return address.toString();
    }

    public byte[] toByteArray() throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.flush();
                oos.writeObject(address);
                oos.flush();
            }

            return baos.toByteArray();
        }
    }

    public static MachineAddress fromByteArray(final byte[] bytes) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
            try (ObjectInputStream ois = new ObjectInputStream(bais)) {
                InetSocketAddress o1 = (InetSocketAddress) ois.readObject();
                ois.close();
                return new MachineAddress(o1);
            }
        }
    }

    public InetSocketAddress getAddress() {
        return address;
    }
}
