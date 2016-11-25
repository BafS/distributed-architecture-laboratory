import messages.Message;
import messages.MessageType;
import services.ServiceType;
import util.MachineAddress;
import util.MachineType;

import java.io.IOException;
import java.net.*;
import java.util.*;


/**
 * Linker are the bridge between clients and service
 * Linkers addresses are known from the clients and the services.
 *
 * Linker communicate to clients with messages (Message)
 *
 * When a linker is halt or stopped by error, the linker automatically tries to restart
 *
 * Linker cannot be added after the initialization
 */
public class Linker {

    private int PORT;

    private Map<ServiceType, Set<MachineAddress>> services = new HashMap<>();

    public Linker(final int port) {
        PORT = port;
    }

    private void handleServiceRegistration(Message message) {
    }

//    private void handleClients(MachineType) {
//
//    }
//
//    private void handleService() {
//
//    }

    /**
     * Listen for new messages from clients or services
     *
     * @throws IOException
     */
    public void listen() throws IOException, ClassNotFoundException {
        DatagramSocket socket = new DatagramSocket(PORT);

        byte[] buff = new byte[512];

        DatagramPacket packet = new DatagramPacket(buff, buff.length);

        // Listen for new messages
        while (true) {
            socket.receive(packet);

            Message message = Message.fromByteArray(buff);

            byte[] payload = message.getPayload();

            // DEBUG
            System.out.println("New message from " + packet.getAddress().getHostName() + ":" + packet.getPort());

            ServiceType serviceType = ServiceType.values()[payload[0]];

            switch (message.getMessageType()) {
                case REGISTER_SERVICE:
                    System.out.println("> Register service");

                    switch (serviceType) {
                        case SERVICE_REPLY:
                            System.out.println("  Type: reply");
                            break;
                        case SERVICE_TIME:
                            System.out.println("  Type: time");
                            break;
                    }

                    // TODO check for doubles
                    if (services.isEmpty() || services.get(serviceType).isEmpty()) {
                        Set<MachineAddress> set = new HashSet<>();
                        services.put(serviceType, set);
                    }

//                        HashSet<MachineAddress> set = new HashSet<>(packet.getSocketAddress());
//                        services.put(serviceType, set);
//                        services.get(serviceType).add(packet.getSocketAddress());
                    services.get(serviceType).add(new MachineAddress(packet.getAddress().getHostAddress(), packet.getPort()));

                    System.out.println("[i] Services:");
                    printServices();

                    break;

                case REQUEST_SERVICE:
                    if (!services.isEmpty()) {
                        System.out.println("> A client asked for a service");
                        if (services.containsKey(serviceType)) {
                            System.out.println(serviceType);

                            Set<MachineAddress> specificServices = services.get(serviceType);

                            System.out.println("[i] There is currently " + specificServices.size() + " services of " + serviceType.name());

                            packet.setLength(buff.length);

                            MachineAddress randomService = getAny(specificServices);

                            // Send the address of one of the specific service
                            buff = new Message(
                                    MessageType.RESPONSE,
                                    MachineType.LINKER,
                                    randomService.toByteArray()
                            ).toByteArray();

                            packet.setData(buff);

                            System.out.println("Send machine to client");
                            socket.send(packet);
//                            socket.
//                            socket.close();
                        }
                    }

                    break;
                default:
                    System.out.println(">>> Unknown message");
            }

            // Reset the length of the packet before reuse
            packet.setLength(buff.length);
        }
    }

    public static MachineAddress getAny(Set<MachineAddress> coll) {
        int num = (int) (Math.random() * coll.size());
        for(MachineAddress ma: coll) if (--num < 0) return ma;
        throw new AssertionError();
    }

    private void send(DatagramPacket packet, Message m) throws IOException {
        byte[] buff = m.toByteArray();

        packet.setLength(buff.length);

        DatagramSocket socket = new DatagramSocket(PORT);
        socket.send(packet);
        socket.close();
    }

//    private Optional<Machine> getService(final MachineType type) {
//        return this.services.stream().parallel().filter(m -> m.getMachineType() == type).findAny();
//    }

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
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
