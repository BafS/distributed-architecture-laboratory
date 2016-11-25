package services;

import messages.Message;
import messages.MessageType;
import util.MachineAddress;
import util.MachineType;

import java.io.EOFException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

/**
 * At initialization, a service will register himself to a random linker (sending him the host,
 * port and type of the service).
 * Then, the service will just listen for new messages and reply.
 *
 * Multiple types of services exists.
 * Automatically restart on error.
 *
 * Handshake:
 * Service --(register)--> Linker
 *         <----(ack)-----
 */
public abstract class Service {

    private final DatagramSocket socket;

    private List<MachineAddress> linkers;

    private int timeout = 500;

    public Service(List<MachineAddress> linkers, final int port) throws SocketException {
        this.linkers = linkers;
        this.socket = new DatagramSocket(port);
    }

    /**
     * At initialization, service need to register himself to one of the linkers
     */
    boolean handshake() throws IOException {
//        System.out.println("[i] Linkers:");
//        linkers.forEach(System.out::println);

        // Use a random linker in the list
        MachineAddress linker = linkers.get((int) (Math.random() * linkers.size()));
        System.out.println("[i] Selected linker: " + linker);

        // We send a REGISTER_SERVICE with the service type
        byte[] buff = new byte[512];
        DatagramPacket packet = new DatagramPacket(buff, buff.length, linker.getAddress(), linker.getPort());
        packet.setData(new Message(
                MessageType.REGISTER_SERVICE,
                MachineType.SERVICE,
                new byte[]{ this.getServiceType().getType() }
        ).toByteArray());

        socket.send(packet);

        // Step 2 - We need the ACK packet //

//        packet.setLength(buff.length);
        socket.setSoTimeout(timeout);
        Message message;

        packet = new DatagramPacket(buff, buff.length);

        try {
            socket.receive(packet);

            // Continue, even if the packet is corrupt and cannot be unserialized
            try {
                message = Message.fromByteArray(buff);
            } catch (EOFException e) {
                System.out.println(e.getStackTrace());
                return handshake();
            }

            if (message.getMessageType() == MessageType.ACK) {
                System.out.println("[i] Handshake ok");
                return true;
            }
        } catch(SocketTimeoutException | ClassNotFoundException e) {
            System.out.println("[i] Timeout (" + timeout + "ms)");
            // We increment the timeout to not saturate the network (in case the network was not 100% safe)
            timeout = Math.min((int) (timeout * 1.25), timeout * 10);
            return handshake();
        }

        return false;
    }

    /**
     * Listen for incoming message
     */
    void listen() throws IOException, ClassNotFoundException {
        socket.setSoTimeout(0);
        byte[] buff = new byte[512];

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
                    System.out.println("> Ask for the service function");

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
            System.out.println("Usage: java service <type> <port> <list of linkers>");
            return;
        }

        List<MachineAddress> linkers = new ArrayList<>();
        String type = args[0];
        int port = Integer.parseInt(args[1]);

        // 127.0.0.1:8080,127.0.0.1:8090
        for (String info : args[2].split(",")) {
            String[] token = info.split(":");
            linkers.add(new MachineAddress(token[0], Integer.parseInt(token[1])));
        }

        Service service;
        if (type.equals("time")) {
            service = new ServiceTime(linkers, port);
        } else {
            service = new ServiceReply(linkers, port);
        }

        try {
            if (service.handshake()) {
                service.listen();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
