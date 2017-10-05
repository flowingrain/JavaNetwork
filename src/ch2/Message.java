package ch2;

public class Message {
    String from;
    String to;
    String subject;
    String content;
    String data;

    public Message(String from, String to, String subject, String content) {
        this.from = from;
        this.to = to;
        this.subject = subject;
        this.content = content;
        data = "Subject:" + subject + "\r\n" + content;
    }
}
