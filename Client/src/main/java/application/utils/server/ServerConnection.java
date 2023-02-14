package application.utils.server;

import java.io.*;
import java.net.Socket;

public class ServerConnection {
    private final Socket socket;
    public ServerConnection(String hostAddress, short port) throws IOException {
        socket = new Socket(hostAddress, port);
    }

    public DataInputStream getInputStream() throws IOException {
        return new DataInputStream(new BufferedInputStream(socket.getInputStream()));
    }

    public DataOutputStream getOutputStream() throws IOException {
        return new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
    }

    public void closeConnection() throws IOException {
        socket.close();
    }
}
