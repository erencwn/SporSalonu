package application.models;

import application.DatabaseConnection;
import java.sql.*;

public class AdminModel {

    Connection conn;

    public AdminModel() {
        conn = DatabaseConnection.connect();
    }

    public boolean adminGirisKontrol(String email, String adminKey) {
        String sorgu = "SELECT * FROM admin WHERE email = ? AND admin_key = ?";
        try {
            PreparedStatement ps = conn.prepareStatement(sorgu);
            ps.setString(1, email);
            ps.setString(2, adminKey);
            ResultSet rs = ps.executeQuery();

            return rs.next();
        } catch (SQLException e) {
            System.out.println("Admin giriş kontrol hatası: " + e.getMessage());
            return false;
        }
    }

    public boolean adminGirisKontrolByKey(String adminKey) {
        String sorgu = "SELECT * FROM admin WHERE admin_key = ?";
        try {
            PreparedStatement ps = conn.prepareStatement(sorgu);
            ps.setString(1, adminKey);
            ResultSet rs = ps.executeQuery();

            return rs.next();
        } catch (SQLException e) {
            System.out.println("Admin key giriş kontrol hatası: " + e.getMessage());
            return false;
        }
    }

    public ResultSet adminBilgileriGetir(String email) {
        String sorgu = "SELECT * FROM admin WHERE email = ?";
        try {
            PreparedStatement ps = conn.prepareStatement(sorgu);
            ps.setString(1, email);
            return ps.executeQuery();
        } catch (SQLException e) {
            System.out.println("Admin bilgileri çekme hatası: " + e.getMessage());
            return null;
        }
    }
}
