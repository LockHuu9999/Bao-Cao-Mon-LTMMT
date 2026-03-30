import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class RandomMatchServer {
    private static final int PORT = 5555;
    private static final List<Socket> waitingList = new ArrayList<>();
    private static volatile ServerSocket serverSocket = null;

    public static void main(String[] args) throws IOException {
        serverSocket = new ServerSocket(PORT);
        System.out.println("🎮 Random Match Server đang chạy trên cổng " + PORT);

        // clean shutdown: close server socket and any waiting client sockets on JVM exit
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down server...");
            try {
                if (serverSocket != null && !serverSocket.isClosed()) serverSocket.close();
            } catch (IOException ex) {
                // ignore
            }
            synchronized (waitingList) {
                for (Socket s : waitingList) {
                    try { s.close(); } catch (IOException ex) { }
                }
                waitingList.clear();
            }
        }));

        while (true) {
            Socket player = serverSocket.accept();
            System.out.println("🔗 Người chơi mới kết nối: " + player);

            synchronized (waitingList) {
                if (waitingList.isEmpty()) {
                    waitingList.add(player);
                    // write a simple line without creating an extra Closeable to avoid leak warnings
                    try {
                        player.getOutputStream().write(("WAITING\n").getBytes(StandardCharsets.UTF_8));
                        player.getOutputStream().flush();
                    } catch (IOException ioe) {
                        try { player.close(); } catch (IOException ex) { }
                    }
                } else {
                    Socket opponent = waitingList.remove(0);
                    startMatch(player, opponent);
                }
            }
        }
    }

    private static void startMatch(Socket player1, Socket player2) {
        try {
            PrintWriter out1 = new PrintWriter(player1.getOutputStream(), true);
            PrintWriter out2 = new PrintWriter(player2.getOutputStream(), true);

            // Decide who starts randomly
            boolean player1Starts = new Random().nextBoolean();
            if (player1Starts) {
                out1.println("MATCHED:START:X");
                out2.println("MATCHED:START:O");
            } else {
                out1.println("MATCHED:START:O");
                out2.println("MATCHED:START:X");
            }

            // Start forwarding threads
            new Thread(new MatchHandler(player1, player2)).start();
            new Thread(new MatchHandler(player2, player1)).start();

            System.out.println("✅ Ghép thành công hai người chơi. (player1Starts=" + player1Starts + ")");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class MatchHandler implements Runnable {
        private Socket player;
        private Socket opponent;

        public MatchHandler(Socket player, Socket opponent) {
            this.player = player;
            this.opponent = opponent;
        }

        @Override
        public void run() {
            BufferedReader in = null;
            try {
                in = new BufferedReader(new InputStreamReader(player.getInputStream(), StandardCharsets.UTF_8));
                OutputStreamWriter ow = new OutputStreamWriter(opponent.getOutputStream(), StandardCharsets.UTF_8);
                BufferedWriter out = new BufferedWriter(ow);
                String line;
                while ((line = in.readLine()) != null) {
                    out.write(line);
                    out.write('\n');
                    out.flush();
                }
            } catch (IOException e) {
                System.out.println("❌ Một người chơi đã thoát trận.");
            } finally {
                // Close both sockets to ensure the match fully terminates and resources freed
                try { if (player != null && !player.isClosed()) player.close(); } catch (IOException ex) { }
                try { if (opponent != null && !opponent.isClosed()) opponent.close(); } catch (IOException ex) { }
                try { if (in != null) in.close(); } catch (IOException ex) { }
            }
        }
    }
}
