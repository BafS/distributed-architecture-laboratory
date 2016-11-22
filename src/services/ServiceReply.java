package services;

import messages.Message;
import util.Machine;

import java.util.List;

public class ServiceReply extends Service {
    public ServiceReply(List<Machine> linkers, final int port) {
        super(linkers, port);
    }

    @Override
    byte[] getResponse() {
        return null; // TODO
    }

    @Override
    byte getServiceType() {
        return Message.TIME; // TODO
    }
}
