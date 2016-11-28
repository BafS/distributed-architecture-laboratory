import messages.Message;
import messages.MessageType;
import services.ServiceType;
import util.ConfigReader;
import util.MachineAddress;
import util.MachineType;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.*;

/**
 * Linkers are the bridge between clients and services
 * Linker addresses are known by all clients and services.
 * <p>
 * Linkers communicate to clients with messages (Message)
 * <p>
 * When a linker is halted or stopped by error, the linker automatically tries to restart
 * <p>
 * Linkers cannot be added after the initialization
 * <p>
 * Clients can send
 * REQUEST_SERVICE
 * - Send the service address
 * <p>
 * NOT_RESPONDING_SERVICE
 * - Checks if the service is dead and if so removes the service from the list
 * - If true: Send his updated table to the other linkers
 */
public class Linker {

    /**
     * UDP socket that will be used to send and receive messages
     */
    private final DatagramSocket socket;

    /**
     * List of all linkers (except itself)
     */
    private List<MachineAddress> linkers;

    /**
     * Set of machines for each type of service
     */
    private Map<ServiceType, Set<MachineAddress>> services = new HashMap<>();

    public Linker(final int port, List<MachineAddress> otherLinkers) throws SocketException {
        socket = new DatagramSocket(port);
        this.linkers = otherLinkers;
    }

    /**
     * Handle register service request from a service
     *
     * @param message
     * @param packet
     * @throws IOException
     */
    private void handleFirstRegisterService(Message message, DatagramPacket packet) throws IOException {
        handleRegisterService(message, packet);

        ServiceType serviceType = ServiceType.values()[message.getPayload()[0]];

        // Send an ACK to show that the linker is alive
        packet.setData(new Message(
                MessageType.ACK,
                MachineType.LINKER,
                null
        ).toByteArray());

        socket.send(packet);

        MachineAddress newService = new MachineAddress(
                packet.getAddress().getHostAddress(),
                packet.getPort()
        );

        // Concat [service type | service object]
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
        outputStream.write( serviceType.getType() );
        outputStream.write( newService.toByteArray() );

        // Send service to other linkers
        sendForEachOtherLinker(
                new Message(
                        MessageType.REGISTER_SERVICE,
                        MachineType.LINKER,
                        outputStream.toByteArray()
                ),
                packet
        );
    }

    /**
     * Handle register service when it comes from a linker
     *
     * @param message
     * @param packet
     * @throws IOException
     */
    private void handleLinkerRegisterService(Message message, DatagramPacket packet) throws IOException {
        try {
            // We read the [service type | machine address]
            byte[] buff = new byte[message.getPayload().length - 1];
            System.arraycopy(message.getPayload(), 1, buff, 0, message.getPayload().length - 1);
            MachineAddress ma = MachineAddress.fromByteArray(buff);
            packet.setAddress(ma.getAddress());
            packet.setPort(ma.getPort());

            ServiceType serviceType = ServiceType.values()[message.getPayload()[0]];

            // We rebuild the payload message just with the type
            // (because the address is now used in the socket, it's to avoid duplication)
            message.setPayload(
                    new byte[]{ serviceType.getType() }
            );
            handleRegisterService(message, packet);
        } catch (ClassNotFoundException e) {
            System.out.println("[i] Message malformed");
        }
    }

    /**
     * Handle service, used by both handleLinkerRegisterService and handleFirstRegisterService methods
     *
     * @param message
     * @param packet
     * @throws IOException
     */
    private void handleRegisterService(Message message, DatagramPacket packet) throws IOException {
        ServiceType serviceType = ServiceType.values()[message.getPayload()[0]];

        System.out.println("[i] Register service (" + serviceType + ")");

        // If the set is empty, we create the new set
        if (!services.containsKey(serviceType)) {
            Set<MachineAddress> set = new HashSet<>();
            services.put(serviceType, set);

        }

        // We add the machine to the set
        MachineAddress newService = new MachineAddress(
                packet.getAddress().getHostAddress(),
                packet.getPort()
        );
        services.get(serviceType).add(newService);
    }

    /**
     * Handle request service request
     *
     * @param message
     * @param packet
     * @throws IOException
     */
    private void handleRequestService(Message message, DatagramPacket packet) throws IOException {
        ServiceType serviceType = ServiceType.values()[message.getPayload()[0]];

        if (!services.isEmpty()) {
            System.out.println("[>] A client asked for a service (" + serviceType.name() + ")");
            if (services.containsKey(serviceType)) {
                System.out.println(serviceType);

                Set<MachineAddress> specificServices = services.get(serviceType);

                System.out.println("[i] There is currently " + specificServices.size() + " services of " + serviceType.name());

                if (specificServices.size() <= 0) {
                    System.out.println("[i] No service available");
                    // We cannot response, the client will ask again soon.
                    return;
                }

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

    private void warnOtherLinkers(MachineAddress serviceDownMachineAddress, Message message, DatagramPacket packet) throws IOException {
        sendForEachOtherLinker(
                new Message(
                    MessageType.REMOVE_SERVICE,
                    MachineType.LINKER,
                        serviceDownMachineAddress.toByteArray()
                ),
                packet
        );
    }

    private void sendForEachOtherLinker(Message message, DatagramPacket packet) throws IOException {
        byte[] buff = new byte[512];

        for (MachineAddress linker : linkers) {
            packet = new DatagramPacket(buff, buff.length, linker.getAddress(), linker.getPort());

            packet.setData(message.toByteArray());

            socket.send(packet);
        }
    }

    private void handleRemoveService(Message message, DatagramPacket packet) {
        try {
            MachineAddress service = MachineAddress.fromByteArray(message.getPayload());
            removeService(service);
            printServices();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Send new list of services to other linkers
     *
     * @param message
     * @param packet
     * @throws IOException
     */
    private void handleServiceDown(Message message, DatagramPacket packet) throws IOException {
        System.out.println("[i] Service down !");

        // Send an ACK to the client to show that we received the request
        packet.setData(new Message(
                MessageType.ACK,
                MachineType.LINKER,
                null
        ).toByteArray());
        socket.send(packet);

        try {
            MachineAddress possibleDeadService = MachineAddress.fromByteArray(message.getPayload());

            byte[] buff = new byte[512];
            DatagramPacket tempPacket = new DatagramPacket(
                    buff, buff.length, possibleDeadService.getAddress(), possibleDeadService.getPort());

            tempPacket.setData(new Message(
                    MessageType.PING,
                    MachineType.LINKER,
                    null
            ).toByteArray());

            try {
                System.out.println("[i] Send a PING to the service");
                socket.send(tempPacket);

                socket.setSoTimeout(1000);
                socket.receive(tempPacket);
            } catch (SocketTimeoutException socketEx) {
                // Service is down
                System.out.println("[i] Service is down indeed");

                removeService(possibleDeadService);

                warnOtherLinkers(possibleDeadService, message, packet);

                socket.setSoTimeout(0);
            }
        } catch (ClassNotFoundException e) {
            System.out.println("[i] Error, invalid packet");

            socket.setSoTimeout(0);
            return;
        }
    }

    /**
     * Remove the given service from the list
     *
     * @param deadService
     */
    private void removeService(MachineAddress deadService) {
        services.forEach((a, list) -> {
            if (list.contains(deadService)) {
                list.remove(deadService);
            }
        });

        System.out.println("[i] Updated services list");
        printServices();
    }

    /**
     * Listen for new messages from clients or services
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public void listen() throws IOException, ClassNotFoundException {
        byte[] buff = new byte[1024];

        DatagramPacket packet;

        Message message;

        // Listen for new messages
        System.out.println("[i] Listen for new messages (on " + socket.getLocalSocketAddress() + ")...");

        while (true) {
            // Reset packet before reuse
            packet = new DatagramPacket(buff, buff.length);
            socket.setSoTimeout(0); // Be sure to listen for ever
            socket.receive(packet);

            // Continue, even if the packet is corrupted and cannot be unserialized
            try {
                message = Message.fromByteArray(buff);
            } catch (EOFException e) {
                System.out.println("[i] Message could not be decoded !");
                e.printStackTrace();
                continue;
            }

            // DEBUG
            System.out.println("New message [" + packet.getAddress().getHostName() + ":" + packet.getPort() + "]");
            System.out.println(message);

            switch (message.getMessageType()) {
                case REGISTER_SERVICE:
                    if (message.getMachineType() == MachineType.LINKER) {
                        handleLinkerRegisterService(message, packet);
                    } else {
                        handleFirstRegisterService(message, packet);
                    }

                    System.out.println("[i] Services:");
                    printServices();

                    break;
                case REQUEST_SERVICE:
                    handleRequestService(message, packet);
                    break;
                case SERVICE_DOWN:
                    handleServiceDown(message, packet);
                    break;
                case REMOVE_SERVICE:
                    handleRemoveService(message, packet);
                    break;
                default:
                    System.out.println("> Got an unknown message !");
            }
        }
    }

    /**
     * Helper to get a random service from the list
     *
     * @param services
     * @return
     */
    public static MachineAddress getAny(final Set<MachineAddress> services) {
        int num = (int) (Math.random() * services.size());
        for (MachineAddress ma : services) if (--num < 0) return ma;
        throw new AssertionError();
    }

    /**
     * Helper to print the list of current services
     */
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
            System.out.println("Usage: java linker <linker id>");
            System.out.println("Note: <linker id> is the line number in linkers.txt");
            return;
        }

        final int id = Integer.parseInt(args[0]);

        try {
            List<MachineAddress> linkers = ConfigReader.read(new File("linkers.txt"));
            MachineAddress config = linkers.get(id);
            linkers.remove(id);

            Linker linker = new Linker(config.getPort(), linkers);
            linker.listen();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
