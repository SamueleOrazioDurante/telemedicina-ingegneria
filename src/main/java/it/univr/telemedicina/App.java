package it.univr.telemedicina;

import it.univr.telemedicina.persistence.DatabaseManager;
import it.univr.telemedicina.presentation.SceneManager;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Main JavaFX Application class.
 * Initializes the SceneManager and loads the login screen.
 */
public class App extends Application {

    @Override
    public void start(Stage stage) {
        DatabaseManager dbManager = new DatabaseManager();
        dbManager.initializeDatabase();

        SceneManager.init(stage, dbManager);
        stage.setTitle("Telemedicine System — Diabetic Patients Monitoring");
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        SceneManager.switchScene("login-view.fxml");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
