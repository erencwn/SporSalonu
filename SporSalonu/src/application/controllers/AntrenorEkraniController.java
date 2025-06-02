package application.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class AntrenorEkraniController {

    private Stage stage;
    private Scene scene;
    private Parent root;

    @FXML
    private void handleKiloTakibi(ActionEvent event) throws IOException {
        switchScene(event, "/fxml/KiloTakibi.fxml");
    }

    @FXML
    private void handleAntrenmanEkle(ActionEvent event) throws IOException {
        switchScene(event, "/fxml/AntrenorAntrenmanProgrami.fxml");
    }

    @FXML
    private void handleBilgilerimiGuncelle(ActionEvent event) throws IOException {
        switchScene(event, "/fxml/AntrenorBilgiGuncelle.fxml");
    }

    @FXML
    private void handleCikis(ActionEvent event) throws IOException {
        switchScene(event, "/fxml/AcilisEkrani.fxml");
    }

    private void switchScene(ActionEvent event, String fxmlPath) throws IOException {
        root = FXMLLoader.load(getClass().getResource(fxmlPath));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/css/application.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }
}
