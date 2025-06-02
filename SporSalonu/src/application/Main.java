package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.sql.Connection;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        Connection conn = DatabaseConnection.connect();

        if (conn != null) {
            System.out.println("✅ Veritabanı bağlantısı başarılı!");
        } else {
            System.out.println("❌ Veritabanına bağlanılamadı!");
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AcilisEkrani.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 600, 500);
        scene.getStylesheets().add(getClass().getResource("/css/application.css").toExternalForm());

        primaryStage.setTitle("Spor Salonu Otomasyonu");
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/uygulama-icon.png")));
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
