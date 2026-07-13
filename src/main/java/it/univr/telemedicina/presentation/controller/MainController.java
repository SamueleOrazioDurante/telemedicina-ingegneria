package it.univr.telemedicina.presentation.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class MainController {

    @FXML
    private Button startButton;

    @FXML
    protected void onStartButtonClick() {
        System.out.println("Telemedicine App started: Initialized layers are ready.");
    }
}
