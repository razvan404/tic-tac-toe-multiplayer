package application.controller;

import application.gui.Main;
import application.models.ValidationException;
import application.service.Service;
import application.utils.observer.Observer;
import application.utils.server.ServerFlag;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;

import java.io.IOException;

public class MainViewController extends WindowController implements Observer {
    private Service service;
    @FXML
    protected Text errorText;
    @FXML
    protected TextField nicknameField;
    public void build(Service service) {
        errorText.setText(null);
        this.service = service;
        service.addObserver(this);
    }
    @FXML
    protected void handleCreatorHyperlink() {
        application.getHostServices().showDocument("https://github.com/razvan404");
    }

    @FXML
    protected void handleConnectButton() throws IOException {
        try {
            service.connect(nicknameField.getText());
            errorText.setText(null);
        } catch (ValidationException e) {
            errorText.setText(e.getMessage());
        }
    }

    @Override
    public void update(Object... resources) {
        Platform.runLater(() -> {
            try {
                if (resources.length > 0 && resources[0] instanceof ServerFlag) {
                    if (resources[0] == ServerFlag.START_FLAG) {
                        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("game-view.fxml"));
                        AnchorPane pane = fxmlLoader.load();
                        fxmlLoader.<GameViewController>getController().build(service);
                        setScene(pane);
                        service.removeObserver(this);
                    }
                    else if (resources[0] == ServerFlag.WAIT_FLAG) {
                        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("game-wait.fxml"));
                        setScene(fxmlLoader.load());
                    }
                }
            } catch (IOException ioException) {
                throw new RuntimeException(ioException);
            }
        });
    }
}
