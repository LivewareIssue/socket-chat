import model.Network;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;

public class Server implements AutoCloseable {

    public static void main(String...args) throws IOException {
        try (Server server = new Server(4444, 4)) {
            while(true){}
        }
    }

    private final ExecutorService serverSocketListener;
    private final ThreadPoolExecutor connectionPool;
    private final Map<String, Connection> connections;

    private ServerSocket serverSocket;

    private Server(int port, int maxConnections) throws IOException {

        if (!Network.UserPorts.contains(port))
            throw new IllegalArgumentException();

        try {
            log("Starting server on port %d", port);
            serverSocket = new ServerSocket(port);

        } catch (IOException e) {
            log(e.getMessage());
        }

        connections = new HashMap<>();
        connectionPool = new ThreadPoolExecutor(maxConnections, maxConnections, 3,
                TimeUnit.SECONDS, new LinkedBlockingQueue<>(maxConnections));

        serverSocketListener = Executors.newSingleThreadExecutor();

        log("Listening on port %s", serverSocket.getLocalSocketAddress());
        serverSocketListener.submit(this::startListening);
    }

    // <editor-fold desc="Output Stream">

    private Optional<OutputStreamWriter> out
            = Optional.empty();

    private static OutputStreamWriter defaultStreamWriter =
            new OutputStreamWriter(System.out);

    private void log(String format, Object...arguments) {

        OutputStreamWriter writer = out.orElse(defaultStreamWriter);

        try {
            writer.write(DateTimeFormatter.ofPattern("dd.mm.yy hh:mm:ss - ").format(LocalDateTime.now()));
            writer.write(String.format(format, arguments) + "\n");

            writer.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void attatch(OutputStream outputStream) {
        this.out = Optional.of(new OutputStreamWriter(outputStream));
    }

    public void dettatch() {
        this.out = Optional.empty();
    }

    // </editor-fold>

    private void startListening() {
        while (true){
            if (connectionPool.getActiveCount() < connectionPool.getMaximumPoolSize()) {
                try {
                    Socket clientSocket = serverSocket.accept();

                    log("%s: Established connection", clientSocket.getRemoteSocketAddress());
                    log("%s: (Client Socket Local)", clientSocket.getLocalSocketAddress());
                    connectionPool.submit(new Connection(clientSocket));

                } catch (IOException e) {
                    log(e.getMessage());
                }
            }
        }
    }

    public void close() throws IOException {

        log("Stopped Listening");
        serverSocketListener.shutdownNow();

        for (Runnable runnable : connectionPool.shutdownNow()) {
            if (Connection.class.isAssignableFrom(runnable.getClass())) {
                Connection connection = (Connection)runnable;
                connection.close();

                log("Terminated connection: %s", connection.clientSocket.getLocalSocketAddress());
            }
        }

        serverSocket.close();
        log("Stopped Server");
    }

    private class Connection implements AutoCloseable, Runnable {

        private final Socket clientSocket;
        private PrintWriter out;
        private Optional<String> client;

        Connection(Socket clientSocket) {
            this.clientSocket = clientSocket;
            this.client = Optional.empty();
        }

        private String getClient() {
            return client.orElse(clientSocket.getRemoteSocketAddress().toString());
        }

        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))){

                out = new PrintWriter(clientSocket.getOutputStream());

                String message;
                while ((message = in.readLine()) != null) {
                    if (!client.isPresent()) {
                        client = Optional.of(message);
                        connections.put(getClient(), this);

                    } else {
                        log("%s: \"%s\"", getClient(), message);

                        for (Connection connection : connections.values()) {
                            if (connection != this) {
                                connection.echo(String.format("%s: %s", getClient(), message));
                            }
                        }
                    }
                }

            } catch (IOException e) {
                log("%s: %s", getClient(), e.getMessage());
                connections.remove(getClient(), e.getMessage());
            }
        }

        void echo(String format, Object... arguments) {
            out.println(String.format(format, arguments));
            out.flush();
        }

        public void close() throws IOException {
            if (out != null) out.close();
            clientSocket.close();
        }
    }
}
