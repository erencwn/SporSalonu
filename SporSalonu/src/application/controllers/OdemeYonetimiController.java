package application.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.awt.Desktop;
import java.io.File;
import java.sql.*;
import java.time.LocalDate;
import java.util.stream.Collectors;

public class OdemeYonetimiController {

    @FXML private TableView<Odeme> tableOdemeler;
    @FXML private TableColumn<Odeme, String> colUyeAd;
    @FXML private TableColumn<Odeme, LocalDate> colTarih;
    @FXML private TableColumn<Odeme, Double> colTutar;
    @FXML private TableColumn<Odeme, String> colYontem;
    @FXML private TableColumn<Odeme, Integer> colDurum;
    @FXML private TableColumn<Odeme, Void> colDekont;
    @FXML private TableColumn<Odeme, Void> colIslem;
    @FXML private DatePicker filterDatePicker;
    @FXML private DatePicker filterDatePicker2;
    @FXML private PieChart chartAylik;
    @FXML private PieChart chartYillik;
    @FXML private PieChart chartToplam;
    @FXML private VBox chartContainer;
    @FXML private Label lblBekleyen;
    @FXML private Label lblOnaylanan;
    @FXML private Label lblToplam;
    @FXML private Button btnGeriDon;

    private final ObservableList<Odeme> odemeList = FXCollections.observableArrayList();
    private final String dbUrl = "jdbc:sqlserver://localhost:1433;databaseName=spor_salonu;encrypt=true;trustServerCertificate=true";
    private final String dbUser = "sa";
    private final String dbPass = "Can,34087";

    @FXML
    public void initialize() {
        colUyeAd.setCellValueFactory(new PropertyValueFactory<>("uyeAd"));
        colTarih.setCellValueFactory(new PropertyValueFactory<>("tarih"));
        colTutar.setCellValueFactory(new PropertyValueFactory<>("tutar"));
        colYontem.setCellValueFactory(new PropertyValueFactory<>("yontem"));
        colDurum.setCellValueFactory(new PropertyValueFactory<>("durum"));

        colDurum.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Integer durum, boolean empty) {
                super.updateItem(durum, empty);
                if (empty || durum == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(switch (durum) {
                        case 1 -> "Onaylandı";
                        case 0 -> "Bekleniyor";
                        case -1 -> "Reddedildi";
                        default -> "Bilinmiyor";
                    });
                    setStyle("-fx-font-weight: bold");
                    getStyleClass().removeAll("cell-onaylandi", "cell-bekleniyor", "cell-reddedildi");
                    if (durum == 1) getStyleClass().add("cell-onaylandi");
                    else if (durum == 0) getStyleClass().add("cell-bekleniyor");
                    else if (durum == -1) getStyleClass().add("cell-reddedildi");
                }
            }
        });

        colDekont.setCellFactory(tc -> new TableCell<>() {
            private final Button btnGor = new Button("Görüntüle");
            {
                btnGor.getStyleClass().add("button-small-blue");
                btnGor.setOnAction(e -> {
                    Odeme o = getTableView().getItems().get(getIndex());
                    if (o.getDekontYolu() != null) {
                        try {
                            Desktop.getDesktop().open(new File(o.getDekontYolu()));
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableView().getItems().get(getIndex()).getDekontYolu() == null) {
                    setGraphic(null);
                } else {
                    setGraphic(btnGor);
                }
            }
        });

        colIslem.setCellFactory(param -> new TableCell<>() {
            private final Button btnOnayla = new Button("Onayla");
            private final Button btnReddet = new Button("Reddet");
            private final HBox hBox = new HBox(5, btnOnayla, btnReddet);

            {
                btnOnayla.setOnAction(event -> {
                    Odeme o = getTableView().getItems().get(getIndex());
                    if (o.getDurum() != 1) {
                        guncelleDurum(o.getId(), 1);
                        o.setDurum(1);
                        tableOdemeler.refresh();
                        updateCharts();
                    }
                });
                btnReddet.setOnAction(event -> {
                    Odeme o = getTableView().getItems().get(getIndex());
                    if (o.getDurum() != -1) {
                        guncelleDurum(o.getId(), -1);
                        o.setDurum(-1);
                        tableOdemeler.refresh();
                        updateCharts();
                    }
                });
                btnOnayla.getStyleClass().add("button-small-green");
                btnReddet.getStyleClass().add("button-small-red");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else {
                    Odeme o = getTableView().getItems().get(getIndex());
                    hBox.setVisible(o.getDurum() == 0);
                    setGraphic(hBox);
                }
            }
        });

        tableOdemeler.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Odeme o, boolean empty) {
                super.updateItem(o, empty);
                getStyleClass().removeAll("row-onaylandi", "row-bekleniyor", "row-reddedildi");
                if (o != null && !empty) {
                    switch (o.getDurum()) {
                        case 1 -> getStyleClass().add("row-onaylandi");
                        case 0 -> getStyleClass().add("row-bekleniyor");
                        case -1 -> getStyleClass().add("row-reddedildi");
                    }
                }
            }
        });

        odemeList.addAll(veriGetir());
        tableOdemeler.setItems(odemeList);
        updateCharts();
    }

    private ObservableList<Odeme> veriGetir() {
        ObservableList<Odeme> list = FXCollections.observableArrayList();
        try (Connection c = DriverManager.getConnection(dbUrl, dbUser, dbPass);
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery("SELECT o.OdemeID, u.ad + ' ' + u.soyad AS UyeAd, o.Tutar, o.OdemeTarihi, o.OdemeYontemi, o.DekontDosya, o.OdemeDurum FROM Odemeler o JOIN Uyeler u ON o.UyeID = u.id")) {

            while (rs.next()) {
                list.add(new Odeme(
                        rs.getInt("OdemeID"),
                        rs.getString("UyeAd"),
                        rs.getDate("OdemeTarihi").toLocalDate(),
                        rs.getDouble("Tutar"),
                        rs.getString("OdemeYontemi"),
                        rs.getInt("OdemeDurum"),
                        rs.getString("DekontDosya")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private void guncelleDurum(int id, int yeniDurum) {
        try (Connection c = DriverManager.getConnection(dbUrl, dbUser, dbPass);
             PreparedStatement ps = c.prepareStatement("UPDATE Odemeler SET OdemeDurum=? WHERE OdemeID=?")) {
            ps.setInt(1, yeniDurum);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleFiltrele() {
        LocalDate baslangic = filterDatePicker.getValue();
        LocalDate bitis = filterDatePicker2.getValue();

        if (baslangic != null && bitis != null) {
            ObservableList<Odeme> filtreli = odemeList.stream()
                    .filter(o -> !o.getTarih().isBefore(baslangic) && !o.getTarih().isAfter(bitis))
                    .collect(Collectors.toCollection(FXCollections::observableArrayList));
            tableOdemeler.setItems(filtreli);
            tableOdemeler.refresh();
        }
    }

    @FXML
    public void handleSifirla() {
        tableOdemeler.setItems(odemeList);
        filterDatePicker.setValue(null);
        filterDatePicker2.setValue(null);
        tableOdemeler.refresh(); 
    }


    private void updateCharts() {
        LocalDate bugun = LocalDate.now();

        ObservableList<Odeme> aylikOdemeler = odemeList.filtered(o -> o.getTarih().isAfter(bugun.minusDays(30)));
        ObservableList<Odeme> yillikOdemeler = odemeList.filtered(o -> o.getTarih().isAfter(bugun.minusDays(365)));

        generatePieChart(chartAylik, aylikOdemeler, "Aylık Ödemeler Grafiği");
        generatePieChart(chartYillik, yillikOdemeler, "Yıllık Ödemeler Grafiği");
        generatePieChart(chartToplam, odemeList, "Toplam Ödemeler Grafiği");
    }

    private void generatePieChart(PieChart chart, ObservableList<Odeme> list, String title) {
        long onaylanan = list.stream().filter(o -> o.getDurum() == 1).count();
        long bekleyen = list.stream().filter(o -> o.getDurum() == 0).count();
        long reddedilen = list.stream().filter(o -> o.getDurum() == -1).count();

        double tOnay = list.stream().filter(o -> o.getDurum() == 1).mapToDouble(Odeme::getTutar).sum();
        double tBekleyen = list.stream().filter(o -> o.getDurum() == 0).mapToDouble(Odeme::getTutar).sum();
        double tReddedilen = list.stream().filter(o -> o.getDurum() == -1).mapToDouble(Odeme::getTutar).sum();

        ObservableList<PieChart.Data> data = FXCollections.observableArrayList(
                new PieChart.Data("Onaylanan", onaylanan),
                new PieChart.Data("Bekleyen", bekleyen),
                new PieChart.Data("Reddedilen", reddedilen)
        );

        chart.setTitle(title);
        chart.setData(data);

        for (PieChart.Data d : data) {
            double toplamTutar = switch (d.getName()) {
                case "Onaylanan" -> tOnay;
                case "Bekleyen" -> tBekleyen;
                case "Reddedilen" -> tReddedilen;
                default -> 0;
            };
            Tooltip tooltip = new Tooltip(d.getName() + ": " + toplamTutar + " ₺");
            Tooltip.install(d.getNode(), tooltip);

            if (d.getName().equals("Onaylanan")) d.getNode().setStyle("-fx-pie-color: #2ecc71;");
            if (d.getName().equals("Bekleyen")) d.getNode().setStyle("-fx-pie-color: #f1c40f;");
            if (d.getName().equals("Reddedilen")) d.getNode().setStyle("-fx-pie-color: #e74c3c;");
        }
    }

    @FXML
    public void handleGeriDon() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/AdminEkrani.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) btnGeriDon.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class Odeme {
        private final int id;
        private final String uyeAd;
        private final LocalDate tarih;
        private final double tutar;
        private final String yontem;
        private int durum;
        private final String dekontYolu;

        public Odeme(int id, String uyeAd, LocalDate tarih, double tutar, String yontem, int durum, String dekontYolu) {
            this.id = id;
            this.uyeAd = uyeAd;
            this.tarih = tarih;
            this.tutar = tutar;
            this.yontem = yontem;
            this.durum = durum;
            this.dekontYolu = dekontYolu;
        }

        public int getId() { return id; }
        public String getUyeAd() { return uyeAd; }
        public LocalDate getTarih() { return tarih; }
        public double getTutar() { return tutar; }
        public String getYontem() { return yontem; }
        public int getDurum() { return durum; }
        public void setDurum(int durum) { this.durum = durum; }
        public String getDekontYolu() { return dekontYolu; }
    }
}
