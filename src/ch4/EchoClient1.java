package ch4;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by flowingfog on 2017/10/4.
 */
public class EchoClient1 {
    private SocketChannel socketChannel = null;
    private ByteBuffer sendBuffer = ByteBuffer.allocate(1024);
    private ByteBuffer receiveBuffer = ByteBuffer.allocate(1024);
    private Charset charset = Charset.forName("GBK");
    private Selector selector;

    public EchoClient1() throws IOException {
        socketChannel = SocketChannel.open();
        InetAddress ia = InetAddress.getLocalHost();
        InetSocketAddress isa = new InetSocketAddress(ia, 8000);
        socketChannel.connect(isa);//连接服务器
        socketChannel.configureBlocking(false);//设置为非阻塞模式
        System.out.println("与服务器的连接建立成功");
        selector = Selector.open();
    }

    private PrintWriter getWriter(Socket socket) throws IOException {
        OutputStream socketOut = socket.getOutputStream();
        return new PrintWriter(socketOut, true);
    }

    private BufferedReader getReader(Socket socket) throws IOException {
        InputStream socketIn = socket.getInputStream();
        return new BufferedReader(new InputStreamReader(socketIn));
    }

    public void receiveFromUser() {//接收用户从控制台输入的数据，把它放到sendBuffer中
        try {
            BufferedReader localReader = new BufferedReader((new InputStreamReader(System.in)));
            String msg = null;
            while ((msg = localReader.readLine()) != null) {
                synchronized (sendBuffer) {
                    sendBuffer.put(encode(msg + "\r\n"));
                }
                if (msg.equals("bye"))
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String decode(ByteBuffer buffer) {//解码
        CharBuffer charBuffer = charset.decode(buffer);
        return charBuffer.toString();
    }

    public ByteBuffer encode(String str) {
        return charset.encode(str);
    }

    public void talk() throws IOException {//接收和发送数据
        socketChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        while (selector.select() > 0) {
            Set readyKeys = selector.selectedKeys();
            Iterator it = readyKeys.iterator();
            while (it.hasNext()) {
                SelectionKey key = null;
                try {
                    key = (SelectionKey) it.next();
                    it.remove();

                    if (key.isReadable()) {
                        receive(key);
                    }
                    if (key.isWritable()) {
                        send(key);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    try {
                        if (key != null) {
                            key.cancel();
                            key.channel().close();
                        }
                    } catch (Exception ex) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void send(SelectionKey key) throws IOException {
        //发送sendBuffer中的数据
        SocketChannel socketChannel = (SocketChannel) key.channel();
        synchronized (sendBuffer) {
            sendBuffer.flip();                  //把极限设为位置，把位置设为0
            socketChannel.write(sendBuffer);    //发送数据
            sendBuffer.compact();               //删除已经发送的数据
        }
    }

    public void receive(SelectionKey key) throws IOException {
        //接收EchoServer发送的数据，把它放到receiveBuffer中
        //如果receiveBuffer中由一行数据，就打印这行数据，然后把它从receiveBuffer中删除
        SocketChannel socketChannel = (SocketChannel) key.channel();
        socketChannel.read(receiveBuffer);
        receiveBuffer.flip();
        String receiveData = decode(receiveBuffer);

        if (receiveData.indexOf("\n") == -1)
            return;

        String outputData = receiveData.substring(0, receiveData.indexOf("\n") + 1);
        System.out.print(outputData);
        if (outputData.equals("echo:bye\r\n")) {
            key.cancel();
            socketChannel.close();
            System.out.println("关闭与服务器的链接");
            selector.close();
            System.exit(0);//结束程序
        }

        ByteBuffer temp = encode(outputData);
        receiveBuffer.position();
        receiveBuffer.compact();//删除已经打印的数据
    }

    public static void main(String args[]) throws IOException {
        final EchoClient1 client = new EchoClient1();
        Thread receiver = new Thread() {
            public void run() {
                client.receiveFromUser();
            }
        };
        receiver.start();
        ;
        client.talk();
    }
}