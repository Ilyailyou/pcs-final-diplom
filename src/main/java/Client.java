import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {
    private static final int PORT = 8989;
    private static final String HOST = "localhost";

    public static void main(String[] args) {
        try (Socket clientSocket = new Socket(HOST, PORT);
             PrintWriter out = new PrintWriter(
                     clientSocket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(
                     new InputStreamReader(clientSocket.getInputStream()));
        ) {
            Scanner scanner = new Scanner(System.in);
            String request = scanner.nextLine();
            out.println(request);
            System.out.println(in.readLine());
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
