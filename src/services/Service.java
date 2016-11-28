package services;

import messages.Message;
import messages.MessageType;
import util.ConfigReader;
import util.MachineAddress;
import util.MachineType;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
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
        } catch (SocketTimeoutException | ClassNotFoundException e) {
            System.out.println("[i] Timeout (" + timeout + "ms)");
            // We increment the timeout to not saturate the network (in case the network was not 100% safe)
            timeout = Math.min((int) (timeout * 1.25), timeout * 10);
            return handshake();
        }

        return false;
    }

    /**
     *
     * @param message
     * @param packet
     * @throws IOException
     */
    private void handleRequest(Message message, DatagramPacket packet) throws IOException {
        byte[] buffRes = getResponse(message, packet); // polymorphism

        message = new Message(
                MessageType.RESPONSE,
                MachineType.SERVICE,
                buffRes
        );

        System.out.println(message);

        packet.setData(message.toByteArray());

        socket.send(packet);
    }

    /**
     * Listen for incoming message
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    void listen() throws IOException, ClassNotFoundException {
        socket.setSoTimeout(0);
        byte[] buff = new byte[512];

        DatagramPacket packet;

        Message message;

        System.out.println("[i] Listen for new messages...");

        // Listen for new messages
        while (true) {
            // Reset the packet
            packet = new DatagramPacket(buff, buff.length);
            socket.receive(packet);

            message = Message.fromByteArray(buff);

            // DEBUG
            System.out.println("New message [" + packet.getAddress().getHostName() + ":" + packet.getPort() + "]");
            System.out.println(message);

            switch (message.getMessageType()) {
                case REQUEST:
                    System.out.println("> Ask for the service function");

                    handleRequest(message, packet);
                    break;
                default:
                    System.out.println("> Unknown message");
            }

            // Reset the length of the packet before reuse
//            packet.setLength(buff.length);
        }
    }

    abstract byte[] getResponse(Message message, DatagramPacket packet);

    abstract ServiceType getServiceType();

    public static void main(String[] args) {
        System.out.println("- Service -");

        // TODO -> split main in each service ?

        if (args.length < 2) {
            System.out.println("Usage: java service <type> <port>");
            return;
        }

        final String type = args[0];
        final int port = Integer.parseInt(args[1]);

        Service service;
        try {
            List<MachineAddress> linkers = ConfigReader.read(new File("linkers.txt")); // TODO file name: shared const

            if (type.equals("time")) {
                service = new ServiceTime(linkers, port);
            } else if (type.equals("reply")) {
                service = new ServiceReply(linkers, port);
            } else {
                service = new ServiceSum(linkers, port);
            }

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
