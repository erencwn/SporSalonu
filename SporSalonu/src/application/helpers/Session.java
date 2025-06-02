package application.helpers;

public class Session {

    private static String aktifKullaniciEmail;
    private static int aktifKullaniciId;

    public static void setAktifKullaniciEmail(String email) {
        aktifKullaniciEmail = email;
    }

    public static String getAktifKullaniciEmail() {
        return aktifKullaniciEmail;
    }

    public static void setAktifKullaniciId(int id) {
        aktifKullaniciId = id;
    }

    public static int getAktifKullaniciId() {
        return aktifKullaniciId;
    }

    public static void clearSession() {
        aktifKullaniciEmail = null;
        aktifKullaniciId = 0;
    }
}
