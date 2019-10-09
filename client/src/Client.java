import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {

    public static void main(String...args) throws IOException {

        Socket socket = new Socket("localhost", 4444);

        final BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        final BufferedReader standardInput = new BufferedReader(new InputStreamReader(System.in));
        final PrintWriter out = new PrintWriter(socket.getOutputStream());

        System.out.println(socket.getLocalSocketAddress());
        System.out.println(socket.getRemoteSocketAddress());

        System.out.print("Name: ");
        String name = standardInput.readLine();
        System.out.println();

        out.println(name);
        out.flush();

        Thread response = new Thread(() -> {
            String message;
            try {
                while (!Thread.interrupted() && (message = in.readLine()) != null) {
                    System.out.println(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        response.start();

        Thread input = new Thread(() -> {
            String message;
            try {
                while (!Thread.interrupted() && (message = standardInput.readLine()) != null) {
                    out.println(message);
                    out.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        input.start();
    }
}
