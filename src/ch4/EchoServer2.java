package ch4;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by flowingfog on 2017/10/4.
 * 混合使用阻塞模式与非阻塞模式
 */
public class EchoServer2 {
    private Selector selector = null;
    private int port = 8000;
    private ServerSocketChannel serverSocketChannel = null;
    private Charset charset = Charset.forName("GBK");

    public EchoServer2() throws IOException {
        selector = Selector.open();
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().setReuseAddress(true);
        //serverSocketChannel.configureBlocking(false);
        serverSocketChannel.socket().bind(new InetSocketAddress(port));
        System.out.println("服务器已启动");
    }

    public void accept() {
        for (; ; ) {
            try {
                SocketChannel socketChannel = serverSocketChannel.accept();
                System.out.println("接收到客户链接，来自：" +
                        socketChannel.socket().getInetAddress() +
                        ":" + socketChannel.socket().getPort());
                socketChannel.configureBlocking(false);

                ByteBuffer buffer = ByteBuffer.allocate(1024);
                synchronized (gate) {
                    selector.wakeup();
                    socketChannel.register(selector,
                            SelectionKey.OP_READ | SelectionKey.OP_WRITE,
                            buffer);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Object gate = new Object();

    public void service() throws IOException {
        for (; ; ) {
            synchronized (gate) {
            }
            ;
            int n = selector.select();
            if (n == 0)
                continue;
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
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    public void send(SelectionKey key) throws IOException {
        ByteBuffer buffer = (ByteBuffer) key.attachment();
        SocketChannel socketChannel = (SocketChannel) key.channel();
        buffer.flip();//把集显设为位置，把位置设为0
        String data = decode(buffer);
        if (data.indexOf("\r\n") == -1)
            return;
        String outputData = data.substring(0, data.indexOf("\n") + 1);
        System.out.print(outputData);
        ByteBuffer outputBuffer = encode("echo:" + outputData);
        while (outputBuffer.hasRemaining())//发送一行字符串
            socketChannel.write(outputBuffer);

        ByteBuffer temp = encode(outputData);
        buffer.position(temp.limit());
        buffer.compact();//删除已经处理的字符串

        if (outputData.equals("bye\r\n")) {
            key.cancel();
            socketChannel.close();
            System.out.println("关闭与客户的连接");
        }
    }

    public void receive(SelectionKey key) throws IOException {
        ByteBuffer buffer = (ByteBuffer) key.attachment();

        SocketChannel socketChannel = (SocketChannel) key.channel();
        ByteBuffer readBuff = ByteBuffer.allocate(32);
        socketChannel.read(readBuff);
        readBuff.flip();

        buffer.limit(buffer.capacity());
        buffer.put(readBuff);//把读到的数据放到buffer中
    }

    public String decode(ByteBuffer buffer) {//解码
        CharBuffer charBuffer = charset.decode(buffer);
        return charBuffer.toString();
    }

    public ByteBuffer encode(String str) {//编码
        return charset.encode(str);
    }

    public static void main(String[] args) throws IOException {
        final EchoServer2 server = new EchoServer2();
        Thread accept = new Thread() {
            public void run() {
                server.accept();
            }
        };
        accept.start();
        server.service();
    }
}
