package pl.net.brach;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import pl.net.brach.commons.ui.Branding;

public class ExchangeDifferences extends Application {

    static final String BRACHSOFT_TITLE = "r4_tech - Różnice kursowe v.1.1";
    static final String STYLE_PATH = "pl/net/brach/style.css";

    static final String PRIMARY_PRINTER_NAME = "Xprinter XP-350B";
    static final String SECONDARY_PRINTER_NAME = "Xprinter XP-420B";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(this.getClass().getResource("MainWindow.fxml"));
        Parent root = fxmlLoader.load();

        Scene scene = new Scene(root);
        scene.getStylesheets().add(STYLE_PATH);

        stage.setTitle(BRACHSOFT_TITLE);
        Branding.applyIcon(stage);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    protected static void displaySummary(String[] args) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ExchangeDifferences.class.getResource("Summary.fxml"));
        Pane root = fxmlLoader.load();

        Scene scene = new Scene(root);
        scene.getStylesheets().add(STYLE_PATH);

        Stage stage = new Stage();

        stage.setTitle(BRACHSOFT_TITLE + " - Podsumowanie");
        Branding.applyIcon(stage);
        stage.setScene(scene);
        stage.setResizable(false);

        SummaryController summaryController = fxmlLoader.getController();
        summaryController.generateSummary(args);

        stage.show();
    }
}
