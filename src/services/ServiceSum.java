package services;

import messages.Message;
import util.MachineAddress;

import java.net.DatagramPacket;
import java.net.SocketException;
import java.util.List;

public class ServiceSum extends Service {
    public ServiceSum(List<MachineAddress> linkers, final int port) throws SocketException {
        super(linkers, port);
    }

    @Override
    byte[] getResponse(Message message, DatagramPacket packet) {
        byte[] payload = message.getPayload();
        if (payload != null && payload.length >= 2) {
            return new byte[] {(byte) (payload[0] + payload[1])};
        }

        return new byte[] { 0 };
    }

    @Override
    ServiceType getServiceType() {
        return ServiceType.SERVICE_SUM;
    }

    public static int getResponseFromByteArray(byte[] response) {
        return (int) response[0];
    }
}
