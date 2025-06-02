package application.controllers;

import application.DatabaseConnection;
import application.helpers.NotificationHelper;
import application.helpers.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class UyeKiloTakibiController {

    @FXML private Label lblUyeAdSoyad, lblDogumTarihi, lblBoy, lblBMI, lblHedefBMI;
    @FXML private Label lblGuncelKg, lblHedefKg;
    @FXML private LineChart<String, Number> lineChart;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;

    private final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @FXML
    public void initialize() {
        int uyeId = Session.getAktifKullaniciId();
        if (uyeId <= 0) {
            NotificationHelper.showError("Oturum bilgisi bulunamadı.");
            return;
        }
        uyeDetaylariniYukle(uyeId);
        takipVerileriniYukle(uyeId);
    }

    public void handleGeriDon() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/UyeEkrani.fxml"));
            Pane root = loader.load();
            Stage stage = (Stage) lblUyeAdSoyad.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            NotificationHelper.showError("Geri dönülemedi: " + e.getMessage());
        }
    }

    private void uyeDetaylariniYukle(int uyeId) {
        try (Connection conn = DatabaseConnection.connect()) {
            String sql = "SELECT ad, soyad, dogum_tarihi, BoyCm FROM uyeler WHERE id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, uyeId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                lblUyeAdSoyad.setText(rs.getString("ad") + " " + rs.getString("soyad"));
                lblDogumTarihi.setText(rs.getDate("dogum_tarihi").toLocalDate().format(dateFormat));
                int boy = rs.getInt("BoyCm");
                lblBoy.setText(boy + " cm");

                if (boy > 0) guncelBMIGoster(uyeId, boy);
            }
        } catch (SQLException e) {
            NotificationHelper.showError("Üye bilgileri alınamadı.");
        }
    }

    private void guncelBMIGoster(int uyeId, int boyCm) {
        try (Connection conn = DatabaseConnection.connect()) {
            String sql = "SELECT TOP 1 Kilo FROM BoyKiloTakip WHERE UyeID = ? ORDER BY Tarih DESC";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, uyeId);
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

    private void takipVerileriniYukle(int uyeId) {
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
            ps.setInt(1, uyeId);
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
}
