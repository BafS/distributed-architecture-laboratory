import messages.Message;
import messages.MessageType;
import services.ServiceType;
import util.MachineAddress;
import util.MachineType;

import java.io.EOFException;
import java.io.IOException;
import java.net.*;
import java.util.*;

/**
 * Linker are the bridge between clients and service
 * Linkers addresses are known from the clients and the services.
 * <p>
 * Linker communicate to clients with messages (Message)
 * <p>
 * When a linker is halt or stopped by error, the linker automatically tries to restart
 * <p>
 * Linker cannot be added after the initialization
 * <p>
 * Client can send
 * REQUEST_SERVICE
 * - Send the service address
 * <p>
 * NOT_RESPONDING_SERVICE
 * - Check if the service is dead and remove the service from the list
 * - If true: Send his table to the other linkers
 */
public class Linker {

    private final DatagramSocket socket;

    private Map<ServiceType, Set<MachineAddress>> services = new HashMap<>();

    public Linker(final int port) throws SocketException {
        socket = new DatagramSocket(port);
    }

    /**
     *
     * @param message
     * @param packet
     * @throws IOException
     */
    private void handleRegisterService(Message message, DatagramPacket packet) throws IOException {
        ServiceType serviceType = ServiceType.values()[message.getPayload()[0]];

        System.out.println("> Register service");

        switch (serviceType) {
            case SERVICE_REPLY:
                System.out.println("  Type: reply");
                break;
            case SERVICE_TIME:
                System.out.println("  Type: time");
                break;
        }

        // If the set is empty, we create the new set
        if (services.isEmpty() || services.get(serviceType).isEmpty()) {
            Set<MachineAddress> set = new HashSet<>();
            services.put(serviceType, set);
        }

        // We add the machine to the set
        services.get(serviceType).add(
                new MachineAddress(
                        packet.getAddress().getHostAddress(),
                        packet.getPort()
                ));

        System.out.println("[i] Services:");
        printServices();

        // Send an ACK to show that the linker is alive
        packet.setData(new Message(
                MessageType.ACK,
                MachineType.LINKER,
                null
        ).toByteArray());

        socket.send(packet);
    }

    /**
     *
     * @param message
     * @param packet
     * @throws IOException
     */
    private void handleRequestService(Message message, DatagramPacket packet) throws IOException {
        ServiceType serviceType = ServiceType.values()[message.getPayload()[0]];

        if (!services.isEmpty()) {
            System.out.println("> A client asked for a service");
            if (services.containsKey(serviceType)) {
                System.out.println(serviceType);

                Set<MachineAddress> specificServices = services.get(serviceType);

                System.out.println("[i] There is currently " + specificServices.size() + " services of " + serviceType.name());

                MachineAddress randomService = getAny(specificServices);

                // Send the address of one of the specific service
                packet.setData(new Message(
                        MessageType.RESPONSE,
                        MachineType.LINKER,
                        randomService.toByteArray()
                ).toByteArray());

                System.out.println("[i] Send service address to client");
                socket.send(packet);
            }
        }
    }

    /**
     * Listen for new messages from clients or services
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public void listen() throws IOException, ClassNotFoundException {
        byte[] buff = new byte[512];

        DatagramPacket packet = new DatagramPacket(buff, buff.length);

        Message message;

        // Listen for new messages
        while (true) {
            System.out.println("[i] listen for new messages...");
            socket.receive(packet);

            // Continue, even if the packet is corrupt and cannot be unserialized
            try {
                message = Message.fromByteArray(buff);
            } catch (EOFException e) {
                System.out.println(e.getStackTrace());
                continue;
            }

            // DEBUG
            System.out.println("New message from " + packet.getAddress().getHostName() + ":" + packet.getPort());

            switch (message.getMessageType()) {
                case REGISTER_SERVICE:
                    handleRegisterService(message, packet);
                    break;

                case REQUEST_SERVICE:
                    handleRequestService(message, packet);
                    break;
                default:
                    System.out.println("> Got an unknown message");
            }

            // Reset the length of the packet before reuse
//            packet.setLength(buff.length);
//            packet = new DatagramPacket(buff, buff.length);
        }
    }

    public static MachineAddress getAny(final Set<MachineAddress> services) {
        int num = (int) (Math.random() * services.size());
        for (MachineAddress ma : services) if (--num < 0) return ma;
        throw new AssertionError();
    }

    private void printServices() {
        services.forEach((m, a) -> System.out.println("- " + a));
    }

    /**
     * Run a linker on a specific port from the command line
     *
     * @param args
     */
    public static void main(String... args) {
        System.out.println("- Linker -");

        if (args.length < 1) {
            System.out.println("Usage: java linker <port>");
            return;
        }

        final int port = Integer.parseInt(args[0]);

        try {
            Linker linker = new Linker(port);
            linker.listen();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
