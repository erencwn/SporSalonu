package application.models;

import java.sql.Date;

public class OdemeModel {
    private double tutar;
    private Date tarih;
    private String yontem;
    private String aciklama;
    private int durum;
    private String dekontDosya;

    public OdemeModel(double tutar, Date tarih, String yontem, String aciklama, int durum, String dekontDosya) {
        this.tutar = tutar;
        this.tarih = tarih;
        this.yontem = yontem;
        this.aciklama = aciklama;
        this.durum = durum;
        this.dekontDosya = dekontDosya;
    }

    public double getTutar() {
        return tutar;
    }

    public void setTutar(double tutar) {
        this.tutar = tutar;
    }

    public Date getTarih() {
        return tarih;
    }

    public void setTarih(Date tarih) {
        this.tarih = tarih;
    }

    public String getYontem() {
        return yontem;
    }

    public void setYontem(String yontem) {
        this.yontem = yontem;
    }

    public String getAciklama() {
        return aciklama;
    }

    public void setAciklama(String aciklama) {
        this.aciklama = aciklama;
    }

    public int getDurum() {
        return durum;
    }

    public void setDurum(int durum) {
        this.durum = durum;
    }

    public String getDekontDosya() {
        return dekontDosya;
    }

    public void setDekontDosya(String dekontDosya) {
        this.dekontDosya = dekontDosya;
    }
}
