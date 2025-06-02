package application.controllers;

import application.DatabaseConnection;
import application.helpers.NotificationHelper;
import application.helpers.Session;
import application.models.OdemeModel;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.sql.*;
import java.time.LocalDate;

public class OdemeTakibiController {

    @FXML private TextField txtTutar, txtAciklama, txtKartNo, txtSKT, txtCVV;
    @FXML private DatePicker dpTarih, dpBaslangicTarihi, dpBitisTarihi;
    @FXML private ComboBox<String> cmbOdemeYontemi;
    @FXML private Label lblToplamTutar, lblDekontYolu;
    @FXML private TableView<OdemeModel> tblOdemeler;
    @FXML private TableColumn<OdemeModel, Double> colTutar;
    @FXML private TableColumn<OdemeModel, Date> colTarih;
    @FXML private TableColumn<OdemeModel, String> colYontem, colAciklama, colDurum, colDekont;
    @FXML private VBox krediKartiAlanlari, dekontAlanlari;

    private ObservableList<OdemeModel> odemeListesi = FXCollections.observableArrayList();
    private String secilenDekontYolu = null;

    @FXML
    public void initialize() {
        cmbOdemeYontemi.setItems(FXCollections.observableArrayList("Nakit", "Kredi Kartı", "Havale"));
        colTutar.setCellValueFactory(cell -> new javafx.beans.property.SimpleObjectProperty<>(cell.getValue().getTutar()));
        colTarih.setCellValueFactory(cell -> new javafx.beans.property.SimpleObjectProperty<>(cell.getValue().getTarih()));
        colYontem.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getYontem()));
        colAciklama.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getAciklama()));
        colDurum.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDurum() == 1 ? "Onaylandı" : "Bekleniyor"));
        colDekont.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Görüntüle");

            {
                btn.getStyleClass().add("button-small-blue");
                btn.setOnAction(event -> {
                    OdemeModel odeme = getTableView().getItems().get(getIndex());
                    if (odeme.getDekontDosya() != null) {
                        try {
                            Desktop.getDesktop().open(new File(odeme.getDekontDosya()));
                        } catch (IOException e) {
                            NotificationHelper.showError("Dosya açılamadı: " + e.getMessage());
                        }
                    }
                });
            }

            @Override
            protected void updateItem(String path, boolean empty) {
                super.updateItem(path, empty);
                if (empty || path == null) {
                    setGraphic(null);
                } else {
                    setGraphic(btn);
                }
            }
        });
        colDekont.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDekontDosya()));

        loadOdemeler();
        kontrolEtVeUyar();
    }

    private void loadOdemeler() {
        odemeListesi.clear();
        try (Connection conn = DatabaseConnection.connect()) {
            String sql = "SELECT Tutar, OdemeTarihi, OdemeYontemi, Aciklama, OdemeDurum, DekontDosya FROM Odemeler WHERE UyeID = (SELECT id FROM Uyeler WHERE Email = ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, Session.getAktifKullaniciEmail());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                odemeListesi.add(new OdemeModel(
                        rs.getDouble("Tutar"),
                        rs.getDate("OdemeTarihi"),
                        rs.getString("OdemeYontemi"),
                        rs.getString("Aciklama"),
                        rs.getInt("OdemeDurum"),
                        rs.getString("DekontDosya")
                ));
            }
            tblOdemeler.setItems(odemeListesi);
            toplamTutarGuncelle();
        } catch (SQLException e) {
            NotificationHelper.showError("Veritabanı hatası: " + e.getMessage());
        }
    }

    private void toplamTutarGuncelle() {
        double toplam = odemeListesi.stream().mapToDouble(OdemeModel::getTutar).sum();
        lblToplamTutar.setText(String.format("₺ %.2f", toplam));
    }

    @FXML
    private void handleOdemeYontemiSecildi() {
        String secilen = cmbOdemeYontemi.getValue();
        boolean krediKarti = "Kredi Kartı".equals(secilen);
        boolean nakitHavale = "Nakit".equals(secilen) || "Havale".equals(secilen);

        krediKartiAlanlari.setVisible(krediKarti);
        krediKartiAlanlari.setManaged(krediKarti);
        dekontAlanlari.setVisible(nakitHavale);
        dekontAlanlari.setManaged(nakitHavale);
    }

    @FXML
    private void handleDekontSec() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("PDF Dekont Seçiniz");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Dosyaları", "*.pdf"));
        File secilen = fileChooser.showOpenDialog(null);
        if (secilen != null) {
            try {
                Path hedefKlasor = Paths.get("Dekont");
                Files.createDirectories(hedefKlasor);
                Path hedef = hedefKlasor.resolve(secilen.getName());
                Files.copy(secilen.toPath(), hedef, StandardCopyOption.REPLACE_EXISTING);
                secilenDekontYolu = hedef.toString();
                lblDekontYolu.setText("Seçilen dosya: " + hedef.getFileName());
            } catch (IOException e) {
                NotificationHelper.showError("Dekont kopyalanırken hata oluştu.");
            }
        }
    }

    @FXML
    private void handleOdemeYap() {
        try {
            double tutar = Double.parseDouble(txtTutar.getText());
            LocalDate tarih = dpTarih.getValue();
            String yontem = cmbOdemeYontemi.getValue();
            String aciklama = txtAciklama.getText().isEmpty() ? "Manuel Ödeme" : txtAciklama.getText();

            if (tarih == null || yontem == null) {
                NotificationHelper.showWarning("Tarih ve ödeme yöntemi zorunludur.");
                return;
            }

            int odemeDurum = yontem.equals("Kredi Kartı") ? 1 : 0;
            if (odemeDurum == 0 && secilenDekontYolu == null) {
                NotificationHelper.showWarning("Dekont PDF dosyası yükleyiniz.");
                return;
            }

            try (Connection conn = DatabaseConnection.connect()) {
                String sql = "INSERT INTO Odemeler (UyeID, Tutar, OdemeTarihi, OdemeYontemi, Aciklama, OdemeDurum, DekontDosya) VALUES ((SELECT id FROM Uyeler WHERE Email = ?), ?, ?, ?, ?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, Session.getAktifKullaniciEmail());
                stmt.setDouble(2, tutar);
                stmt.setDate(3, Date.valueOf(tarih));
                stmt.setString(4, yontem);
                stmt.setString(5, aciklama);
                stmt.setInt(6, odemeDurum);
                stmt.setString(7, secilenDekontYolu);
                stmt.executeUpdate();
            }

            NotificationHelper.showSuccess("Ödeme kaydedildi.");
            temizleForm();
            loadOdemeler();
        } catch (Exception e) {
            NotificationHelper.showError("Hata oluştu: " + e.getMessage());
        }
    }

    private void temizleForm() {
        txtTutar.clear();
        dpTarih.setValue(null);
        cmbOdemeYontemi.getSelectionModel().clearSelection();
        txtAciklama.clear();
        txtKartNo.clear();
        txtSKT.clear();
        txtCVV.clear();
        lblDekontYolu.setText("Seçilen dosya: -");
        secilenDekontYolu = null;
        handleOdemeYontemiSecildi();
    }

    @FXML
    private void handleFiltrele() {
        LocalDate baslangic = dpBaslangicTarihi.getValue();
        LocalDate bitis = dpBitisTarihi.getValue();

        if (baslangic == null || bitis == null) {
            NotificationHelper.showWarning("Tarih aralığı seçiniz.");
            return;
        }

        ObservableList<OdemeModel> filtreli = odemeListesi.filtered(odeme ->
                !odeme.getTarih().toLocalDate().isBefore(baslangic) &&
                        !odeme.getTarih().toLocalDate().isAfter(bitis)
        );
        tblOdemeler.setItems(filtreli);
    }

    @FXML
    private void handleFiltreSifirla() {
        tblOdemeler.setItems(odemeListesi);
        dpBaslangicTarihi.setValue(null);
        dpBitisTarihi.setValue(null);
    }

    private void kontrolEtVeUyar() {
        LocalDate bugun = LocalDate.now();
        boolean uyar = odemeListesi.stream().anyMatch(odeme ->
                odeme.getTarih().toLocalDate().isAfter(bugun) &&
                        odeme.getTarih().toLocalDate().isBefore(bugun.plusDays(4))
        );

        if (uyar) {
            NotificationHelper.showWarning("Ödeme tarihiniz yaklaşıyor!");
        }
    }

    @FXML
    private void handleGeriDon() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/UyeEkrani.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) tblOdemeler.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            NotificationHelper.showError("Geri dönüş sırasında hata oluştu: " + e.getMessage());
        }
    }
}
