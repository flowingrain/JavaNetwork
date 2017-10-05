package ch2;

import java.net.*;

import sun.misc.BASE64Encoder;

import java.io.*;

public class MailSenderWithAuth {
    private String smtpServer = "smtp.qq.com";
    private int port = 25;

    public static void main(String[] args) {
        Message msg = new Message("shlip@foxmail.com",
                "flowingfog@163.com",
                "hello",
                "Myself");
        new MailSenderWithAuth().sendMail(msg);
    }

    public void sendMail(Message msg) {
        Socket socket = null;
        try {
            socket = new Socket(smtpServer, port);
            BufferedReader br = getReader(socket);
            PrintWriter pw = getWriter(socket);
            String localhost = InetAddress.getLocalHost().getHostName();

            String username = "flowingfog@163.com";
            String password = "Lee1990";

            //base64
            username = new BASE64Encoder().encode(username.getBytes());
            password = new BASE64Encoder().encode(password.getBytes());
            sendAndReceive(null, br, pw);
            sendAndReceive("EHLO " + localhost, br, pw);
            sendAndReceive("AUTH LOGIN", br, pw);
            sendAndReceive(username, br, pw);
            sendAndReceive(password, br, pw);
            sendAndReceive("MAIL FROM:" + msg.from + "", br, pw);
            sendAndReceive("RCPT TO:" + msg.to + "", br, pw);
            sendAndReceive("DATA", br, pw);
            pw.println(msg.data);
            System.out.println("Client>" + msg.data);
            sendAndReceive(".", br, pw);
            sendAndReceive("QUIT", br, pw);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (socket != null)
                    socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendAndReceive(String str, BufferedReader br, PrintWriter pw) throws IOException {
        if (str != null) {
            System.out.println("Client>" + str);
            pw.println(str);
        }
        String response;
        if ((response = br.readLine()) != null) {
            System.out.println("Server>" + response);
        }
    }

    private PrintWriter getWriter(Socket socket) throws IOException {
        OutputStream socketOut = socket.getOutputStream();
        return new PrintWriter(socketOut, true);
    }

    private BufferedReader getReader(Socket socket) throws IOException {
        InputStream socketIn = socket.getInputStream();
        return new BufferedReader(new InputStreamReader(socketIn));
    }
}
