import util.Machine;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.List;

/**
 * Linker are the bridge between clients and service
 * because linkers IPs are known from the clients and the services.
 *
 * Linker communicate to clients with messages (Message)
 *
 * When a linker is halt or stopped by error, the linker automatically tries to restart
 *
 * Linker cannot be added after the initialization
 */
public class Linker {
    private final String HOST;

    private final int PORT;

    private List<Machine> services = new ArrayList<>();

    public Linker(String host, int port) {
        HOST = host;
        PORT = port;
    }

//    private void onNewRegistration()

    public void listen() throws IOException {
        byte[] buf = new byte[Long.BYTES];
        DatagramPacket dp = new DatagramPacket(buf, 128);

        DatagramSocket socket = new DatagramSocket(PORT);

        // Listen for new messages
        while (true) {
            socket.receive(dp);

            byte[] data = dp.getData();

            // switch on type

            System.out.println(data);
        }
    }

    public static void main(String[] args) {
        System.out.println("- Linker -");

        if (args.length < 2) {
            System.out.println("Usage: java linker <host> <port>");
            return;
        }

        String host = args[1];
        int port = Integer.parseInt(args[2]);

        try {
            Linker linker = new Linker(host, port);
            linker.listen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
