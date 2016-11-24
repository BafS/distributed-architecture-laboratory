package services;

import messages.MessageType;
import util.Machine;

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
    MessageType getServiceType() {
        return MessageType.RESPONSE_REPLY;
    }
}
