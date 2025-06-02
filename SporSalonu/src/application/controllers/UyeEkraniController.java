package application.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class UyeEkraniController {

    private Stage stage;
    private Scene scene;
    private Parent root;	

    @FXML
    private void handleAntrenmanProgramim(ActionEvent event) throws IOException {
        root = FXMLLoader.load(getClass().getResource("/fxml/UyeAntrenmanProgrami.fxml"));
        switchScene(event, root);
    }

    @FXML
    private void handleBilgilerimiGuncelle(ActionEvent event) throws IOException {
        root = FXMLLoader.load(getClass().getResource("/fxml/UyeBilgiGuncelle.fxml"));
        switchScene(event, root);
    }

    @FXML
    private void handleKiloTakibim(ActionEvent event) throws IOException {
        root = FXMLLoader.load(getClass().getResource("/fxml/UyeKiloTakibi.fxml"));
        switchScene(event, root);
    }

    @FXML
    private void handleOdemeTakiplerim(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/OdemeTakibi.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCikisYap(ActionEvent event) throws IOException {
        application.helpers.Session.clearSession();
        root = FXMLLoader.load(getClass().getResource("/fxml/AcilisEkrani.fxml"));
        switchScene(event, root);
    }

    @FXML
    private void handleCikis(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/AcilisEkrani.fxml"));
            switchScene(event, root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void switchScene(ActionEvent event, Parent root) {
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}
