package application.controllers;

import application.DatabaseConnection;
import application.helpers.NotificationHelper;
import application.models.UyeModel;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class AntrenorAntrenmanProgramiController implements Initializable {

    @FXML private TableView<UyeModel> tblUyeler;
    @FXML private TableColumn<UyeModel, String> colAd, colSoyad, colEmail, colTelefon, colProgramTarihi;
    @FXML private TextField tfAra;

    @FXML private Label lblBoy, lblGuncelKilo, lblHedefKilo;
    @FXML private ComboBox<String> cbHareket1, cbHareket2, cbHareket3, cbHareket4, cbHareket5;
    @FXML private Label lblHareket1, lblHareket2, lblHareket3, lblHareket4, lblHareket5;
    @FXML private ImageView imgHareket1, imgHareket2, imgHareket3, imgHareket4, imgHareket5;
    @FXML private Label lblKategori1, lblKategori2, lblKategori3, lblKategori4, lblKategori5;
    @FXML private TextField tfTekrar1, tfTekrar2, tfTekrar3, tfTekrar4, tfTekrar5;
    @FXML private Button btnKaydet, btnGuncelle, btnSil, btnGeriDon;

    private int seciliUyeId = -1;
    private final ObservableList<UyeModel> uyelerListesi = FXCollections.observableArrayList();
    private final FilteredList<UyeModel> filtreliUyeler = new FilteredList<>(uyelerListesi, e -> true);
    private final ObservableList<String> hareketlerListesi = FXCollections.observableArrayList();
    private final Map<Integer, String> hareketAdlari = new HashMap<>();
    private final Map<Integer, String> hareketKategorileri = new HashMap<>();
    private final Map<Integer, String> hareketGifYollari = new HashMap<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colAd.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getAd()));
        colSoyad.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getSoyad()));
        colEmail.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getEmail()));
        colTelefon.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getTelefon()));
        colProgramTarihi.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getProgramTarihi()));

        tfAra.textProperty().addListener((obs, oldVal, newVal) -> {
            filtreliUyeler.setPredicate(uye -> {
                if (newVal == null || newVal.isEmpty()) return true;
                String filtre = newVal.toLowerCase();
                return uye.getAd().toLowerCase().contains(filtre) || uye.getSoyad().toLowerCase().contains(filtre);
            });
        });

        tblUyeler.setItems(filtreliUyeler);
        tblUyeler.setFixedCellSize(30);
        tblUyeler.setMaxHeight(300);

        tblUyeler.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                seciliUyeId = newVal.getId();
                uyeBilgileriGetir();
                programYukle(seciliUyeId);
            }
        });

        hareketleriYukle();
        FXCollections.observableArrayList(cbHareket1, cbHareket2, cbHareket3, cbHareket4, cbHareket5)
                .forEach(cb -> cb.setItems(hareketlerListesi));

        comboboxDinle(cbHareket1, lblHareket1, lblKategori1, imgHareket1);
        comboboxDinle(cbHareket2, lblHareket2, lblKategori2, imgHareket2);
        comboboxDinle(cbHareket3, lblHareket3, lblKategori3, imgHareket3);
        comboboxDinle(cbHareket4, lblHareket4, lblKategori4, imgHareket4);
        comboboxDinle(cbHareket5, lblHareket5, lblKategori5, imgHareket5);

        uyeleriYukle();
    }

    private void programYukle(int uyeId) {
        try (Connection conn = DatabaseConnection.connect()) {
            String sql = "SELECT TOP 1 * FROM antrenman_programi WHERE uye_id = ? ORDER BY tarih DESC";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, uyeId);
            ResultSet rs = ps.executeQuery();

            ComboBox[] comboBoxes = {cbHareket1, cbHareket2, cbHareket3, cbHareket4, cbHareket5};
            TextField[] tekrarlar = {tfTekrar1, tfTekrar2, tfTekrar3, tfTekrar4, tfTekrar5};

            if (rs.next()) {
                for (int i = 0; i < 5; i++) {
                    int hareketId = rs.getInt("hareket_id_" + (i + 1));
                    if (hareketAdlari.containsKey(hareketId)) {
                        String hareketText = hareketId + " - " + hareketAdlari.get(hareketId) + " (" + hareketKategorileri.get(hareketId) + ")";
                        comboBoxes[i].setValue(hareketText);
                    } else {
                        comboBoxes[i].getSelectionModel().clearSelection();
                    }
                    tekrarlar[i].setText(rs.getString("tekrar_" + (i + 1)));
                }
            } else {
                for (ComboBox<String> cb : comboBoxes) cb.getSelectionModel().clearSelection();
                for (TextField tf : tekrarlar) tf.clear();
            }
        } catch (SQLException e) {
            NotificationHelper.showError("Program bilgisi alınamadı: " + e.getMessage());
        }
    }

    @FXML
    private void handleKaydet() {
        if (seciliUyeId == -1) {
            NotificationHelper.showWarning("Lütfen bir üye seçin!");
            return;
        }

        try (Connection conn = DatabaseConnection.connect()) {
        	String sql = "INSERT INTO antrenman_programi (uye_id, hareket_id_1, tekrar_1, hareket_id_2, tekrar_2, hareket_id_3, tekrar_3, hareket_id_4, tekrar_4, hareket_id_5, tekrar_5, tarih) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, GETDATE())";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, seciliUyeId);
            ps.setInt(2, secilenHareket(cbHareket1));
            ps.setString(3, tfTekrar1.getText().trim());
            ps.setInt(4, secilenHareket(cbHareket2));
            ps.setString(5, tfTekrar2.getText().trim());
            ps.setInt(6, secilenHareket(cbHareket3));
            ps.setString(7, tfTekrar3.getText().trim());
            ps.setInt(8, secilenHareket(cbHareket4));
            ps.setString(9, tfTekrar4.getText().trim());
            ps.setInt(10, secilenHareket(cbHareket5));
            ps.setString(11, tfTekrar5.getText().trim());

            ps.executeUpdate();
            NotificationHelper.showSuccess("Antrenman programı kaydedildi.");
            uyeleriYukle();
        } catch (SQLException e) {
            NotificationHelper.showError("Kayıt hatası: " + e.getMessage());
        }
    }

    private void comboboxDinle(ComboBox<String> comboBox, Label lblHareket, Label lblKategori, ImageView imgView) {
        comboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                try {
                    int hareketId = Integer.parseInt(newVal.split(" - ")[0]);
                    String ad = hareketAdlari.getOrDefault(hareketId, "?");
                    String kategori = hareketKategorileri.getOrDefault(hareketId, "?");
                    String gif = hareketGifYollari.get(hareketId);

                    lblHareket.setText(ad + " (" + kategori + ")");
                    lblKategori.setText("Kategori: " + kategori);
                    if (gif != null && !gif.isEmpty()) {
                        imgView.setImage(new Image(getClass().getResourceAsStream(gif)));
                    } else {
                        imgView.setImage(null);
                    }
                } catch (Exception e) {
                    NotificationHelper.showError("Hareket bilgisi yüklenemedi.");
                }
            } else {
                lblHareket.setText("-");
                lblKategori.setText("Kategori: -");
                imgView.setImage(null);
            }
        });
    }

    private int secilenHareket(ComboBox<String> comboBox) {
        try {
            if (comboBox.getValue() == null || comboBox.getValue().isEmpty()) return 0;
            return Integer.parseInt(comboBox.getValue().split(" - ")[0]);
        } catch (Exception e) {
            return 0;
        }
    }

    private void hareketleriYukle() {
        hareketlerListesi.clear();
        hareketAdlari.clear();
        hareketKategorileri.clear();
        hareketGifYollari.clear();

        try (Connection conn = DatabaseConnection.connect()) {
            PreparedStatement ps = conn.prepareStatement("SELECT hareket_id, hareket_adi, kategori, gif_yolu FROM hareketler");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("hareket_id");
                String ad = rs.getString("hareket_adi");
                String kategori = rs.getString("kategori");
                String gif = rs.getString("gif_yolu");

                hareketAdlari.put(id, ad);
                hareketKategorileri.put(id, kategori);
                hareketGifYollari.put(id, gif);
                hareketlerListesi.add(id + " - " + ad + " (" + kategori + ")");
            }
        } catch (SQLException e) {
            NotificationHelper.showError("Hareketler yüklenemedi: " + e.getMessage());
        }
    }

    private void uyeleriYukle() {
        uyelerListesi.clear();
        try (Connection conn = DatabaseConnection.connect()) {
            String sql = "SELECT id, ad, soyad, email, telefon FROM uyeler";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                UyeModel uye = new UyeModel();
                uye.setId(rs.getInt("id"));
                uye.setAd(rs.getString("ad"));
                uye.setSoyad(rs.getString("soyad"));
                uye.setEmail(rs.getString("email"));
                uye.setTelefon(rs.getString("telefon"));
                uye.setProgramTarihi(programTarihiGetir(uye.getId()));
                uyelerListesi.add(uye);
            }
        } catch (SQLException e) {
            NotificationHelper.showError("Üyeler yüklenemedi: " + e.getMessage());
        }
    }

    private String programTarihiGetir(int uyeId) {
        try (Connection conn = DatabaseConnection.connect()) {
            String sql = "SELECT TOP 1 tarih FROM antrenman_programi WHERE uye_id = ? ORDER BY tarih DESC";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, uyeId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                LocalDate tarih = rs.getDate("tarih").toLocalDate();
                return tarih.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            }
        } catch (SQLException e) {
            return "-";
        }
        return "Yok";
    }

    private void uyeBilgileriGetir() {
        try (Connection conn = DatabaseConnection.connect()) {
            String sql = "SELECT BoyCm FROM uyeler WHERE id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, seciliUyeId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int boy = rs.getInt("BoyCm");
                lblBoy.setText(boy + " cm");
                guncelKgHesapla(conn, boy);
            }
        } catch (SQLException e) {
            NotificationHelper.showError("Üye bilgisi alınamadı: " + e.getMessage());
        }
    }

    private void guncelKgHesapla(Connection conn, int boyCm) throws SQLException {
        String sql = "SELECT TOP 1 Kilo FROM BoyKiloTakip WHERE UyeID = ? ORDER BY Tarih DESC";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, seciliUyeId);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            double kilo = rs.getDouble("Kilo");
            lblGuncelKilo.setText(String.format("%.1f kg", kilo));
            double hedef = 22 * Math.pow(boyCm / 100.0, 2);
            lblHedefKilo.setText(String.format("%.1f kg", hedef));
        }
    }

    @FXML
    private void handleGuncelle() {
        if (seciliUyeId == -1) {
            NotificationHelper.showWarning("Lütfen bir üye seçin!");
            return;
        }

        try (Connection conn = DatabaseConnection.connect()) {
            String sql = "UPDATE antrenman_programi SET " +
                         "hareket_id_1 = ?, tekrar_1 = ?, " +
                         "hareket_id_2 = ?, tekrar_2 = ?, " +
                         "hareket_id_3 = ?, tekrar_3 = ?, " +
                         "hareket_id_4 = ?, tekrar_4 = ?, " +
                         "hareket_id_5 = ?, tekrar_5 = ? " +
                         "WHERE uye_id = ? AND tarih = (SELECT MAX(tarih) FROM antrenman_programi WHERE uye_id = ?)";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, secilenHareket(cbHareket1));
            ps.setString(2, tfTekrar1.getText().trim());
            ps.setInt(3, secilenHareket(cbHareket2));
            ps.setString(4, tfTekrar2.getText().trim());
            ps.setInt(5, secilenHareket(cbHareket3));
            ps.setString(6, tfTekrar3.getText().trim());
            ps.setInt(7, secilenHareket(cbHareket4));
            ps.setString(8, tfTekrar4.getText().trim());
            ps.setInt(9, secilenHareket(cbHareket5));
            ps.setString(10, tfTekrar5.getText().trim());
            ps.setInt(11, seciliUyeId);
            ps.setInt(12, seciliUyeId);

            int affected = ps.executeUpdate();
            if (affected > 0) {
                NotificationHelper.showSuccess("Antrenman programı güncellendi.");
                uyeleriYukle();
            } else {
                NotificationHelper.showWarning("Güncellenecek kayıt bulunamadı.");
            }
        } catch (SQLException e) {
            NotificationHelper.showError("Güncelleme hatası: " + e.getMessage());
        }
    }

    @FXML
    private void handleSil() {
        if (seciliUyeId == -1) {
            NotificationHelper.showWarning("Lütfen bir üye seçin!");
            return;
        }

        try (Connection conn = DatabaseConnection.connect()) {
            String sql = "DELETE FROM antrenman_programi WHERE uye_id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, seciliUyeId);
            int silinen = ps.executeUpdate();

            if (silinen > 0) {
                NotificationHelper.showSuccess("Antrenman programı silindi.");
            } else {
                NotificationHelper.showWarning("Silinecek program bulunamadı.");
            }

            ComboBox[] cbs = {cbHareket1, cbHareket2, cbHareket3, cbHareket4, cbHareket5};
            TextField[] tfs = {tfTekrar1, tfTekrar2, tfTekrar3, tfTekrar4, tfTekrar5};
            for (ComboBox cb : cbs) cb.getSelectionModel().clearSelection();
            for (TextField tf : tfs) tf.clear();

            uyeleriYukle();
        } catch (SQLException e) {
            NotificationHelper.showError("Silme hatası: " + e.getMessage());
        }
    }

    
    @FXML
    private void handleGeriDon(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/AntrenorEkrani.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}