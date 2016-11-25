package services;

import util.MachineAddress;

import java.nio.ByteBuffer;
import java.util.List;

public class ServiceTime extends Service {
    public ServiceTime(List<MachineAddress> linkers, final int port) {
        super(linkers, port);
    }

    @Override
    ServiceType getServiceType() {
        return ServiceType.SERVICE_TIME;
    }

    /**
     * Get system time
     *
     * @return byte[]
     */
    @Override
    byte[] getResponse(byte[] message) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(System.nanoTime());
        return buffer.array();
    }

    public static Long getResponseFromByteArray(byte[] response) {
        ByteBuffer bb = ByteBuffer.wrap(response);
        return bb.getLong();
    }
}
