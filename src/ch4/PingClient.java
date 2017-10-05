package ch4;

import java.io.IOException;
import java.nio.channels.Selector;
import java.util.LinkedList;
import java.util.TreeMap;

/**
 * Created by flowingfog on 2017/10/5.
 * 非阻塞模式
 */
public class PingClient {//表示一想任务
    private Selector selector;
    //存放用户新提交的任务
    private LinkedList target = new LinkedList();
    //存放已经完成的需要打印的任务
    private LinkedList finishedTarget = new LinkedList();

    public PingClient() throws IOException {
        selector = Selector.open();
        Connector
    }

    boolean shutdown = false;//用于控制Connector线程

    public class Printer extends Thread {
        public Printer() {
            setDaemon(true);//设置为后台线程
        }

        public void run() {
            printFinishedTarget();
        }
    }

    public class Connector extends Thread {
        public void run() {
            while (!shutdown) {
                try {
                    registerTargets();
                    if (selector.select() > 0) {
                        processSelectedKeys();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            try {
                selector.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}