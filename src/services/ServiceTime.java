package services;

import messages.Message;
import util.MachineAddress;

import java.net.DatagramPacket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Calendar;
import java.util.List;

public class ServiceTime extends Service {
    public ServiceTime(List<MachineAddress> linkers, final int port) throws SocketException {
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
    byte[] getResponse(Message message, DatagramPacket packet) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(Calendar.getInstance().getTimeInMillis());
        return buffer.array();
    }

    public static Long getResponseFromByteArray(byte[] response) {
        ByteBuffer bb = ByteBuffer.wrap(response);
        return bb.getLong();
    }
}
