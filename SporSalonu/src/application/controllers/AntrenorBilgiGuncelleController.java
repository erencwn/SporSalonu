package application.controllers;

import application.DatabaseConnection;
import application.helpers.Session;
import application.helpers.NotificationHelper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.scene.Node;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ResourceBundle;
import java.io.IOException;

public class AntrenorBilgiGuncelleController implements Initializable {

    @FXML private TextField txtAd;
    @FXML private TextField txtSoyad;
    @FXML private TextField txtEmail;
    @FXML private TextField txtTelefon;
    @FXML private TextField txtBrans;
    @FXML private TextField txtSifre;
    @FXML private DatePicker dateDogumTarihi;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        bilgileriGetir();
    }

    private void bilgileriGetir() {
        try (Connection conn = DatabaseConnection.connect()) {
            String kullaniciEmail = Session.getAktifKullaniciEmail(); 

            String sql = "SELECT * FROM antrenorler WHERE email=?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, kullaniciEmail);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                txtAd.setText(rs.getString("ad"));
                txtSoyad.setText(rs.getString("soyad"));
                txtEmail.setText(rs.getString("email"));
                txtTelefon.setText(rs.getString("telefon"));
                txtBrans.setText(rs.getString("brans"));
                txtSifre.setText(rs.getString("sifre"));
                if (rs.getDate("dogum_tarihi") != null) {
                    dateDogumTarihi.setValue(rs.getDate("dogum_tarihi").toLocalDate());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            NotificationHelper.showError("Bilgiler getirilirken hata oluştu: " + e.getMessage());
        }
    }

    @FXML
    private void handleBilgileriKaydet() {
        try (Connection conn = DatabaseConnection.connect()) {
            String sql = "UPDATE antrenorler SET ad=?, soyad=?, email=?, telefon=?, brans=?, sifre=?, dogum_tarihi=? WHERE email=?";
            PreparedStatement stmt = conn.prepareStatement(sql);

            stmt.setString(1, txtAd.getText().trim());
            stmt.setString(2, txtSoyad.getText().trim());
            stmt.setString(3, txtEmail.getText().trim()); 
            stmt.setString(4, txtTelefon.getText().trim());
            stmt.setString(5, txtBrans.getText().trim());
            stmt.setString(6, txtSifre.getText());
            stmt.setString(7, dateDogumTarihi.getValue().toString());
            stmt.setString(8, Session.getAktifKullaniciEmail()); 
            int rowsUpdated = stmt.executeUpdate();

            if (rowsUpdated > 0) {
                NotificationHelper.showSuccess("Bilgileriniz güncellendi!");
                Session.setAktifKullaniciEmail(txtEmail.getText().trim());
            } else {
                NotificationHelper.showError("Güncelleme başarısız!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            NotificationHelper.showError("Bir hata oluştu: " + e.getMessage());
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
