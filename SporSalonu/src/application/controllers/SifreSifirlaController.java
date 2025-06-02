package application.controllers;

import application.helpers.MailSender;
import application.helpers.NotificationHelper;
import application.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Random;

public class SifreSifirlaController {

    @FXML
    private TextField emailField;

    @FXML
    private void handleSifreSifirla() {
        String email = emailField.getText().trim();

        if (email.isEmpty()) {
            NotificationHelper.showWarning("Lütfen e-posta adresinizi giriniz.");
            return;
        }

        try (Connection conn = DatabaseConnection.connect()) {

            boolean kullaniciBulundu = false;

            if (kullaniciVarMi(conn, "uyeler", email)) {
                kullaniciBulundu = true;
                sifreyiGuncelleVeMailGonder(conn, "uyeler", email);
            } else if (kullaniciVarMi(conn, "antrenorler", email)) {
                kullaniciBulundu = true;
                sifreyiGuncelleVeMailGonder(conn, "antrenorler", email);
            }

            if (!kullaniciBulundu) {
                NotificationHelper.showError("Bu e-posta ile kayıtlı kullanıcı bulunamadı.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            NotificationHelper.showError("Bir hata oluştu: " + e.getMessage());
        }
    }

    private boolean kullaniciVarMi(Connection conn, String tablo, String email) throws Exception {
        String sql = "SELECT id FROM " + tablo + " WHERE email = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }

    private void sifreyiGuncelleVeMailGonder(Connection conn, String tablo, String email) throws Exception {
        String yeniSifre = generateRandomPassword();
        String updateSQL = "UPDATE " + tablo + " SET sifre = ? WHERE email = ?";
        try (PreparedStatement updateStmt = conn.prepareStatement(updateSQL)) {
            updateStmt.setString(1, yeniSifre);
            updateStmt.setString(2, email);
            updateStmt.executeUpdate();
        }

        boolean mailGonderildi = MailSender.sendEmail(email, "Şifre Yenileme", "Yeni şifreniz: " + yeniSifre);

        if (mailGonderildi) {
            NotificationHelper.showSuccess("Yeni şifre e-posta adresinize gönderildi.");
            ((Stage) emailField.getScene().getWindow()).close();
        } else {
            NotificationHelper.showError("E-posta gönderilemedi!");
        }
    }

    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789._?*";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
