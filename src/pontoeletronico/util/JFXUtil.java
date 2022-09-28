package pontoeletronico.util;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import pontoeletronico.util.AudioUtils;

public class JFXUtil {

    public static void maximizar(Stage stage) {
        // Get current screen of the stage
        ObservableList<Screen> screens = Screen.getScreensForRectangle(new Rectangle2D(stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight()));

        // Change stage properties
        Rectangle2D bounds = screens.get(0).getVisualBounds();
        stage.setX(bounds.getMinX());
        stage.setY(bounds.getMinY());
        stage.setWidth(bounds.getWidth());
        stage.setHeight(bounds.getHeight());
    }

    // displays a temporary message in a label when a button is pressed,
    // and gradually fades the label away after the message has been displayed.
    public void displayFlashMessageOnAction(final Button button, final Label label, final String message) {
        final FadeTransition ft = new FadeTransition(Duration.seconds(3), label);
        ft.setInterpolator(Interpolator.EASE_BOTH);
        ft.setFromValue(1);
        ft.setToValue(0);
        button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                label.setText(message);
                label.setStyle("-fx-text-fill: forestgreen;");
                ft.playFromStart();
            }
        });
    }

    public static void showInfoMessage(String sumary, String mesage) {

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                AudioUtils.tocarAudio(AudioUtils.ARQUIVO_OK);

                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Informação");
                alert.setHeaderText(sumary);
                alert.setContentText(mesage);
                alert.showAndWait();
            }
        });

    }
    
    public static void showInfoMessageNow(String sumary, String mesage) {

        AudioUtils.tocarAudio(AudioUtils.ARQUIVO_OK);

        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Informação");
        alert.setHeaderText(sumary);
        alert.setContentText(mesage);
        alert.showAndWait();

    }    

    public static void showErrorMessage(String sumary, String mesage) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                AudioUtils.tocarAudio(AudioUtils.ARQUIVO_ERRO);

                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Erro");
                alert.setHeaderText(sumary);
                alert.setContentText(mesage);
                alert.showAndWait();

            }
        });
    }

    public static void showErrorMessageNow(String sumary, String mesage) {

        AudioUtils.tocarAudio(AudioUtils.ARQUIVO_ERRO);
        
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setHeaderText(sumary);
        alert.setContentText(mesage);
        alert.showAndWait();
    }

    public static void showWarningMessage(String sumary, String mesage) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                AudioUtils.tocarAudio(AudioUtils.ARQUIVO_OK);

                Alert alert = new Alert(AlertType.WARNING);
                alert.setTitle("Atenção");
                alert.setHeaderText(sumary);
                alert.setContentText(mesage);
                alert.showAndWait();
            }
        });
    }
    
    public static void showWarningMessageNow(String sumary, String msg) {

        AudioUtils.tocarAudio(AudioUtils.ARQUIVO_OK);

        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle("Atenção");
        alert.setHeaderText(sumary);
        alert.setContentText(msg);
        alert.showAndWait();
        
    }    

    public static void showConfirmationMessage(String sumary, String msg) {
        AudioUtils.tocarAudio(AudioUtils.ARQUIVO_OK);

        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirmação");
        alert.setHeaderText(sumary);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    public static void closeWindow(Event e) {
        Node source = (Node) e.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }

    public static void hideWindow(Event e) {
        Node source = (Node) e.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.hide();
    }

}
