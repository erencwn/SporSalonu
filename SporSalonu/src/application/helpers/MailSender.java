package application.helpers;

import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class MailSender {

    private static final String FROM_EMAIL = "gelisimsporveyasam@gmail.com";
    private static final String PASSWORD = "egly ubid ucta uvrc"; 

    public static boolean sendEmail(String toEmail, String subject, String yeniSifre) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Gelişim Spor & Yaşam Merkezi - Şifre Yenileme İşlemi");

            String htmlContent = "<div style='font-family: Arial, sans-serif; padding: 20px;'>"
                + "<div style='text-align: center;'>"
                + "<img src='https://i.hizliresim.com/cxh80l4.png' alt='Gelişim Spor ve Yaşam Merkezi' style='width: 200px; height: auto; margin-bottom: 20px;'/>"
                + "<h2 style='color: #1e284b;'>Gelişim Spor & Yaşam Merkezi</h2>"
                + "</div>"
                + "<p>Merhaba,</p>"
                + "<p>Şifre yenileme talebiniz başarıyla alınmıştır.<br>"
                + "Sistem tarafından size atanan yeni şifreniz aşağıda yer almaktadır:</p>"
                + "<div style='background-color: #f2f2f2; padding: 10px; text-align: center; font-size: 20px; letter-spacing: 2px; margin: 20px 0; border: 1px solid #ccc;'>"
                + yeniSifre
                + "</div>"
                + "<p>Lütfen giriş yaptıktan sonra <b>Profil Bilgilerimi Güncelle</b> bölümünden şifrenizi değiştiriniz.</p>"
                + "<br>"
                + "<p style='font-size: 14px; color: #555;'>"
                + "Sevgilerimizle,<br>"
                + "<b>Gelişim Spor & Yaşam Merkezi Ekibi</b>"
                + "</p>"
                + "</div>";

            message.setContent(htmlContent, "text/html; charset=utf-8");

            Transport.send(message);
            System.out.println("E-posta başarıyla gönderildi: " + toEmail);
            return true;
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }
}
