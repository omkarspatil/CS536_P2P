import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

public class TestClient {
        public static void main(String[] args) throws IOException {
            ServerSocket listener = new ServerSocket(4445);
            try {
                while (true) {
                    Socket socket = listener.accept();
                    System.out.println("Estab");
                    try {

                        BufferedReader input =
                                new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        System.out.println(input.readLine());
                    } finally {
                        socket.close();
                    }
                }
            }
            finally {
                listener.close();
            }
        }
    }
