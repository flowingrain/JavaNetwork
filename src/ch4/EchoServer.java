package ch4;

/**
 * Created by flowingfog on 2017/10/3.
 * 阻塞模式
 */

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.*;
import java.nio.channels.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EchoServer {
    private int port = 8000;
    private ServerSocketChannel serverSocketChannel = null;
    private ExecutorService executorService;
    private static final int POOL_MULTIPLE = 4;

    public EchoServer() throws IOException {
        //创建一个线程池
        executorService = Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors() * POOL_MULTIPLE
        );
        //创建一个ServerSocketChannel对象
        serverSocketChannel = ServerSocketChannel.open();
        /*
        使得在同一个主机上关闭了服务器程序，紧接着再启动该服务器程序时，可以顺利绑定相同的端口
         */
        serverSocketChannel.socket().setReuseAddress(true);
        //把服务器进程与一个本地端口绑定
        serverSocketChannel.socket().bind(new InetSocketAddress(port));
        System.out.println("服务器启动");
    }

    public void service() {
        while (true) {
            SocketChannel socketChannel = null;
            try {
                socketChannel = serverSocketChannel.accept();
                executorService.execute(new Handler(socketChannel));//处理客户链接
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        new EchoServer().service();
    }
}