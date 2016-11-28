import messages.Message;
import messages.MessageType;
import services.ServiceType;
import util.ConfigReader;
import util.MachineAddress;
import util.MachineType;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
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

    private List<MachineAddress> linkers;

    public Linker(final int port) throws SocketException {
        socket = new DatagramSocket(port);
        linkers = new LinkedList<>();

        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream("linkers.txt"));
        } catch (IOException e) {

        }

        for (String key : properties.stringPropertyNames()) {
            String value = properties.getProperty(key);
            if (Integer.parseInt(value) != port) // A linker shouldn't have itself in it's list
                linkers.add(new MachineAddress("localhost", Integer.parseInt(value)));
        }
    }

    /**
     * Handle register service request
     *
     * @param message
     * @param packet
     * @throws IOException
     */
    private void handleRegisterService(Message message, DatagramPacket packet) throws IOException {
        ServiceType serviceType = ServiceType.values()[message.getPayload()[0]];

        System.out.println("> Register service (" + serviceType + ")");

        // If the set is empty, we create the new set
        if (!services.containsKey(serviceType)) {
            Set<MachineAddress> set = new HashSet<>();
            services.put(serviceType, set);

        }
        String serviceHost = packet.getAddress().getHostAddress();
        int servicePort = packet.getPort();
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
        // Send service to other linkers
        byte[] buff = new byte[512];
        for (MachineAddress linkerAddress : linkers) {
            DatagramPacket linkerPacket = new DatagramPacket(buff, buff.length, linkerAddress.getAddress(), linkerAddress.getPort());
            linkerPacket.setData(new Message(
                    MessageType.REGISTER_SERVICE_FROM_LINKER,
                    MachineType.LINKER,
                    new String(serviceHost + servicePort).getBytes() // THIS IS WRONG, SHOULD SEND SERVICETYPE + SERVICEADDRESS
            ).toByteArray()
            );
            socket.send(linkerPacket);
        }

    }


    private void addService(Message message, DatagramPacket packet) {
        ServiceType serviceType = ServiceType.values()[message.getPayload()[0]];

        // If the set is empty, we create the new set
        if (!services.containsKey(serviceType)) {
            Set<MachineAddress> set = new HashSet<>();
            services.put(serviceType, set);

        }

        // We add the machine to the set
        services.get(serviceType).add(
                new MachineAddress(
                        packet.getAddress().getHostAddress(),
                        packet.getPort()
                ));
        printServices();
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
     * @param message
     * @param packet
     * @throws IOException
     */
    private void handleServiceDown(Message message, DatagramPacket packet) throws IOException {
        System.out.println("DATA = " + new String(message.getPayload()));
        String mess = new String(message.getPayload());
        String host = mess.split(":")[0].substring(1);
        int port = new Integer(mess.split(":")[1]);
        MachineAddress machineAddress = new MachineAddress(host, port);
        socket.setSoTimeout(1000);
        byte[] buff = "Are you there?".getBytes();
        DatagramPacket tempDatagramPacket = new DatagramPacket(buff, buff.length, machineAddress.getAddress(), machineAddress.getPort());

        try {
            socket.send(tempDatagramPacket);

            socket.receive(tempDatagramPacket);

        } catch (SocketTimeoutException socketEx) {
            // Service is down
            System.out.println("Service is down indeed");
            warnOtherLinkers(machineAddress, message, packet);
        }
    }

    private void warnOtherLinkers(MachineAddress serviceDownMachineAddress, Message message, DatagramPacket packet) throws IOException {
        byte[] buff = new byte[512];
        for (MachineAddress linkerAddress : linkers) {

            packet = new DatagramPacket(buff, buff.length, linkerAddress.getAddress(), linkerAddress.getPort());

            packet.setData(new Message(
                    MessageType.REMOVE_SERVICE,
                    MachineType.LINKER,
                    serviceDownMachineAddress.toString().getBytes()).toByteArray()
            );

            socket.send(packet);

        }
    }

    private void removeService(Message message, DatagramPacket packet) {
        System.out.println("REMOVE SERVICE");
        String mess = new String(message.getPayload());
        String host = mess.split(":")[0].substring(1);
        int port = new Integer(mess.split(":")[1]);
        System.out.println("Removing service: " + host + ":" + port);
        services.values().remove(new MachineAddress(host, port));
        printServices();
    }

    /**
     * Listen for new messages from clients or services
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public void listen() throws IOException, ClassNotFoundException {
        byte[] buff = new byte[512];

        DatagramPacket packet;

        Message message;

        // Listen for new messages
        while (true) {
            System.out.println("[i] listen for new messages...");

            // Reset packet before reuse
            packet = new DatagramPacket(buff, buff.length);
            socket.receive(packet);

            // Continue, even if the packet is corrupt and cannot be unserialized
            try {
                message = Message.fromByteArray(buff);
            } catch (EOFException e) {
                System.out.println("In catch");
                e.printStackTrace();
                continue;
            }

            // DEBUG
            System.out.println("New message [" + packet.getAddress().getHostName() + ":" + packet.getPort() + "]");
            System.out.println(message);

            switch (message.getMessageType()) {
                case REGISTER_SERVICE:
                    handleRegisterService(message, packet);
                    break;
                case REGISTER_SERVICE_FROM_LINKER:
                    addService(message, packet);
                    break;
                case REQUEST_SERVICE:
                    handleRequestService(message, packet);
                    break;
                case SERVICE_DOWN:
                    handleServiceDown(message, packet);
                    break;
                case REMOVE_SERVICE:
                    removeService(message, packet);
                    break;
                default:
                    System.out.println("> Got an unknown message");
            }
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
            System.out.println("Usage: java linker <linker id>");
            System.out.println("Note: <linker id> is the line number in linkers.txt");
            return;
        }

        final int id = Integer.parseInt(args[0]);

        try {
            List<MachineAddress> linkers = ConfigReader.read(new File("linkers.txt"));
            MachineAddress config = linkers.get(id);

            Linker linker = new Linker(config.getPort());
            linker.listen();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
