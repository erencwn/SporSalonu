package application.controllers;

import application.helpers.Session;
import application.helpers.NotificationHelper;
import application.models.Antrenor;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.regex.Pattern;

public class AntrenorGirisController {

    private Stage stage;
    private Scene scene;
    private Parent root;

    @FXML
    private TextField txtEmail;

    @FXML
    private PasswordField txtSifre;

    @FXML
    private void handleGirisYap(ActionEvent event) {
        String email = txtEmail.getText().trim();
        String sifre = txtSifre.getText();

        if (email.isEmpty() || sifre.isEmpty()) {
            NotificationHelper.showError("Lütfen tüm alanları doldurun!");
            return;
        }

        if (!Pattern.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", email)) {
            NotificationHelper.showError("Lütfen geçerli bir e-posta adresi girin!");
            return;
        }

        Antrenor girisYapan = Antrenor.girisKontrol(email, sifre);

        if (girisYapan != null) {
            Session.setAktifKullaniciEmail(girisYapan.getEmail());

            NotificationHelper.showSuccess("Giriş işlemi tamamlandı!");
            System.out.println("Giriş başarılı: " + girisYapan.getAd());

            try {
                Parent root = FXMLLoader.load(getClass().getResource("/fxml/AntrenorEkrani.fxml"));
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
                NotificationHelper.showError("Antrenör ekranı yüklenemedi!");
            }
        } else {
            NotificationHelper.showError("E-posta veya şifre hatalı!");
        }
    }

    @FXML
    private void handleGeriDon(ActionEvent event) throws IOException {
        root = FXMLLoader.load(getClass().getResource("/fxml/AcilisEkrani.fxml"));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    private void handleSifreUnuttum() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SifreSifirla.fxml"));
            Stage sifreStage = new Stage();
            sifreStage.initModality(Modality.APPLICATION_MODAL);
            sifreStage.setTitle("Şifre Sıfırlama");
            sifreStage.getIcons().add(new javafx.scene.image.Image(getClass().getResourceAsStream("/images/uygulama-icon.png")));
            sifreStage.setScene(new Scene(loader.load()));
            sifreStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            NotificationHelper.showError("Şifre sıfırlama ekranı açılamadı!");
        }
    }
}
