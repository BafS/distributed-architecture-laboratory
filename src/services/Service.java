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
 * @author Henrik Akesson
 * @author Fabien Salathe
 * At initialization, a service will register itself to a random linker (sending him the host,
 * port and type of the service).
 * Then, the service listens for new messages and replies.
 *
 * Multiple types of services exist.
 * Automatically restart on error.
 *
 * Handshake:
 * Service --(register)--> Linker
 *         <----(ack)-----
 *
 * Service <--(request)--- Client
 *         ---(response)->
 *
 * Launching Services:
 *    `java Service <type> <port>`
 *    type: Type of service
 *        - "reply"
 *        - "time"
 *        - "sum"
 */

public abstract class Service {

    /**
     * UDP socket that will be used to send and receive messages
     */
    private final DatagramSocket socket;

    /**
     * List of all linkers
     */
    private List<MachineAddress> linkers;

    private int timeout = 500;

    public Service(List<MachineAddress> linkers, final int port) throws SocketException {
        this.linkers = linkers;
        this.socket = new DatagramSocket(port);
    }

    /**
     * At initialization, service need to register itself to one of the linkers
     */
    boolean handshake() throws IOException {
        // Use a random linker in the list
        MachineAddress linker = linkers.get((int) (Math.random() * linkers.size()));
        System.out.println("[i] Selected linker: " + linker);

        // Sends a REGISTER_SERVICE with the service type
        byte[] buff = new byte[512];
        DatagramPacket packet = new DatagramPacket(buff, buff.length, linker.getAddress(), linker.getPort());
        packet.setData(new Message(
                MessageType.REGISTER_SERVICE,
                MachineType.SERVICE,
                new byte[]{ this.getServiceType().getType() }
        ).toByteArray());

        socket.send(packet);

        // Step 2 - Needs the ACK packet from a Linker //

        socket.setSoTimeout(timeout);
        Message message;

        packet = new DatagramPacket(buff, buff.length);

        try {
            socket.receive(packet);

            // Continue, even if the packet is corrupted and cannot be unserialized
            try {
                message = Message.fromByteArray(buff);
            } catch (EOFException e) {
                System.out.println(e.getStackTrace());
                return handshake();
            }

            if (message.getMessageType() == MessageType.ACK
                    && message.getMachineType() == MachineType.LINKER) {
                System.out.println("[i] Handshake ok");
                return true;
            }
        } catch (SocketTimeoutException | ClassNotFoundException e) {
            System.out.println("[i] Timeout (" + timeout + "ms)");
            // Increment the timeout in order to not saturate the network
            timeout = Math.min((int) (timeout * 1.1), timeout * 10);
            return handshake();
        }

        return false;
    }

    /**
     * Handle request message
     * Uses the response of the concrete service and sends it to the client
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
     * Handle ping message
     * Useful to see if the service is still alive
     *
     * @param message
     * @param packet
     * @throws IOException
     */
    private void handlePing(Message message, DatagramPacket packet) throws IOException {
        message = new Message(
                MessageType.PONG,
                MachineType.SERVICE,
                null
        );

        packet.setData(message.toByteArray());
        socket.send(packet);
    }

    /**
     * Listens for incoming messages and dispatches them
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    void listen() throws IOException, ClassNotFoundException {
        socket.setSoTimeout(0);
        byte[] buff = new byte[512];

        System.out.println("[i] Listen for new messages...");

        DatagramPacket packet;
        Message message;

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
                case PONG:
                    handlePing(message, packet);
                    break;
                default:
                    System.out.println("> Unknown message");
            }
        }

        // The socket should be closed at the end
//        socket.close();
    }

    /**
     * Each service has its own response to send and must implement the getResponse method
     *
     * @param message
     * @param packet
     * @return
     */
    abstract byte[] getResponse(Message message, DatagramPacket packet);

    /**
     * To know the type of the concrete service
     *
     * @return
     */
    abstract ServiceType getServiceType();

    public static void main(String[] args) {
        System.out.println("- Service -");

        if (args.length < 2) {
            System.out.println("Usage: java service <type> <port>");
            System.out.println("<type> can be 'sum', 'reply' or 'time'");
            return;
        }

        final String type = args[0].toLowerCase();
        final int port = Integer.parseInt(args[1]);

        Service service;
        try {
            List<MachineAddress> linkers = ConfigReader.read(new File("linkers.txt")); // TODO file name: shared const

            if (type.equals("time")) {
                service = new ServiceTime(linkers, port);
            } else if (type.equals("reply")) {
                service = new ServiceReply(linkers, port);
            } else if (type.equals("sum")) {
                service = new ServiceSum(linkers, port);
            } else {
                System.out.println("Invalid <type> !");
                System.exit(0);
                return;
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
