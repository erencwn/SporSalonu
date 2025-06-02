package application.controllers;

import application.DatabaseConnection;
import application.helpers.NotificationHelper;
import application.models.UyeModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;


import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class KiloTakibiController {

    @FXML private ListView<String> lstUyeler;
    @FXML private Label lblUyeAdSoyad, lblDogumTarihi, lblBoy, lblBMI, lblHedefBMI;
    @FXML private Label lblGuncelKg, lblHedefKg;
    @FXML private TextField txtKilo, txtYag, txtKas, txtBoyGiris;
    @FXML private Button btnBoyKaydet, btnVeriKaydet;
    @FXML private LineChart<String, Number> lineChart;
    @FXML private HBox boxUyari;
    @FXML private Button btnGeriDon;


    private int seciliUyeId = -1;
    private final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @FXML
    public void initialize() {
        uyeleriYukle();
        lstUyeler.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                seciliUyeId = Integer.parseInt(newVal.split(" - ")[0]);
                uyeDetaylariniGoster();
                takipVerileriniYukle();
            }
        });
    }

    private void uyeleriYukle() {
        try (Connection conn = DatabaseConnection.connect()) {
            String sql = "SELECT id, ad, soyad FROM uyeler";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                String adsoyad = rs.getString("ad") + " " + rs.getString("soyad");
                lstUyeler.getItems().add(id + " - " + adsoyad);
            }
        } catch (SQLException e) {
            NotificationHelper.showError("Üye listesi yüklenemedi: " + e.getMessage());
        }
    }

    private void uyeDetaylariniGoster() {
        try (Connection conn = DatabaseConnection.connect()) {
            String sql = "SELECT ad, soyad, dogum_tarihi, BoyCm FROM uyeler WHERE id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, seciliUyeId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                lblUyeAdSoyad.setText(rs.getString("ad") + " " + rs.getString("soyad"));
                LocalDate dogum = rs.getDate("dogum_tarihi").toLocalDate();
                lblDogumTarihi.setText(dogum.format(dateFormat));
                int boy = rs.getInt("BoyCm");

                if (rs.wasNull() || boy == 0) {
                    boxUyari.setVisible(true);
                    boxUyari.setManaged(true);
                    btnBoyKaydet.setDisable(false);
                    lblBoy.setText("-");
                    lblBMI.setText("-");
                    lblHedefBMI.setText("-");
                    lblGuncelKg.setText("-");
                    lblHedefKg.setText("-");
                } else {
                    boxUyari.setVisible(false);
                    boxUyari.setManaged(false);
                    lblBoy.setText(boy + " cm");
                    guncelBMIGoster(boy);
                }
            }
        } catch (SQLException e) {
            NotificationHelper.showError("Üye detayları alınamadı.");
        }
    }

    private void guncelBMIGoster(int boyCm) {
        try (Connection conn = DatabaseConnection.connect()) {
            String sql = "SELECT TOP 1 Kilo FROM BoyKiloTakip WHERE UyeID = ? ORDER BY Tarih DESC";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, seciliUyeId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                double kilo = rs.getDouble("Kilo");
                double boyMetre = boyCm / 100.0;
                double bmi = kilo / (boyMetre * boyMetre);
                double hedefKg = 22 * (boyMetre * boyMetre);

                lblBMI.setText(String.format("%.2f", bmi));
                lblHedefBMI.setText("22.0");
                lblGuncelKg.setText(String.format("%.1f", kilo));
                lblHedefKg.setText(String.format("%.1f", hedefKg));
            } else {
                lblBMI.setText("-");
                lblHedefBMI.setText("22.0");
                lblGuncelKg.setText("-");
                lblHedefKg.setText("-");
            }
        } catch (Exception e) {
            lblBMI.setText("-");
        }
    }

    @FXML
    private void handleBoyKaydet() {
        try {
            int boy = Integer.parseInt(txtBoyGiris.getText());
            UyeModel model = new UyeModel();
            if (model.boyGuncelle(seciliUyeId, boy)) {
                NotificationHelper.showSuccess("Boy bilgisi güncellendi.");
                txtBoyGiris.clear();
                uyeDetaylariniGoster();
                guncelBMIGoster(boy);
            } else {
                NotificationHelper.showError("Boy bilgisi güncellenemedi.");
            }
        } catch (Exception e) {
            NotificationHelper.showWarning("Geçerli bir boy giriniz.");
        }
    }

    @FXML
    private void handleVeriKaydet() {
        try {
            double kilo = Double.parseDouble(txtKilo.getText());
            double yag = Double.parseDouble(txtYag.getText());
            double kas = Double.parseDouble(txtKas.getText());

            try (Connection conn = DatabaseConnection.connect()) {
                String sql = "INSERT INTO BoyKiloTakip (UyeID, Tarih, Kilo, YagKg, KasKg) VALUES (?, GETDATE(), ?, ?, ?)";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, seciliUyeId);
                ps.setDouble(2, kilo);
                ps.setDouble(3, yag);
                ps.setDouble(4, kas);
                ps.executeUpdate();
                NotificationHelper.showSuccess("Veri başarıyla kaydedildi.");

                txtKilo.clear();
                txtYag.clear();
                txtKas.clear();

                takipVerileriniYukle();
                guncelBMIGoster(getUyeBoyCm());
            }
        } catch (Exception e) {
            NotificationHelper.showWarning("Geçerli veriler giriniz.");
        }
    }

    private int getUyeBoyCm() {
        try (Connection conn = DatabaseConnection.connect()) {
            String sql = "SELECT BoyCm FROM uyeler WHERE id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, seciliUyeId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("BoyCm");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void takipVerileriniYukle() {
        lineChart.getData().clear();
        XYChart.Series<String, Number> seriKilo = new XYChart.Series<>();
        seriKilo.setName("Kilo");

        XYChart.Series<String, Number> seriYag = new XYChart.Series<>();
        seriYag.setName("Yağ (kg)");

        XYChart.Series<String, Number> seriKas = new XYChart.Series<>();
        seriKas.setName("Kas (kg)");

        try (Connection conn = DatabaseConnection.connect()) {
            String sql = "SELECT Tarih, Kilo, YagKg, KasKg FROM BoyKiloTakip WHERE UyeID = ? ORDER BY Tarih";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, seciliUyeId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String tarih = rs.getDate("Tarih").toLocalDate().format(dateFormat);

                XYChart.Data<String, Number> dKilo = new XYChart.Data<>(tarih, rs.getDouble("Kilo"));
                XYChart.Data<String, Number> dYag  = new XYChart.Data<>(tarih, rs.getDouble("YagKg"));
                XYChart.Data<String, Number> dKas  = new XYChart.Data<>(tarih, rs.getDouble("KasKg"));

                addTooltip(dKilo, "Kilo: %.1f kg");
                addTooltip(dYag, "Yağ: %.1f kg");
                addTooltip(dKas, "Kas: %.1f kg");

                seriKilo.getData().add(dKilo);
                seriYag.getData().add(dYag);
                seriKas.getData().add(dKas);
            }

        } catch (SQLException e) {
            NotificationHelper.showError("Grafik verileri alınamadı.");
        }

        lineChart.getData().addAll(seriKilo, seriYag, seriKas);
    }

    private void addTooltip(XYChart.Data<String, Number> data, String format) {
        Tooltip tooltip = new Tooltip(String.format(format, data.getYValue().doubleValue()));
        tooltip.setShowDelay(Duration.millis(100));

        if (data.getNode() != null) {
            Tooltip.install(data.getNode(), tooltip);
        }

        data.nodeProperty().addListener((obs, oldNode, newNode) -> {
            if (newNode != null) {
                Tooltip.install(newNode, tooltip);
            }
        });
    }
    


    @FXML
    private void handleGeriDon() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AntrenorEkrani.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnGeriDon.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
            NotificationHelper.showError("Sayfa yüklenemedi: " + e.getMessage());
        }
    }


    
}