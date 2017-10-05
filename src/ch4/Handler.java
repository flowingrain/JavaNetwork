package ch4;

import java.io.*;
import java.net.Socket;
import java.nio.channels.SocketChannel;

/**
 * Created by flowingfog on 2017/10/3.
 */
class Handler implements Runnable {
    private SocketChannel socketChannel;

    public Handler(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    public void run() {
        handle(socketChannel);
    }

    public void handle(SocketChannel socketChannel) {
        try {
            Socket socket = socketChannel.socket();//获得与socketChannel关联的SOcket对象
            System.out.println("接收到客户链接，来自：" +
                    socket.getInetAddress() + ":" + socket.getPort());

            BufferedReader br = getrReader(socket);
            PrintWriter pw = getWriter(socket);

            String msg = null;
            while ((msg = br.readLine()) != null) {
                System.out.println(msg);
                pw.println(echo(msg));
                if (msg.equals("bye"))
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (socketChannel != null)
                    socketChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private PrintWriter getWriter(Socket socket) throws IOException {
        OutputStream socketOut = socket.getOutputStream();
        return new PrintWriter(socketOut, true);
    }

    private BufferedReader getrReader(Socket socket) throws IOException {
        InputStream socketIn = socket.getInputStream();
        return new BufferedReader(new InputStreamReader(socketIn));
    }

    public String echo(String msg) {
        return "echo:" + msg;
    }
}
