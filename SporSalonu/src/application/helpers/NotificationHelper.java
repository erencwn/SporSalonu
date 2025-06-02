package application.helpers;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class NotificationHelper {

    public static void showSuccess(String message) {
        showNotification("Başarılı", message, Alert.AlertType.INFORMATION);
    }

    public static void showError(String message) {
        showNotification("Hata", message, Alert.AlertType.ERROR);
    }

    public static void showWarning(String message) {
        showNotification("Uyarı", message, Alert.AlertType.WARNING);
    }

    public static void showInfo(String message) {
        showNotification("Bilgi", message, Alert.AlertType.INFORMATION);
    }

    private static void showNotification(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(NotificationHelper.class.getResourceAsStream("/images/uygulama-icon.png")));

        Image icon;
        if (type == Alert.AlertType.INFORMATION) {
            icon = new Image(NotificationHelper.class.getResourceAsStream("/images/basarili-icon.png"));
        } else if (type == Alert.AlertType.ERROR) {
            icon = new Image(NotificationHelper.class.getResourceAsStream("/images/basarisiz-icon.png"));
        } else if (type == Alert.AlertType.WARNING) {
            icon = new Image(NotificationHelper.class.getResourceAsStream("/images/uyari-icon.png"));
        } else {
            icon = new Image(NotificationHelper.class.getResourceAsStream("/images/uyari-icon.png"));
        }

        ImageView imageView = new ImageView(icon);
        imageView.setFitHeight(48);
        imageView.setFitWidth(48);
        alert.setGraphic(imageView);

        alert.showAndWait();
    }
}
