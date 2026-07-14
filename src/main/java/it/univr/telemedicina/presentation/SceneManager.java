package it.univr.telemedicina.presentation;

import it.univr.telemedicina.App;
import it.univr.telemedicina.domain.Doctor;
import it.univr.telemedicina.domain.Patient;
import it.univr.telemedicina.persistence.DatabaseManager;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Utility class that manages scene navigation throughout the application.
 * Holds the current Stage, the authenticated user session, and the shared DatabaseManager reference.
 */
public class SceneManager {

    private static Stage primaryStage;
    private static DatabaseManager dbManager;
    private static Object currentUser; // Doctor or Patient
    private static final String CSS_PATH = "/it/univr/telemedicina/presentation/styles.css";

    private SceneManager() {}

    public static void init(Stage stage, DatabaseManager dbManager) {
        SceneManager.primaryStage = stage;
        SceneManager.dbManager = dbManager;
    }

    public static DatabaseManager getDbManager() {
        return dbManager;
    }

    public static void setCurrentUser(Object user) {
        currentUser = user;
    }

    public static Object getCurrentUser() {
        return currentUser;
    }

    public static Patient getCurrentPatient() {
        return (currentUser instanceof Patient) ? (Patient) currentUser : null;
    }

    public static Doctor getCurrentDoctor() {
        return (currentUser instanceof Doctor) ? (Doctor) currentUser : null;
    }

    public static boolean isDoctor() {
        return currentUser instanceof Doctor;
    }

    public static boolean isPatient() {
        return currentUser instanceof Patient;
    }

    /**
     * Loads a new FXML view and sets it as the current scene.
     * @param fxmlPath relative path from the presentation package (e.g. "login-view.fxml")
     */
    public static void switchScene(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("presentation/" + fxmlPath));
            Parent root = loader.load();
            double width = (primaryStage.getScene() != null) ? primaryStage.getScene().getWidth() : 1280;
            double height = (primaryStage.getScene() != null) ? primaryStage.getScene().getHeight() : 720;
            Scene scene = new Scene(root, width, height);
            String cssUrl = SceneManager.class.getResource(CSS_PATH) != null 
                ? SceneManager.class.getResource(CSS_PATH).toExternalForm() 
                : null;
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl);
            }
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            System.err.println("Error loading scene: " + fxmlPath);
            e.printStackTrace();
        }
    }

    /**
     * Loads a new FXML view and returns the controller, useful when the caller
     * needs to pass data to the controller before switching.
     */
    public static <T> T switchSceneAndGetController(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("presentation/" + fxmlPath));
            Parent root = loader.load();
            double width = (primaryStage.getScene() != null) ? primaryStage.getScene().getWidth() : 1280;
            double height = (primaryStage.getScene() != null) ? primaryStage.getScene().getHeight() : 720;
            Scene scene = new Scene(root, width, height);
            String cssUrl = SceneManager.class.getResource(CSS_PATH) != null 
                ? SceneManager.class.getResource(CSS_PATH).toExternalForm() 
                : null;
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl);
            }
            primaryStage.setScene(scene);
            primaryStage.show();
            return loader.getController();
        } catch (IOException e) {
            System.err.println("Error loading scene: " + fxmlPath);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Logs out the current user and returns to the login screen.
     */
    public static void logout() {
        currentUser = null;
        switchScene("login-view.fxml");
    }

    public static class ModalResult<T> {
        public final T controller;
        public final Stage stage;
        public ModalResult(T controller, Stage stage) {
            this.controller = controller;
            this.stage = stage;
        }
    }

    public static <T> ModalResult<T> createModal(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("presentation/" + fxmlPath));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.initOwner(primaryStage);
            
            Scene scene = new Scene(root);
            String cssUrl = SceneManager.class.getResource(CSS_PATH) != null 
                ? SceneManager.class.getResource(CSS_PATH).toExternalForm() 
                : null;
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl);
            }
            stage.setScene(scene);
            return new ModalResult<>(loader.getController(), stage);
        } catch (IOException e) {
            System.err.println("Error creating modal: " + fxmlPath);
            e.printStackTrace();
            return null;
        }
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }
}
