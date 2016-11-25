package services;

import util.Machine;
import util.MachineType;

import java.nio.ByteBuffer;
import java.util.List;

public class ServiceTime extends Service {
    public ServiceTime(List<Machine> linkers, final int port) {
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
}
