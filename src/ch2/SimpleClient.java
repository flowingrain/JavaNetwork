package ch2;

import java.net.Socket;

public class SimpleClient {
    public static void main(String[] args) throws Exception {
        Socket s1 = new Socket("localhost", 8000);
        System.out.println("第一次链接成功");
        Socket s2 = new Socket("localhost", 8000);
        System.out.println("第二次链接成功");
        Socket s3 = new Socket("localhost", 8000);
        System.out.println("第三次链接成功");
    }
}
