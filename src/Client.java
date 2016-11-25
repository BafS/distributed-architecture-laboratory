import messages.Message;
import messages.MessageType;
import services.ServiceTime;
import services.ServiceType;
import util.MachineAddress;
import util.MachineType;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * The client knows the linkers and want to use a service
 *
 * To use a service, the client firstly asks randomly one of the linker to give him the host and
 * port of the service.
 * If the service does not reply, the client will ask again, randomly, a linker.
 *
 * 1. Ask a random linker the address of a specific service [ACK_SERVICE-TYPE]
 * 2a. if OK [ACK_SERVICE | message]
 * 2b. if Error -> goto 1.
 */
public class Client {

    private final int PORT;

    // Type of service wanted
    private final String type;

    private List<MachineAddress> linkers;

    // The connected service
    private MachineAddress service;

    public Client(List<MachineAddress> linkers, final String type, final int port) {
        this.linkers = linkers;
        this.type = type.toLowerCase();
        this.PORT = port;
    }

    // TODO share code with Service
    void subscribeToLinker() throws IOException, ClassNotFoundException {
        linkers.forEach(System.out::println);

        // Use a random linker in the list
        MachineAddress linker = linkers.get((int) Math.random() * linkers.size());

        byte[] payload;
        if (type.equals("time")) {
            payload = new byte[]{
                    ServiceType.SERVICE_TIME.getType()
            };
        } else if (type.equals("reply")) {
            payload = new byte[]{
                    ServiceType.SERVICE_REPLY.getType()
            };
        } else {
            throw new RuntimeException(type + " is not a valid type of service");
        }

        // Request a specific service
        byte[] buff = new Message(MessageType.REQUEST_SERVICE, MachineType.CLIENT, payload).toByteArray();

        DatagramPacket packet = new DatagramPacket(buff, buff.length, linker.getAddress(), linker.getPort());

        DatagramSocket socket = new DatagramSocket(PORT);
        socket.send(packet);

        buff = new byte[1024];
        packet = new DatagramPacket(buff, buff.length, linker.getAddress(), linker.getPort());
//        packet.setLength(buff.length);

        // TODO add timeout

        // Get response
        while (true) {
            socket.receive(packet);

            Message message = Message.fromByteArray(buff);

            if (message.getMessageType() == MessageType.RESPONSE) {
                System.out.println("Get service address");

                service = MachineAddress.fromByteArray(message.getPayload());

                System.out.println(service);

                break;
            }
        }

         socket.close();
    }

    // https://stackoverflow.com/questions/27381021/detect-a-key-press-in-console
    public void keyListener() throws IOException, ClassNotFoundException {
        Scanner keyboard = new Scanner(System.in);
        boolean exit = false;
        while (!exit) {
            System.out.println("Enter command (quit to exit):");
            String input = keyboard.nextLine();
            if (input != null) {
                System.out.println("Your input is : " + input);
                if ("quit".equals(input) || "q".equals(input)) {
                    System.out.println("Exit client");
                    exit = true;
                } else {
                    // TODO send "ask" for the specific service

                    byte[] buff = new byte[128];
                    DatagramPacket packet = new DatagramPacket(buff, buff.length, service.getAddress(), service.getPort());

                    DatagramSocket socket = new DatagramSocket(PORT);
                    socket.send(packet);

                    while (true) {
                        socket.receive(packet);
                        Message message = Message.fromByteArray(buff);

                        if (message.getMessageType() == MessageType.RESPONSE_TIME) {
                            System.out.println("> Get time response");

                            long time = ServiceTime.getResponseFromByteArray(message.getPayload());

                            System.out.println(time);
                            System.out.println(System.nanoTime());

                            break;
                        }
                    }

                    socket.close();

                }
            }
        }
        keyboard.close();
    }

    // Send message to the service
    private void send() {

    }

    public static void main(String... args) {
        System.out.println("- Client -");

        if (args.length < 2) {
            System.out.println("Usage: java client <type> <port> <list of linkers>");
            return;
        }

        List<MachineAddress> linkers = new ArrayList<>();
        final String type = args[0];
        final int port = Integer.parseInt(args[1]);

        // 127.0.0.1:8080,127.0.0.1:8090
        for (String info : args[2].split(",")) {
            String[] token = info.split(":");
            linkers.add(new MachineAddress(token[0], Integer.parseInt(token[1])));
        }

        Client client = new Client(linkers, type, port);

        try {
            client.subscribeToLinker();
            client.keyListener();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
