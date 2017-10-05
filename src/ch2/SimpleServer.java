package ch2;

import java.net.ServerSocket;

public class SimpleServer {
    public static void main(String[] args) throws Exception {
        ServerSocket ss = new ServerSocket(8000, 2);//端口8000,请求队列长度2
        Thread.sleep(6000);
    }
}
