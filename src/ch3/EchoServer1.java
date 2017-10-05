package ch3;

import java.io.*;
import java.net.*;

public class EchoServer1 {
    private int port = 8000;
    private ServerSocket serverSocket;
    private ThreadPool threadPool;
    private final int POOL_SIZE = 4;

    public EchoServer() throws IOException {
        serverSocket = new ServerSocket(port);

    }
}
