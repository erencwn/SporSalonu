package application.controllers;

import application.models.AdminModel;
import application.helpers.NotificationHelper; // ✅ NotificationHelper'ı ekledik
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

import java.io.IOException;

public class AdminGirisController {

    private Stage stage;
    private Scene scene;
    private Parent root;

    @FXML
    private PasswordField txtAdminKey;

    private AdminModel adminModel = new AdminModel();

    @FXML
    private void handleAdminGiris(ActionEvent event) {
        String key = txtAdminKey.getText().trim();

        if (key.isEmpty()) {
            NotificationHelper.showError("Lütfen Admin Key'i girin!");
            return;
        }

        boolean girisBasarili = adminModel.adminGirisKontrolByKey(key);

        if (!girisBasarili) {
            NotificationHelper.showError("Geçersiz Admin Key!");
            return;
        }

        NotificationHelper.showSuccess("Admin girişi tamamlandı!");
        System.out.println("Admin girişi başarılı!");

        try {
            root = FXMLLoader.load(getClass().getResource("/fxml/AdminEkrani.fxml"));
            stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            NotificationHelper.showError("Admin Paneli açılamadı!");
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
}
