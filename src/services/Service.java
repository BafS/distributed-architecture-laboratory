package services;

import util.Machine;

import java.util.ArrayList;
import java.util.List;

/**
 * At initialization, a service will register himself to a random linker (sending him the host,
 * port and type of the service).
 * Then, the service will just listen for new messages and reply.
 *
 * Multiple types of services exists.
 * Automatically restart on error.
 */
public abstract class Service {
    private List<Machine> linkers;

    public Service(List<Machine> linkers) {
        this.linkers = linkers;
    }

    /**
     * At initialization, service need to register himself to one of the linkers
     */
    void subscribeToLinker() {
        // Use a random linker in the list
        Machine linker = linkers.get((int) Math.random() * linkers.size());

//        DatagramSocket ds = new DatagramSocket();
//        ds.send(new DatagramPacket(new MessageServiceRegistration(type, host, port)));
    }

    /**
     * Listen for incoming message
     */
    void listen() {

    }

    abstract void sendResponse();

    public static void main(String[] args) {
        System.out.println("- Service -");

        if (args.length < 2) {
            System.out.println("Usage: java service <type> <list of linkers>");
            return;
        }

        List<Machine> linkers = new ArrayList<>();
        String type = args[1];

        // 127.0.0.1:8080|127.0.0.1:8090
        for (String info : args[2].split("|")) {
            String[] token = info.split(":");
            linkers.add(new Machine(token[0], Integer.parseInt(token[1])));
        }

        Service service;
//        if (type.equals("reply"))
        service = new ServiceReply(linkers);
        service.subscribeToLinker();
        service.listen();
    }
}
