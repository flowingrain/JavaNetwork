package ch4;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

/**
 * Created by flowingfog on 2017/10/5.
 */
public class Target {
    InetSocketAddress address;
    SocketChannel socketChannel;
    Exception failure;
    long connectStart;      //开始连接的时间
    long connectFinish = 0;   //连接成功的时间
    boolean shown = false;    //该任务是否已打印

    Target(String host) {
        try {
            address = new InetSocketAddress(InetAddress.getByName(host), 80);
        } catch (IOException e) {
            failure = e;
        }
    }

    void show() {
        String result;
        if (connectFinish != 0) {
            result = Long.toString(connectFinish - connectStart) + "ms";
        } else {
            if (failure != null)
                result = failure.toString();
            else
                result = "Time out";
        }
        System.out.println(address + ":" + result);
        shown = true;
    }
}