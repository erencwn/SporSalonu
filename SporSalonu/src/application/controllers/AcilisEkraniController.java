package application.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class AcilisEkraniController {

    private Stage stage;
    private Scene scene;
    private Parent root;

    @FXML
    private void handleGirisYap(ActionEvent event) throws IOException {
        root = FXMLLoader.load(getClass().getResource("/fxml/GirisEkrani.fxml"));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.sizeToScene(); 
        stage.show();
    }

    @FXML
    private void handleKayitOl(ActionEvent event) throws IOException {
        root = FXMLLoader.load(getClass().getResource("/fxml/KayitOl.fxml"));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.sizeToScene(); 
        stage.show();
    }

    @FXML
    private void handleAdminGiris(ActionEvent event) throws IOException {
        root = FXMLLoader.load(getClass().getResource("/fxml/AdminGiris.fxml"));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.sizeToScene(); 
        stage.show();
    }

    @FXML
    private void handleAntrenorGiris(ActionEvent event) throws IOException {
        root = FXMLLoader.load(getClass().getResource("/fxml/AntrenorGiris.fxml"));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.sizeToScene();  
        stage.show();
    }
}
