package ch3;

import javax.net.ServerSocketFactory;
import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created by flowingfog on 2017/9/16.
 * 具有关闭服务器的功能
 */

public class EchoServer3 {
    private int port = 8000;
    private ServerSocket serverSocket;
    private ExecutorService executorService;//线程池
    private final int POOL_SIZE = 4;//单个CPU是线程池中工作线程的书目

    private int portForShutdown = 8001;//用于监听关闭服务器命令的端口
    private ServerSocket serverSocketForShutDown;
    private boolean isShutdown = false;//服务器是否已关闭

    private Thread shutdownThread = new Thread() {
        public void start() {
            this.setDaemon(true);
            super.start();
        }

        public void run() {
            while (!isShutdown) {
                Socket socketForShutdown = null;
                try {
                    socketForShutdown = serverSocketForShutDown.accept();
                    BufferedReader br = new BufferedReader(new InputStreamReader(socketForShutdown.getInputStream()));
                    String command = br.readLine();
                    if (command.equals("shutdown")) {
                        long beginTime = System.currentTimeMillis();
                        socketForShutdown.getOutputStream().write("服务器正在关闭\r\n".getBytes());
                        isShutdown = true;
                        //请求关闭线程池
                        //线程池不再接受新的任务，但是会继续执行完工作队列中现有的任务
                        executorService.shutdown();

                        //等待关闭线程池，每次等待的超时时间为30秒
                        while (!executorService.isTerminated())
                            executorService.awaitTermination(30, TimeUnit.SECONDS);

                        serverSocket.close();//关闭与EchoClient客户通信的ServerSocket
                        long endTime = System.currentTimeMillis();
                        socketForShutdown.getOutputStream().write(("服务器已经关闭，" + "关闭服务器用了" + (endTime - beginTime) + "毫秒\r\n").getBytes());
                        socketForShutdown.close();
                        serverSocketForShutDown.close();
                        ;
                    } else {
                        socketForShutdown.getOutputStream().write("错误的命令\r\n".getBytes());
                        socketForShutdown.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

    public EchoServer3() throws IOException {
        serverSocket = new ServerSocket(port);
        serverSocket.setSoTimeout(60000);
        serverSocketForShutDown = new ServerSocket(portForShutdown);

        //创建线程池

        executorService = Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors() * POOL_SIZE);
        shutdownThread.start();//启动负责关闭服务器的线程
        System.out.println("服务器启动");
    }

    public void service() {
        while (!isShutdown) {
            Socket socket = null;
            try {
                socket = serverSocket.accept();
                //可能会抛出SocketTimeoutException和SocketException
                socket.setSoTimeout(60000);//把等待客户发送数据的超时时间设为60s
                executorService.execute(new Handler(socket));
                //可能会抛出RejecteedExecutionException
            } catch (SocketTimeoutException e) {
                //不必处理等待客户连接时出现的超时异常
            } catch (RejectedExecutionException e) {
                try {
                    if (socket != null)
                        socket.close();
                } catch (IOException x) {
                }

            } catch (SocketException e) {
                /*如果是由于在执行serverSocket.accept()方法时
                ServerSocket被ShutdownThread线程关闭而导致的异常，就退出service()方法
                 */
                if (e.getMessage().indexOf("socket closed") != -1)
                    return;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String args[]) throws IOException {
        new EchoServer3().service();
    }
}