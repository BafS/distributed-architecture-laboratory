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
        // Reply in L33T 5P34K
        return new String(message.getPayload())
                .toUpperCase()
                .replace("O", "0")
                .replace("A", "4")
                .replace("I", "1")
                .replace("S", "5")
                .replace("E", "3")
                .getBytes();
    }

    @Override
    ServiceType getServiceType() {
        return ServiceType.SERVICE_REPLY;
    }

    public static String getResponseFromByteArray(byte[] response) {
        return new String(response);
    }
}
