package ch3;

import java.util.LinkedList;

public class ThreadPool extends ThreadGroup {
    private boolean isClosed = false;
    private LinkedList<Runnable> workQueue;
    private static int threadPoolID;
    private int threadID;

    public ThreadPool(int poolSize) {
        super("ThreadPool-" + (threadPoolID++));
        setDaemon(true);
        workQueue = new LinkedList<Runnable>();
        for (int i = 0; i < poolSize; i++) {
            new WorkThread().start();
        }
    }

    public synchronized void execute(Runnable task) {

    }

    protected synchronized Runnable getTask() throws InterruptedException {
        while (workQueue.size() == 0) {
            if (isClosed)
                return null;
            wait();
        }
        return workQueue.removeFirst();
    }

    //关闭线程池
    public synchronized void close() {
        if (!isClosed) {
            isClosed = true;
            workQueue.clear();//清空工作队列
            interrupt();//终端所有的工作线程，该方法继承自ThreadGroup类
        }
    }

    //等待工作线程把所有任务执行完
    public void join() {
        synchronized (this) {
            isClosed = true;
            notifyAll();//唤醒还在getTask()方法中等待任务的工作线程
        }

        Thread[] threads = new Thread[activeCount()];
        //enumerate()方法集成ziThreadGroup类。获得线程组中当前所有或者的工作线程
        int count = enumerate(threads);
        for (int i = 0; i < count; i++)//等待所有工作线程运行结束
        {
            try {
                threads[i].join();
            } catch (InterruptedException e) {

            }
        }
    }

    //内部类：工作线程
    private class WorkThread extends Thread {
        public WorkThread() {
            //加入到当前ThreadPool线程组中
            super(ThreadPool.this, "WorkThread-" + (threadID++));
        }

        public void run() {

        }
    }

}
