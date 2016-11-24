import messages.Message;
import messages.MessageType;
import util.Machine;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
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
 * 1. Ask a random linker the address of a specific service
 * 2a. if OK
 * 2b. if Error -> goto 1.
 */
public class Client {

    private final int PORT;

    // Type of service wanted
    private final String type;

    private List<Machine> linkers;

    public Client(List<Machine> linkers, final String type, final int port) {
        this.linkers = linkers;
        this.type = type.toLowerCase();
        this.PORT = port;
    }

    // TODO share code with Service
    int subscribeToLinker() throws IOException {
        linkers.forEach(System.out::println);

        // Use a random linker in the list
        Machine linker = linkers.get((int) Math.random() * linkers.size());

        MessageType messageType = null;
        if (type.equals("time")) {
            messageType = MessageType.ACK_TIME;
        } else if (type.equals("reply")) {
            // TODO
            System.out.println("Not implemented yet");
        } else {
            throw new RuntimeException(type + " is not a valid type of service");
        }

        byte[] buff = new Message(messageType).toByteArray();

        InetAddress address = InetAddress.getByName(linker.getHost());
        DatagramPacket packet = new DatagramPacket(buff, buff.length, address, linker.getPort());

        DatagramSocket ds = new DatagramSocket(PORT);
        ds.send(packet);
        ds.close();

        return ds.getPort();
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

        List<Machine> linkers = new ArrayList<>();
        final String type = args[0];
        final int port = Integer.parseInt(args[1]);

        // 127.0.0.1:8080,127.0.0.1:8090
        for (String info : args[2].split(",")) {
            String[] token = info.split(":");
            linkers.add(new Machine(token[0], Integer.parseInt(token[1])));
        }

        Client client = new Client(linkers, type, port);

        try {
            client.subscribeToLinker();
            client.keyListener();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
