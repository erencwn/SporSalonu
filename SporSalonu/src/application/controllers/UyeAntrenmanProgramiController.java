package application.controllers;

import application.DatabaseConnection;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.*;

public class UyeAntrenmanProgramiController implements Initializable {

    @FXML private TableView<ObservableList<String>> tblProgram;
    @FXML private TableColumn<ObservableList<String>, String> colTarih, colH1, colT1;
    @FXML private ComboBox<String> cbTarihSec;
    @FXML private Label lblHareket1, lblHareket2, lblHareket3, lblHareket4, lblHareket5;
    @FXML private ImageView imgHareket1, imgHareket2, imgHareket3, imgHareket4, imgHareket5;
    @FXML private Button btnGeriDon;

    private final Map<Integer, String> hareketAdlari = new HashMap<>();
    private final Map<Integer, String> hareketGifleri = new HashMap<>();
    private final Map<String, Integer> tarihToProgramId = new LinkedHashMap<>();
    private final int uyeId = application.helpers.Session.getAktifKullaniciId();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        hareketleriYukle();
        tarihleriYukle();

        cbTarihSec.setOnAction(e -> {
            String secilen = cbTarihSec.getValue();
            if (secilen != null) {
                programYukle(secilen);
            }
        });
    }

    private void setupTableColumns() {
        colTarih.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(0)));
        colH1.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(1)));
        colT1.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(2)));
    }

    private void hareketleriYukle() {
        try (Connection conn = DatabaseConnection.connect()) {
            PreparedStatement ps = conn.prepareStatement("SELECT hareket_id, hareket_adi, gif_yolu FROM hareketler");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("hareket_id");
                hareketAdlari.put(id, rs.getString("hareket_adi"));
                hareketGifleri.put(id, rs.getString("gif_yolu"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void tarihleriYukle() {
        try (Connection conn = DatabaseConnection.connect()) {
            String sql = "SELECT * FROM antrenman_programi WHERE uye_id = ? ORDER BY tarih DESC";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, uyeId);
            ResultSet rs = ps.executeQuery();
            cbTarihSec.getItems().clear();
            tarihToProgramId.clear();

            boolean isFirst = true;
            while (rs.next()) {
                String formatted = rs.getDate("tarih").toString();
                String display = isFirst ? formatted + " (Güncel)" : formatted;
                tarihToProgramId.put(display, rs.getInt("program_id"));
                cbTarihSec.getItems().add(display);
                if (isFirst) {
                    cbTarihSec.setValue(display); 
                    programYukle(display);
                    isFirst = false;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void programYukle(String tarihLabel) {
        tblProgram.getItems().clear();
        sifirlaGorseller();

        if (!tarihToProgramId.containsKey(tarihLabel)) return;

        try (Connection conn = DatabaseConnection.connect()) {
            String sql = "SELECT * FROM antrenman_programi WHERE program_id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, tarihToProgramId.get(tarihLabel));
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String tarih = rs.getDate("tarih").toString();

                for (int i = 1; i <= 5; i++) {
                    int hareketId = rs.getInt("hareket_id_" + i);
                    String hareketAd = hareketAdlari.getOrDefault(hareketId, "-");
                    String tekrar = rs.getString("tekrar_" + i);

                    ObservableList<String> row = FXCollections.observableArrayList();
                    row.add(tarih);
                    row.add(hareketAd);
                    row.add(tekrar);
                    tblProgram.getItems().add(row);

                    hareketGoster(i, hareketAd, hareketGifleri.get(hareketId));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void hareketGoster(int index, String ad, String gifPath) {
        Image img = null;
        try {
            if (gifPath != null && getClass().getResourceAsStream(gifPath) != null) {
                img = new Image(getClass().getResourceAsStream(gifPath));
            }
        } catch (Exception ignored) {}

        switch (index) {
            case 1 -> {
                lblHareket1.setText(ad);
                imgHareket1.setImage(img);
            }
            case 2 -> {
                lblHareket2.setText(ad);
                imgHareket2.setImage(img);
            }
            case 3 -> {
                lblHareket3.setText(ad);
                imgHareket3.setImage(img);
            }
            case 4 -> {
                lblHareket4.setText(ad);
                imgHareket4.setImage(img);
            }
            case 5 -> {
                lblHareket5.setText(ad);
                imgHareket5.setImage(img);
            }
        }
    }

    private void sifirlaGorseller() {
        lblHareket1.setText("-");
        lblHareket2.setText("-");
        lblHareket3.setText("-");
        lblHareket4.setText("-");
        lblHareket5.setText("-");

        imgHareket1.setImage(null);
        imgHareket2.setImage(null);
        imgHareket3.setImage(null);
        imgHareket4.setImage(null);
        imgHareket5.setImage(null);
    }

    @FXML
    private void handleGeriDon() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/UyeEkrani.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnGeriDon.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
