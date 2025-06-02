package application.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class AdminEkraniController {

    private Stage stage;
    private Scene scene;
    private Parent root;

    @FXML
    private void handleUyeIslemleri(ActionEvent event) throws IOException {
        root = FXMLLoader.load(getClass().getResource("/fxml/UyeYonetim.fxml"));
        switchScene(event, root);
    }

    @FXML
    private void handleAntrenorListesi(ActionEvent event) throws IOException {
        root = FXMLLoader.load(getClass().getResource("/fxml/AntrenorYonetim.fxml"));
        switchScene(event, root);
    }

    @FXML
    private void handleOdemeYonetimi(ActionEvent event) throws IOException {
        root = FXMLLoader.load(getClass().getResource("/fxml/OdemeYonetimi.fxml"));
        switchScene(event, root);
    }

    @FXML
    private void handleCikis(ActionEvent event) throws IOException {
        root = FXMLLoader.load(getClass().getResource("/fxml/AcilisEkrani.fxml"));
        switchScene(event, root);
    }

    private void switchScene(ActionEvent event, Parent root) {
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/css/application.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }
}
