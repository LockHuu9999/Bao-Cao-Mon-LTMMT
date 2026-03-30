import java.io.*;
import java.net.*;

/**
 * Very small TCP client helper for the RandomMatchServer protocol.
 * It reads lines from the server on a background thread and notifies a listener.
 */
public class NetworkClient {
    public interface Listener {
        void onConnected();
        void onMessage(String msg);
        void onDisconnected(Exception ex);
    }

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Thread readerThread;
    private Listener listener;

    public NetworkClient(Listener listener) {
        this.listener = listener;
    }

    public void connect(String host, int port) throws IOException {
        socket = new Socket();
        socket.connect(new InetSocketAddress(host, port), 5000);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        // start reader thread
        readerThread = new Thread(() -> {
            try {
                if (listener != null) listener.onConnected();
                String line;
                while ((line = in.readLine()) != null) {
                    if (listener != null) listener.onMessage(line);
                }
            } catch (Exception ex) {
                if (listener != null) listener.onDisconnected(ex);
            } finally {
                try { if (socket != null) socket.close(); } catch (IOException e) {}
            }
        }, "NetworkClient-Reader");
        readerThread.setDaemon(true);
        readerThread.start();
    }

    public synchronized void send(String s) {
        if (out != null) out.println(s);
    }

    public synchronized void close() {
        try {
            if (socket != null) socket.close();
        } catch (IOException e) {
        }
    }
}
