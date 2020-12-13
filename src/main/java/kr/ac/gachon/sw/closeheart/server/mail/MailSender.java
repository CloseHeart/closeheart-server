package kr.ac.gachon.sw.closeheart.server.mail;

import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

public class MailSender {
    private static final String SENDER_NAME = "no-reply";
    private static final String GOOGLE_SMTP = "smtp.gmail.com";
    private static final int SMTP_PORT = 587;

    public static boolean sendMail(String toEmail, String title, String content) throws Exception {
        Properties props = System.getProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.auth", "true");

        Session session = Session.getDefaultInstance(props);

        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(MailInfo.GOOGLE_ID, SENDER_NAME));
        msg.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
        msg.setSubject("[Closeheart] " + title);
        msg.setContent(content, "text/html;charset=utf-8");

        Transport transport = session.getTransport();

        try {
            transport.connect(GOOGLE_SMTP, MailInfo.GOOGLE_ID, MailInfo.GOOGLE_PW);
            transport.sendMessage(msg, msg.getAllRecipients());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            transport.close();
        }
        return false;
    }
}