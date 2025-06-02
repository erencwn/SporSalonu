package application.controllers;

import application.helpers.Session;
import application.helpers.NotificationHelper;
import application.models.UyeModel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.DatePicker;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;
import java.util.regex.Pattern;

public class KayitOlController {

    private Stage stage;
    private Scene scene;
    private Parent root;

    @FXML
    private TextField txtAd, txtSoyad, txtEmail, txtTelefon;

    @FXML
    private PasswordField txtSifre, txtSifreTekrar;

    @FXML
    private DatePicker dateDogumTarihi;

    private UyeModel uyeModel = new UyeModel();

    @FXML
    private void handleKayitOl(ActionEvent event) {
        String ad = txtAd.getText().trim();
        String soyad = txtSoyad.getText().trim();
        String email = txtEmail.getText().trim();
        String telefon = txtTelefon.getText().trim();
        String sifre = txtSifre.getText();
        String sifreTekrar = txtSifreTekrar.getText();
        LocalDate dogumTarihi = dateDogumTarihi.getValue();

        if (ad.isEmpty() || soyad.isEmpty() || email.isEmpty() || telefon.isEmpty() || sifre.isEmpty() || sifreTekrar.isEmpty() || dogumTarihi == null) {
            NotificationHelper.showWarning("Lütfen tüm alanları doldurun!");
            return;
        }

        if (!Pattern.matches("^05\\d{9}$", telefon)) {
            NotificationHelper.showError("Telefon numarası 05 ile başlamalı ve 11 haneli olmalıdır!");
            return;
        }

        if (!Pattern.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", email)) {
            NotificationHelper.showError("Lütfen geçerli bir e-posta adresi girin!");
            return;
        }

        if (sifre.length() < 6) {
            NotificationHelper.showError("Şifre en az 6 karakter olmalıdır!");
            return;
        }

        if (!sifre.equals(sifreTekrar)) {
            NotificationHelper.showError("Şifreler uyuşmuyor! Lütfen tekrar girin.");
            return;
        }

        boolean sonuc = uyeModel.uyeEkle(ad, soyad, email, sifre, telefon, Date.valueOf(dogumTarihi));

        if (sonuc) {
            NotificationHelper.showSuccess("Kayıt işlemi tamamlandı!");
            try {
                root = FXMLLoader.load(getClass().getResource("/fxml/GirisEkrani.fxml"));
                stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                scene = new Scene(root);
                scene.getStylesheets().add(getClass().getResource("/css/application.css").toExternalForm());
                stage.setScene(scene);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
                NotificationHelper.showError("Giriş ekranı yüklenemedi!");
            }
        } else {
            NotificationHelper.showError("Kayıt yapılamadı! Lütfen tekrar deneyin.");
        }
    }

    @FXML
    private void handleGeriDon(ActionEvent event) throws IOException {
        root = FXMLLoader.load(getClass().getResource("/fxml/AcilisEkrani.fxml"));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/css/application.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }
}
