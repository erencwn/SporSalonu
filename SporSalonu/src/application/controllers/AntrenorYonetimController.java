package application.controllers;

import application.DatabaseConnection;
import application.helpers.NotificationHelper;
import application.models.Antrenor;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Files;
import java.sql.*;
import java.time.LocalDate;
import java.util.stream.Collectors;

public class AntrenorYonetimController {

    @FXML private TextField tf_ad;
    @FXML private TextField tf_soyad;
    @FXML private TextField tf_email;
    @FXML private TextField tf_brans;
    @FXML private DatePicker dp_dogumTarihi;
    @FXML private TextField tf_telefon;
    @FXML private PasswordField pf_sifre;
    @FXML private TextField tf_foto;
    @FXML private TextField tf_ara;
    @FXML private ImageView img_antrenor;

    @FXML private TableView<Antrenor> tablo;
    @FXML private TableColumn<Antrenor, String> col_ad;
    @FXML private TableColumn<Antrenor, String> col_soyad;
    @FXML private TableColumn<Antrenor, String> col_email;
    @FXML private TableColumn<Antrenor, String> col_brans;
    @FXML private TableColumn<Antrenor, String> col_dogumTarihi;
    @FXML private TableColumn<Antrenor, String> col_telefon;
    @FXML private TableColumn<Antrenor, String> col_sifre;
    @FXML private TableColumn<Antrenor, String> col_foto;

    private String secilenDosyaYolu = null;
    private ObservableList<Antrenor> tumAntrenorler = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        col_ad.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAd()));
        col_soyad.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSoyad()));
        col_email.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));
        col_brans.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getBrans()));
        col_dogumTarihi.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDogumTarihi()));
        col_telefon.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTelefon()));
        col_sifre.setCellValueFactory(data -> new SimpleStringProperty("\u2022".repeat(data.getValue().getSifre().length())));
        col_foto.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFoto()));

        tablo.setFixedCellSize(25);
        tablo.setItems(tumAntrenorler);
        tablo.setPlaceholder(new Label("Kayıt bulunamadı"));

        tablo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                tf_ad.setText(newVal.getAd());
                tf_soyad.setText(newVal.getSoyad());
                tf_email.setText(newVal.getEmail());
                tf_brans.setText(newVal.getBrans());
                dp_dogumTarihi.setValue(LocalDate.parse(newVal.getDogumTarihi()));
                tf_telefon.setText(newVal.getTelefon());
                pf_sifre.setText(newVal.getSifre());
                tf_foto.setText(newVal.getFoto());
                img_antrenor.setImage(new Image(new File("AntrenorResimleri/" + newVal.getFoto()).toURI().toString()));
            }
        });

        tf_ara.textProperty().addListener((obs, oldVal, newVal) -> filtrele(newVal));

        tabloVeriYukle();
    }

    @FXML
    public void handleFotoSec() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Fotoğraf Seç");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Resim Dosyaları", "*.jpg", "*.jpeg", "*.png"));
        File selectedFile = fileChooser.showOpenDialog(new Stage());

        if (selectedFile != null) {
            secilenDosyaYolu = selectedFile.getAbsolutePath();
            String dosyaAdi = selectedFile.getName();
            tf_foto.setText(dosyaAdi);
            img_antrenor.setImage(new Image(selectedFile.toURI().toString()));
        }
    }

    @FXML
    public void handleEkle() {
        String ad = tf_ad.getText().trim();
        String soyad = tf_soyad.getText().trim();
        String email = tf_email.getText().trim();
        String brans = tf_brans.getText().trim();
        String dogumTarihi = dp_dogumTarihi.getValue().toString();
        String telefon = tf_telefon.getText().trim();
        String sifre = pf_sifre.getText().trim();
        String dosyaAdi = tf_foto.getText().trim();

        if (ad.isEmpty() || soyad.isEmpty() || email.isEmpty() || brans.isEmpty() || dosyaAdi.isEmpty() || telefon.isEmpty() || sifre.isEmpty()) {
            NotificationHelper.showWarning("Lütfen tüm alanları doldurun!");
            return;
        }

        try (Connection conn = DatabaseConnection.connect()) {
            if (secilenDosyaYolu != null) {
                File kaynak = new File(secilenDosyaYolu);
                File hedefKlasor = new File("AntrenorResimleri");
                if (!hedefKlasor.exists()) hedefKlasor.mkdirs();
                File hedef = new File(hedefKlasor, dosyaAdi);
                if (!hedef.exists()) Files.copy(kaynak.toPath(), hedef.toPath());
            }

            String sql = "INSERT INTO antrenorler (ad, soyad, email, brans, dogum_tarihi, telefon, sifre, foto) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, ad);
            ps.setString(2, soyad);
            ps.setString(3, email);
            ps.setString(4, brans);
            ps.setString(5, dogumTarihi);
            ps.setString(6, telefon);
            ps.setString(7, sifre);
            ps.setString(8, dosyaAdi);
            ps.executeUpdate();

            NotificationHelper.showSuccess("Kayıt başarılı!");
            tabloVeriYukle();
            temizle();

        } catch (Exception e) {
            e.printStackTrace();
            NotificationHelper.showError("Hata oluştu: " + e.getMessage());
        }
    }

    @FXML
    public void handleGuncelle() {
        Antrenor secili = tablo.getSelectionModel().getSelectedItem();
        if (secili == null) return;

        try (Connection conn = DatabaseConnection.connect()) {
            String sql = "UPDATE antrenorler SET ad=?, soyad=?, email=?, brans=?, dogum_tarihi=?, telefon=?, sifre=?, foto=? WHERE id=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, tf_ad.getText().trim());
            ps.setString(2, tf_soyad.getText().trim());
            ps.setString(3, tf_email.getText().trim());
            ps.setString(4, tf_brans.getText().trim());
            ps.setString(5, dp_dogumTarihi.getValue().toString());
            ps.setString(6, tf_telefon.getText().trim());
            ps.setString(7, pf_sifre.getText().trim());
            ps.setString(8, tf_foto.getText().trim());
            ps.setInt(9, Integer.parseInt(secili.getId()));

            ps.executeUpdate();
            NotificationHelper.showSuccess("Güncelleme başarılı!");
            tabloVeriYukle();
            temizle();
        } catch (Exception e) {
            e.printStackTrace();
            NotificationHelper.showError("Hata oluştu: " + e.getMessage());
        }
    }

    @FXML
    public void handleSil() {
        Antrenor secili = tablo.getSelectionModel().getSelectedItem();
        if (secili == null) return;

        try (Connection conn = DatabaseConnection.connect()) {
            PreparedStatement ps = conn.prepareStatement("DELETE FROM antrenorler WHERE id = ?");
            ps.setInt(1, Integer.parseInt(secili.getId()));
            ps.executeUpdate();

            NotificationHelper.showSuccess("Antrenör silindi!");
            tabloVeriYukle();
            temizle();
        } catch (Exception e) {
            e.printStackTrace();
            NotificationHelper.showError("Silme işlemi sırasında hata oluştu: " + e.getMessage());
        }
    }

    private void temizle() {
        tf_ad.clear(); tf_soyad.clear(); tf_email.clear(); tf_brans.clear(); dp_dogumTarihi.setValue(null);
        tf_telefon.clear(); pf_sifre.clear(); tf_foto.clear(); img_antrenor.setImage(null);
    }

    private void tabloVeriYukle() {
        tumAntrenorler.clear();
        try (Connection conn = DatabaseConnection.connect()) {
            ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM antrenorler");
            while (rs.next()) {
                Antrenor a = new Antrenor();
                a.setId(String.valueOf(rs.getInt("id")));
                a.setAd(rs.getString("ad"));
                a.setSoyad(rs.getString("soyad"));
                a.setEmail(rs.getString("email"));
                a.setBrans(rs.getString("brans"));
                a.setDogumTarihi(rs.getString("dogum_tarihi"));
                a.setTelefon(rs.getString("telefon"));
                a.setSifre(rs.getString("sifre"));
                a.setFoto(rs.getString("foto"));
                tumAntrenorler.add(a);
            }
            tablo.setItems(FXCollections.observableArrayList(tumAntrenorler));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void filtrele(String anahtar) {
        if (anahtar == null || anahtar.isBlank()) {
            tablo.setItems(FXCollections.observableArrayList(tumAntrenorler));
            return;
        }
        tablo.setItems(tumAntrenorler.stream()
            .filter(a -> (a.getAd() + " " + a.getSoyad()).toLowerCase().contains(anahtar.toLowerCase()))
            .collect(Collectors.toCollection(FXCollections::observableArrayList)));
    }

    @FXML
    public void handleGeriDon() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AdminEkrani.fxml"));
            AnchorPane root = loader.load();
            Stage stage = (Stage) tf_ad.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
            NotificationHelper.showError("Yönlendirme hatası: " + e.getMessage());
        }
    }
}
