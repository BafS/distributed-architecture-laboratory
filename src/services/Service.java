package services;

import messages.Message;
import messages.MessageType;
import util.MachineAddress;
import util.MachineType;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
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

    private final int PORT;

    private List<MachineAddress> linkers;

    public Service(List<MachineAddress> linkers, final int port) {
        this.linkers = linkers;
        this.PORT = port;
    }

    /**
     * At initialization, service need to register himself to one of the linkers
     */
    int subscribeToLinker() throws IOException {
        linkers.forEach(System.out::println);

        // Use a random linker in the list
        MachineAddress linker = linkers.get((int) Math.random() * linkers.size());

        Message message = new Message(
                MessageType.REGISTER_SERVICE,
                MachineType.SERVICE,
                new byte[]{ this.getServiceType().getType() }
        );

        System.out.println(message);

        byte[] buff = message.toByteArray();

        DatagramPacket packet = new DatagramPacket(buff, buff.length, linker.getAddress(), linker.getPort());

        DatagramSocket socket = new DatagramSocket(PORT);

        socket.send(packet);
        socket.close();

        return socket.getPort();
    }

    /**
     * Listen for incoming message
     */
    void listen() throws IOException, ClassNotFoundException {
        DatagramSocket socket = new DatagramSocket(PORT);

        byte[] buff = new byte[Long.BYTES];

        DatagramPacket packet = new DatagramPacket(buff, buff.length);

        Message message;

        System.out.println("[i] Listen for new messages...");

        // Listen for new messages
        while (true) {
            socket.receive(packet);

            message = Message.fromByteArray(buff);

            // DEBUG
            System.out.println("New message from " + packet.getAddress().getHostName() + ":" + packet.getPort());
            System.out.println("(message: " + message.getPayload() + ")");

            switch (message.getMessageType()) {
                case REQUEST_TIME:
                    System.out.println(">>> ASK FOR THE SERVICE");

//                    Service concretService = getServiceObject();
//                    this.get

//                    buff = this.getResponse(message.getPayload()); // polymorphism
//                    message = new Message(type, buff);
//                    sendMessage(packet, message);
                    break;
                default:
                    System.out.println(">>> Unknown message");
            }

            // Reset the length of the packet before reuse
            packet.setLength(buff.length);
        }
    }

    abstract byte[] getResponse(final byte[] message);

    abstract ServiceType getServiceType();

    /**
     * Send a message
     *
     * @param packet
     * @param message
     */
    private void sendMessage(DatagramPacket packet, Message message) throws IOException {
        DatagramPacket packetToSend = new DatagramPacket(
                message.getPayload(), message.getPayload().length, packet.getAddress(), packet.getPort());

        DatagramSocket ds = new DatagramSocket();
        ds.send(packetToSend);
        ds.close();
    }

    public static void main(String[] args) {
        System.out.println("- Service -");

        // TODO -> split main in each service ?

        if (args.length < 2) {
//            System.out.println("Usage: java service <type> <port> <list of linkers>");
            System.out.println("Usage: java service <type> <list of linkers>");
            return;
        }

        List<MachineAddress> linkers = new ArrayList<>();
        String type = args[0];

        // 127.0.0.1:8080,127.0.0.1:8090
        for (String info : args[1].split(",")) {
            String[] token = info.split(":");
            linkers.add(new MachineAddress(token[0], Integer.parseInt(token[1])));
        }

        Service service;
//        if (type.equals("reply"))
        service = new ServiceTime(linkers, 9911);

        try {
            int port = service.subscribeToLinker();
            service.listen();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
