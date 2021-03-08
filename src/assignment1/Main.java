/**
 * Title: CSCI2020U Assignment 1
 * Date: March 6th, 2021
 * Author: Muaz Rehman 100553376
 */

// Package
package assignment1;

// Imports
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

public class Main extends Application {
    // JavaFX Initialization
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load layout for stage
        Parent root = FXMLLoader.load(getClass().getResource("MainForm.fxml"));
        primaryStage.setTitle("Spam Master 3000");
        primaryStage.setScene(new Scene(root, 650, 400));
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
