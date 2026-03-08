import java.io.*;
import java.net.*;

public class Server {
    private Socket s = null;
    private ServerSocket ss = null;
    private DataInputStream in = null;
    //usage Server s = new Server(8080);
    public Server(int port) throws IOException{
        try {
            ss = new ServerSocket(port);
            System.out.println("Server started");

            System.out.println("Waiting for client..");

            s = ss.accept();
            System.out.println("Client Accepted");

            in = new DataInputStream(new BufferedInputStream(s.getInputStream()));

            String m = "";
            while (!m.equals("Over")) {
                try {
                    m = in.readUTF();
                    System.out.println(m);
                } catch (IOException i) {
                    System.out.println(i);
                }
            }
            System.out.println("Closing Connection");
            s.close();
            in.close();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
