package application.gui;

import application.controller.MainViewController;
import application.controller.WindowController;
import application.repository.GameRepository;
import application.service.Service;
import application.utils.Config;
import application.utils.server.ServerConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("main-view.fxml"));
        AnchorPane root = fxmlLoader.load();

        fxmlLoader.<MainViewController>getController().build(new Service());

        WindowController.setApplication(this);
        WindowController.setStage(stage);
        WindowController.setScene(root);

        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}