package util;

import java.io.*;
import java.net.InetSocketAddress;

/**
 * @author Henrik Akesson
 * @author Fabien Salathe
 *
 * Wrapper for machine addresses
 */
public class MachineAddress extends InetSocketAddress implements Serializable, ByteArrayable {

    private static final long serialVersionUID = -2525112949161709437L;

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
