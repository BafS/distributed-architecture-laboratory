import messages.Message;
import messages.MessageType;
import services.ServiceReply;
import services.ServiceSum;
import services.ServiceTime;
import services.ServiceType;
import util.ConfigReader;
import util.MachineAddress;
import util.MachineType;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

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

    private DatagramSocket socket;

    // Type of service wanted
    private final ServiceType serviceType;

    // The list of all existing linkers (cannot change)
    private final List<MachineAddress> linkers;

    // The connected service
    private MachineAddress service;

    private int timeout = 500;

    public Client(final List<MachineAddress> linkers, final String type, final int port) throws SocketException {
        this.linkers = linkers;

        if (type.toLowerCase().equals("time")) {
            this.serviceType = ServiceType.SERVICE_TIME;
        } else if (type.equals("reply")) {
            this.serviceType = ServiceType.SERVICE_REPLY;
        } else if (type.equals("sum")) {
            this.serviceType = ServiceType.SERVICE_SUM;
        } else {
            throw new RuntimeException("'" + type + "' is not a valid type of service");
        }

        this.socket = new DatagramSocket(port);
    }

    void handleNotRespondingService() throws IOException, ClassNotFoundException {
        System.out.println("[i] Service down");

        boolean isSent = false;
        byte[] buff = new Message(
                MessageType.SERVICE_DOWN,
                MachineType.CLIENT,
                this.service.toByteArray()
        ).toByteArray();

        // Send to linker
        // Use a random linker in the list
        while (!isSent) {
            MachineAddress linker = linkers.get((int) (Math.random() * linkers.size()));
            DatagramPacket packet = new DatagramPacket(buff, buff.length, linker.getAddress(), linker.getPort());

            packet.setData(buff);
            socket.send(packet);

            socket.setSoTimeout(timeout);

            try {
                socket.receive(packet);

                Message message = Message.fromByteArray(buff);
                if (message.getMessageType() == MessageType.ACK) {
                    isSent = true;
                }
            } catch (SocketTimeoutException e) {
                System.out.println("[i] Timeout, will try to contact an other linker");
            }
        }
    }

    // TODO add timeout
    boolean subscribeToLinker() throws IOException, ClassNotFoundException {
        byte[] buff = new byte[1024];

        // Use a random linker in the list
        MachineAddress linker = linkers.get((int) (Math.random() * linkers.size()));

        byte[] payload = new byte[]{
                this.serviceType.getType()
        };

        // Request a specific service
        DatagramPacket packet = new DatagramPacket(buff, buff.length, linker.getAddress(), linker.getPort());

        packet.setData(new Message(
                MessageType.REQUEST_SERVICE,
                MachineType.CLIENT,
                payload).toByteArray()
        );

        System.out.println("[i] Request service to linker " + linker);

        socket.send(packet);

        // Reset packet
        packet = new DatagramPacket(buff, buff.length);

        socket.setSoTimeout(timeout);

        // Get response
        while (true) {
            try {
                socket.receive(packet);
            } catch (SocketTimeoutException e) {
                System.out.println("[i] Timeout, ask an other linker");

                return subscribeToLinker();
            }

            try {
                Message message = Message.fromByteArray(buff);
//                System.out.println(message);

                if (message.getMessageType() == MessageType.RESPONSE) {
                    System.out.println("[i] Get service address");

                    service = MachineAddress.fromByteArray(message.getPayload());

                    System.out.println(service);

                    return true;
                }
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("[i] The packet is corrupt");
            }
        }
    }

    // https://stackoverflow.com/questions/27381021/detect-a-key-press-in-console
    public void keyListener() throws IOException, ClassNotFoundException {
        Scanner keyboard = new Scanner(System.in);
        boolean exit = false;
        while (!exit) {
            System.out.println("Enter command or payload (quit to exit):");
            String input = keyboard.nextLine();
            if (input != null) {
                System.out.println("Your input is : " + input);
                if ("quit".equals(input) || "q".equals(input)) {
                    System.out.println("Exit client");
                    exit = true;
                } else {
                    byte[] buff = new byte[512];
                    byte[] buffSend = null;
                    DatagramPacket packet = new DatagramPacket(buff, buff.length, service.getAddress(), service.getPort());

                    if (serviceType == ServiceType.SERVICE_SUM) {
                        String[] inputs = input.split(" ");
                        if (inputs.length >= 2) {
                            try {
                                buffSend = new byte[]{
                                        (byte) Integer.parseInt(inputs[0]),
                                        (byte) Integer.parseInt(inputs[1])
                                };
                            } catch (NumberFormatException e) {
                                System.out.println("[i] Invalid numbers");
                                continue;
                            }
                        }
                    } else if (serviceType == ServiceType.SERVICE_REPLY) {
                        buffSend = input.getBytes();
                    }

                    packet.setData(new Message(
                            MessageType.REQUEST,
                            MachineType.CLIENT,
                            buffSend
                    ).toByteArray());

                    socket.send(packet);

                    packet = new DatagramPacket(buff, buff.length);

                    socket.setSoTimeout(timeout);

                    try {
                        socket.receive(packet);
                    } catch (SocketTimeoutException e) {
                        handleNotRespondingService();
                        return;
                    }

                    socket.setSoTimeout(0);


                    Message message = Message.fromByteArray(buff);

                    if (message.getMessageType() == MessageType.RESPONSE) {
                        System.out.println("> Get response");

                        if (serviceType == ServiceType.SERVICE_TIME) {
                            long time = ServiceTime.getResponseFromByteArray(message.getPayload());
                            Date date = new Date(time);

                            System.out.println("[i] Service time: " + date);
                        } else if (serviceType == ServiceType.SERVICE_REPLY) {
                            String rep = ServiceReply.getResponseFromByteArray(message.getPayload());

                            System.out.println("[i] Service reply: " + rep);
                        } else if (serviceType == ServiceType.SERVICE_SUM) {
                            int sum = ServiceSum.getResponseFromByteArray(message.getPayload());

                            System.out.println("[i] Service sum: " + sum);
                        }
                    }
                }
            }
        }

        socket.close();
        keyboard.close();
    }

    public static void main(String... args) {
        System.out.println("- Client -");

        if (args.length < 2) {
            System.out.println("Usage: java client <type> <port>");
            return;
        }

        final String type = args[0];
        final int port = Integer.parseInt(args[1]);

        try {
            List<MachineAddress> linkers = ConfigReader.read(new File("linkers.txt")); // TODO file name: shared const

            Client client = new Client(linkers, type, port);

            while (true) {
                if (client.subscribeToLinker()) {
                    client.keyListener();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
