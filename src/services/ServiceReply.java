package services;

import util.MachineAddress;

import java.util.List;

public class ServiceReply extends Service {
    public ServiceReply(List<MachineAddress> linkers, final int port) {
        super(linkers, port);
    }

    @Override
    byte[] getResponse(byte[] message) {
        return new byte[0];
    }

    @Override
    ServiceType getServiceType() {
        return ServiceType.SERVICE_REPLY;
    }
}
