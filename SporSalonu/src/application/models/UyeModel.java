package application.models;

import application.DatabaseConnection;
import java.sql.*;

public class UyeModel {
    private int id;
    private String ad;
    private String soyad;
    private String email;
    private String telefon;
    private Date dogumTarihi;
    private int boyCm;
    private String sifre;
    private String odemeDurumu;
    private String programTarihi;

    Connection conn;

    public UyeModel() {
        conn = DatabaseConnection.connect();
    }

    public boolean uyeEkle(String ad, String soyad, String email, String sifre, String telefon, Date dogumTarihi) {
        String sorgu = "INSERT INTO uyeler (ad, soyad, email, sifre, telefon, dogum_tarihi, kayit_tarihi) VALUES (?, ?, ?, ?, ?, ?, GETDATE())";
        try {
            PreparedStatement ps = conn.prepareStatement(sorgu);
            ps.setString(1, ad);
            ps.setString(2, soyad);
            ps.setString(3, email);
            ps.setString(4, sifre);
            ps.setString(5, telefon);
            ps.setDate(6, dogumTarihi);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Üye ekleme hatası: " + e.getMessage());
            return false;
        }
    }

    public UyeModel girisKontrol(String email, String sifre) {
        String sorgu = "SELECT * FROM uyeler WHERE email = ? AND sifre = ?";
        try {
            PreparedStatement ps = conn.prepareStatement(sorgu);
            ps.setString(1, email);
            ps.setString(2, sifre);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                UyeModel uye = new UyeModel();
                uye.setId(rs.getInt("id"));
                uye.setAd(rs.getString("ad"));
                uye.setSoyad(rs.getString("soyad"));
                uye.setEmail(rs.getString("email"));
                uye.setTelefon(rs.getString("telefon"));
                uye.setDogumTarihi(rs.getDate("dogum_tarihi"));
                uye.setBoyCm(rs.getInt("BoyCm"));
                uye.setSifre(rs.getString("sifre"));
                return uye;
            }
        } catch (SQLException e) {
            System.out.println("Üye giriş kontrol hatası: " + e.getMessage());
        }
        return null;
    }

    public ResultSet uyeBilgileriGetir(String email) {
        String sorgu = "SELECT * FROM uyeler WHERE email = ?";
        try {
            PreparedStatement ps = conn.prepareStatement(sorgu);
            ps.setString(1, email);
            return ps.executeQuery();
        } catch (SQLException e) {
            System.out.println("Üye bilgileri çekme hatası: " + e.getMessage());
            return null;
        }
    }

    public boolean boyGuncelle(int id, int boyCm) {
        String sql = "UPDATE uyeler SET BoyCm = ? WHERE id = ?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, boyCm);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Boy güncelleme hatası: " + e.getMessage());
            return false;
        }
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getAd() { return ad; }
    public void setAd(String ad) { this.ad = ad; }

    public String getSoyad() { return soyad; }
    public void setSoyad(String soyad) { this.soyad = soyad; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefon() { return telefon; }
    public void setTelefon(String telefon) { this.telefon = telefon; }

    public Date getDogumTarihi() { return dogumTarihi; }
    public void setDogumTarihi(Date dogumTarihi) { this.dogumTarihi = dogumTarihi; }

    public int getBoyCm() { return boyCm; }
    public void setBoyCm(int boyCm) { this.boyCm = boyCm; }

    public String getSifre() { return sifre; }
    public void setSifre(String sifre) { this.sifre = sifre; }

    public String getOdemeDurumu() { return odemeDurumu; }
    public void setOdemeDurumu(String odemeDurumu) { this.odemeDurumu = odemeDurumu; }

    public String getProgramTarihi() { return programTarihi; }
    public void setProgramTarihi(String programTarihi) { this.programTarihi = programTarihi; }

    @Override
    public String toString() {
        return id + " - " + ad + " " + soyad;
    }
}
