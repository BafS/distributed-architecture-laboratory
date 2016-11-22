package services;

import messages.Message;
import util.Machine;

import java.nio.ByteBuffer;
import java.util.List;

public class ServiceTime extends Service {
    public ServiceTime(List<Machine> linkers, final int port) {
        super(linkers, port);
    }

    @Override
    public byte getServiceType() {
        return Message.TIME;
    }

    /**
     * Get system time
     *
     * @return byte[]
     */
    @Override
    public byte[] getResponse() {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(System.nanoTime());
        return buffer.array();
    }
}
