package com.example.currencyconverterproject;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class MainApplication extends Application {



    @Override
    public void start(Stage primaryStage) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("currencyConverterView.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 500, 450);
        primaryStage.setTitle("Currency Converter - HAXN24");
        primaryStage.setScene(scene);
        primaryStage.show();

        // using environment variable to hide path
        String myIconPath = System.getenv("myJavaPath") + "\\resources\\CoinImage.png";

        // Load icon and set icon
        primaryStage.getIcons().add(new Image(new File(myIconPath).getAbsolutePath()));

        //ends program when closed
        primaryStage.setOnCloseRequest(event -> {
            Platform.exit();
            System.exit(0);
        });


    }

    public static void main(String[] args) {
        launch();
    }
}