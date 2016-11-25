package util;

import java.io.*;
import java.net.InetSocketAddress;

// Wrapper
public class MachineAddress extends InetSocketAddress implements Serializable, ByteArrayable {

    public MachineAddress(String hostname, int port) {
        super(hostname, port);
    }

    public MachineAddress(InetSocketAddress isa) {
        super(isa.getAddress(), isa.getPort());
    }

    @Override
    public String toString() {
        return super.toString();
    }

    public static MachineAddress fromByteArray(final byte[] bytes) throws IOException, ClassNotFoundException {
        return (MachineAddress) ByteArrayable.fromByteArray(bytes);
    }
}
