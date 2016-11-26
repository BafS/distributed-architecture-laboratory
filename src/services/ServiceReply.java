package services;

import messages.Message;
import util.MachineAddress;

import java.net.DatagramPacket;
import java.net.SocketException;
import java.util.List;

public class ServiceReply extends Service {
    public ServiceReply(List<MachineAddress> linkers, final int port) throws SocketException {
        super(linkers, port);
    }

    @Override
    byte[] getResponse(Message message, DatagramPacket packet) {
        return new byte[0];
    }

    @Override
    ServiceType getServiceType() {
        return ServiceType.SERVICE_REPLY;
    }
}
