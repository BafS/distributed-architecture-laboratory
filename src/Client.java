import messages.MessageType;
import messages.MessageUDP;
import util.Machine;
import util.MachineType;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
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

    private List<InetSocketAddress> linkers;

    // The connected service
    private InetSocketAddress service;

    public Client(List<InetSocketAddress> linkers, final String type, final int port) {
        this.linkers = linkers;
        this.type = type.toLowerCase();
        this.PORT = port;
    }

    // TODO share code with Service
    int subscribeToLinker() throws IOException, ClassNotFoundException {
        linkers.forEach(System.out::println);

        // Use a random linker in the list
        InetSocketAddress linker = linkers.get((int) Math.random() * linkers.size());

        byte[] payload = null;
        if (type.equals("time")) {
            payload = new byte[]{MachineType.SERVICE_TIME.getType()};
        } else if (type.equals("reply")) {
            // TODO
            System.out.println("Not implemented yet");
        } else {
            throw new RuntimeException(type + " is not a valid type of service");
        }

        // ask for time
        byte[] buff = new MessageUDP(MessageType.ASK_SERVICE, MachineType.SERVICE_TIME, payload).toByteArray();

        InetAddress address = linker.getAddress();
        DatagramPacket packet = new DatagramPacket(buff, buff.length, address, linker.getPort());

        DatagramSocket socket = new DatagramSocket(PORT);
        socket.send(packet);
        socket.close();

        packet.setLength(buff.length);

        // Get response
        while (true) {
            socket.receive(packet);

            MessageUDP message = MessageUDP.fromByteArray(buff);

            if (message.getMessageType() == MessageType.RESPONSE) {
                System.out.println("GET RESPONSE");

                message.getMessage();
            }
        }

        // Unreachable but should be used
        // socket.close();

//        return socket.getPort();
    }

    // https://stackoverflow.com/questions/27381021/detect-a-key-press-in-console
    public void keyListener() {
        Scanner keyboard = new Scanner(System.in);
        boolean exit = false;
        while (!exit) {
            System.out.println("Enter command (quit to exit):");
            String input = keyboard.nextLine();
            if(input != null) {
                System.out.println("Your input is : " + input);
                if ("quit".equals(input) || "q".equals(input)) {
                    System.out.println("Exit client");
                    exit = true;
                } else {
                    // TODO send "ask time"
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

        List<InetSocketAddress> linkers = new ArrayList<>();
        final String type = args[0];
        final int port = Integer.parseInt(args[1]);

        // 127.0.0.1:8080,127.0.0.1:8090
        for (String info : args[2].split(",")) {
            String[] token = info.split(":");
            linkers.add(new InetSocketAddress(token[0], Integer.parseInt(token[1])));
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
