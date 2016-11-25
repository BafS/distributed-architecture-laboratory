import messages.MessageType;
import messages.MessageUDP;
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

//    private List<Machine> services = new ArrayList<>();

//    private Map<MachineType, Set<Machine>> services2 = new HashMap<>();

    private Map<MachineType, List<InetSocketAddress>> services3 = new HashMap<>();

    public Linker(final int port) {
        PORT = port;
    }

//    private void onRegistration(Message message) {
//    }

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

            MessageUDP message = MessageUDP.fromByteArray(buff);
            MachineType machineType = message.getMachineType();

            // DEBUG
            System.out.println("New message from " + packet.getAddress().getHostName() + ":" + packet.getPort());
            System.out.println("(message: " + message.getMessage() + ")");

            switch (message.getMessageType()) {
                case REGISTER_SERVICE:
                    System.out.println(">>> REGISTER SERVICE");

                    if (message.getMachineType().equals(MachineType.SERVICE_TIME)) {
                        System.out.println("-> TIME");
                    }
                    if (message.getMachineType().equals(MachineType.SERVICE_REPLY)) {
                        System.out.println("-> REPLY");
                    } else {
                        System.out.println("UNKNOWN MESSAGE TYPE");
                    }

                    // TODO check for doubles
//                    services.add(new Machine(MachineType.SERVICE_REPLY, packet));

                    if (services3.isEmpty() || services3.get(machineType).isEmpty()) {
                        ArrayList<InetSocketAddress> list = new ArrayList<>();
                        services3.put(machineType, list);
                    }

//                        HashSet<InetSocketAddress> set = new HashSet<>(packet.getSocketAddress());
//                        services3.put(machineType, set);
//                        services3.get(machineType).add(packet.getSocketAddress());
                    services3.get(machineType).add(new InetSocketAddress(
                            packet.getAddress(),
                            packet.getPort()
                    ));

                    System.out.println("[i] Services:");
                    printServices();

                    break;
                case ASK_SERVICE:
                    System.out.println(">>> A Client asked for a service");

                    if (machineType == MachineType.SERVICE_TIME) {
                        System.out.println("-> TIME");

                        if (!services3.isEmpty()) {
                            System.out.println("A" + services3.size());

                            if (services3.containsKey(MachineType.SERVICE_TIME)) {
                                System.out.println("B");

                                packet.setLength(buff.length);

                                int index = new Random().nextInt(services3.get(MachineType.SERVICE_TIME).size());
                                System.out.println("C" + index);
                                InetSocketAddress randomService = services3.get(MachineType.SERVICE_TIME).get(index);

                                buff = new MessageUDP(
                                        MessageType.RESPONSE,
                                        MachineType.LINKER,
                                        randomService.getAddress().getAddress()
                                ).toByteArray();

                                packet.setData(buff);

                                socket.send(packet);
                                socket.close();

                                System.out.println("Send machine to client");
                            }
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

    private void send(DatagramPacket packet, MessageUDP m) throws IOException {
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
        services3.forEach((m, a) -> System.out.println(a));
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
