import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Small console client to test RandomMatchServer. Run twice to simulate two players.
 */
public class TestClient {
    public static void main(String[] args) throws Exception {
        String host = args.length > 0 ? args[0] : "localhost";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 5555;

        try (Socket s = new Socket(host, port);
             BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8), true);
             Scanner sc = new Scanner(System.in)) {

            System.out.println("Connected to server");

            Thread reader = new Thread(() -> {
                try {
                    String line;
                    while ((line = in.readLine()) != null) {
                        System.out.println("<- " + line);
                    }
                } catch (IOException e) {
                    System.out.println("Connection closed");
                }
            });
            reader.setDaemon(true);
            reader.start();

            while (true) {
                if (!sc.hasNextLine()) break;
                String l = sc.nextLine();
                if (l.equalsIgnoreCase("quit")) break;
                out.println(l);
            }
        }
    }
}
