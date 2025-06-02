package application.models;

import application.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Antrenor {

    private String id;
    private String ad;
    private String soyad;
    private String email;
    private String brans;
    private String dogumTarihi;
    private String telefon;
    private String foto;
    private String sifre;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAd() {
        return ad;
    }

    public void setAd(String ad) {
        this.ad = ad;
    }

    public String getSoyad() {
        return soyad;
    }

    public void setSoyad(String soyad) {
        this.soyad = soyad;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBrans() {
        return brans;
    }

    public void setBrans(String brans) {
        this.brans = brans;
    }

    public String getDogumTarihi() {
        return dogumTarihi;
    }

    public void setDogumTarihi(String dogumTarihi) {
        this.dogumTarihi = dogumTarihi;
    }

    public String getTelefon() {
        return telefon;
    }

    public void setTelefon(String telefon) {
        this.telefon = telefon;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public String getSifre() {
        return sifre;
    }

    public void setSifre(String sifre) {
        this.sifre = sifre;
    }

    public static Antrenor girisKontrol(String email, String sifre) {
        try (Connection conn = DatabaseConnection.connect()) {
            String sql = "SELECT * FROM antrenorler WHERE email = ? AND sifre = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, email);
            ps.setString(2, sifre);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
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
                return a;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
