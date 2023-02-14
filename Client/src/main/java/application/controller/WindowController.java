package application.controller;

import application.utils.Constants;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class WindowController {
    protected static Application application;
    protected static Stage stage;

    public static void setApplication(Application application) {
        WindowController.application = application;
    }
    public static void setStage(Stage stage) {
        WindowController.stage = stage;
        stage.setTitle("Tic Tac Toe");
        stage.getIcons().setAll(Constants.ICON);
    }

    public static void setScene(Pane root) {
        Scene scene = new Scene(root, 512, 512);
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);
        stage.setMinHeight(512);
        stage.setMinWidth(512);
        root.requestFocus();
    }
}
