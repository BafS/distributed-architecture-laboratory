package services;

import util.Machine;
import util.MachineType;

import java.util.List;

public class ServiceReply extends Service {
    public ServiceReply(List<Machine> linkers, final int port) {
        super(linkers, port);
    }

    @Override
    byte[] getResponse(byte[] message) {
        return new byte[0];
    }

    @Override
    MachineType getServiceType() {
        return MachineType.SERVICE_REPLY;
    }

    @Override
    Service getServiceObject() {
        return this;
    }
}
