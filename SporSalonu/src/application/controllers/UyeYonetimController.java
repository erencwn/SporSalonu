package application.controllers;

import application.DatabaseConnection;
import application.helpers.NotificationHelper;
import application.models.UyeModel;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDate;
import java.util.stream.Collectors;

public class UyeYonetimController {

    @FXML private TextField tf_ad, tf_soyad, tf_email, tf_telefon, pf_sifre, tf_boy, tf_ara;
    @FXML private DatePicker dp_dogumTarihi;

    @FXML private TableView<UyeModel> tablo;
    @FXML private TableColumn<UyeModel, String> col_ad, col_soyad, col_email, col_dogumTarihi, col_telefon, col_sifre, col_boy, col_odeme;

    private final ObservableList<UyeModel> tumUyeler = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        col_ad.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAd()));
        col_soyad.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSoyad()));
        col_email.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));
        col_dogumTarihi.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDogumTarihi().toLocalDate().toString()));
        col_telefon.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTelefon()));
        col_sifre.setCellValueFactory(data -> new SimpleStringProperty("●".repeat(data.getValue().getSifre().length())));
        col_boy.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getBoyCm() + " cm"));
        col_odeme.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getOdemeDurumu()));

        col_odeme.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle("-fx-font-weight: bold;");
                    if (item.equalsIgnoreCase("Onaylandı")) {
                        setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    } else if (item.equalsIgnoreCase("Bekleniyor")) {
                        setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: gray;");
                    }
                }
            }
        });

        tablo.setItems(tumUyeler);
        tablo.setPlaceholder(new Label("Kayıt bulunamadı"));

        tablo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                tf_ad.setText(newVal.getAd());
                tf_soyad.setText(newVal.getSoyad());
                tf_email.setText(newVal.getEmail());
                tf_telefon.setText(newVal.getTelefon());
                pf_sifre.setText(newVal.getSifre());
                tf_boy.setText(String.valueOf(newVal.getBoyCm()));
                dp_dogumTarihi.setValue(newVal.getDogumTarihi().toLocalDate());
            }
        });

        if (tf_ara != null) {
            tf_ara.textProperty().addListener((obs, oldVal, newVal) -> filtrele(newVal));
        }

        tabloVeriYukle();
    }

    private void tabloVeriYukle() {
        tumUyeler.clear();
        try (Connection conn = DatabaseConnection.connect()) {
            String sql = "SELECT u.id, u.ad, u.soyad, u.email, u.telefon, u.sifre, u.dogum_tarihi, u.BoyCm," +
                    "ISNULL((SELECT TOP 1 CASE WHEN OdemeDurum = 1 THEN 'Onaylandı' ELSE 'Bekleniyor' END " +
                    "FROM Odemeler o WHERE o.UyeID = u.id ORDER BY OdemeTarihi DESC), 'Yok') AS OdemeDurumu " +
                    "FROM uyeler u";
            ResultSet rs = conn.createStatement().executeQuery(sql);
            while (rs.next()) {
                UyeModel uye = new UyeModel();
                uye.setId(rs.getInt("id"));
                uye.setAd(rs.getString("ad"));
                uye.setSoyad(rs.getString("soyad"));
                uye.setEmail(rs.getString("email"));
                uye.setTelefon(rs.getString("telefon"));
                uye.setSifre(rs.getString("sifre"));
                uye.setDogumTarihi(rs.getDate("dogum_tarihi"));
                uye.setBoyCm(rs.getInt("BoyCm"));
                uye.setOdemeDurumu(rs.getString("OdemeDurumu"));
                tumUyeler.add(uye);
            }
        } catch (Exception e) {
            NotificationHelper.showError("Veri yüklenemedi: " + e.getMessage());
        }
    }

    @FXML
    public void handleEkle() {
        try (Connection conn = DatabaseConnection.connect()) {
            String sql = "INSERT INTO uyeler (ad, soyad, email, telefon, sifre, dogum_tarihi, kayit_tarihi, BoyCm) VALUES (?, ?, ?, ?, ?, ?, GETDATE(), ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, tf_ad.getText().trim());
            ps.setString(2, tf_soyad.getText().trim());
            ps.setString(3, tf_email.getText().trim());
            ps.setString(4, tf_telefon.getText().trim());
            ps.setString(5, pf_sifre.getText().trim());
            ps.setDate(6, Date.valueOf(dp_dogumTarihi.getValue()));
            ps.setInt(7, Integer.parseInt(tf_boy.getText().trim()));
            ps.executeUpdate();
            NotificationHelper.showSuccess("Üye eklendi.");
            temizle();
            tabloVeriYukle();
        } catch (Exception e) {
            NotificationHelper.showError("Ekleme hatası: " + e.getMessage());
        }
    }

    @FXML
    public void handleGuncelle() {
        UyeModel secili = tablo.getSelectionModel().getSelectedItem();
        if (secili == null) return;

        try (Connection conn = DatabaseConnection.connect()) {
            String sql = "UPDATE uyeler SET ad=?, soyad=?, email=?, telefon=?, sifre=?, dogum_tarihi=?, BoyCm=? WHERE id=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, tf_ad.getText().trim());
            ps.setString(2, tf_soyad.getText().trim());
            ps.setString(3, tf_email.getText().trim());
            ps.setString(4, tf_telefon.getText().trim());
            ps.setString(5, pf_sifre.getText().trim());
            ps.setDate(6, Date.valueOf(dp_dogumTarihi.getValue()));
            ps.setInt(7, Integer.parseInt(tf_boy.getText().trim()));
            ps.setInt(8, secili.getId());
            ps.executeUpdate();
            NotificationHelper.showSuccess("Güncelleme başarılı.");
            temizle();
            tabloVeriYukle();
        } catch (Exception e) {
            NotificationHelper.showError("Güncelleme hatası: " + e.getMessage());
        }
    }

    @FXML
    public void handleSil() {
        UyeModel secili = tablo.getSelectionModel().getSelectedItem();
        if (secili == null) return;

        try (Connection conn = DatabaseConnection.connect()) {
            PreparedStatement ps = conn.prepareStatement("DELETE FROM uyeler WHERE id = ?");
            ps.setInt(1, secili.getId());
            ps.executeUpdate();
            NotificationHelper.showSuccess("Üye silindi.");
            temizle();
            tabloVeriYukle();
        } catch (Exception e) {
            NotificationHelper.showError("Silme hatası: " + e.getMessage());
        }
    }

    @FXML
    public void handleGeriDon() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AdminEkrani.fxml"));
            AnchorPane root = loader.load();
            Stage stage = (Stage) tf_ad.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            NotificationHelper.showError("Geri dönüş hatası: " + e.getMessage());
        }
    }

    @FXML
    public void handleOdemeYonetimi() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/OdemeYonetimi.fxml"));
            AnchorPane root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Ödeme Yönetimi");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            NotificationHelper.showError("Ödeme yönetimi ekranı açılamadı: " + e.getMessage());
        }
    }

    private void temizle() {
        tf_ad.clear(); tf_soyad.clear(); tf_email.clear(); tf_telefon.clear();
        pf_sifre.clear(); tf_boy.clear(); dp_dogumTarihi.setValue(null);
    }

    private void filtrele(String anahtar) {
        if (anahtar == null || anahtar.isBlank()) {
            tablo.setItems(tumUyeler);
            return;
        }
        tablo.setItems(tumUyeler.stream()
                .filter(u -> (u.getAd() + " " + u.getSoyad()).toLowerCase().contains(anahtar.toLowerCase()))
                .collect(Collectors.toCollection(FXCollections::observableArrayList)));
    }
}
