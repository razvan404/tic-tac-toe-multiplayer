package application.controller;

import application.gui.Main;
import application.models.Table;
import application.service.Service;
import application.utils.observer.Observer;
import application.utils.server.ServerFlag;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;

import java.io.IOException;

public class GameViewController extends WindowController implements Observer {
    private Service service;
    @FXML
    protected Button button00;
    @FXML
    protected Button button01;
    @FXML
    protected Button button02;
    @FXML
    protected Button button10;
    @FXML
    protected Button button11;
    @FXML
    protected Button button12;
    @FXML
    protected Button button20;
    @FXML
    protected Button button21;
    @FXML
    protected Button button22;
    protected Button[][] buttons;
    @FXML
    protected Text versusText;
    @FXML
    protected Text turnText;
    @FXML
    protected Button backButton;
    public void build(Service service) throws IOException {
        this.service = service;
        buttons = new Button[3][3];
        buttons[0][0] = button00;
        buttons[0][1] = button01;
        buttons[0][2] = button02;
        buttons[1][0] = button10;
        buttons[1][1] = button11;
        buttons[1][2] = button12;
        buttons[2][0] = button20;
        buttons[2][1] = button21;
        buttons[2][2] = button22;

        versusText.setText("You (" + service.getCurrent().getSymbol() + ") VS "
                + service.getAdversary().getName() + " (" + service.getAdversary().getSymbol() + ")");

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                final int ii = i;
                final int jj = j;
                buttons[i][j].setOnAction(event -> {
                    try {
                        handleGridButton(ii, jj);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }
        backButton.setVisible(false);
        service.addObserver(this);
        service.nextMove();
    }

    public void updateGrid(Table table) {
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                buttons[i][j].setText(String.valueOf(table.getSquares()[i][j]));
                buttons[i][j].setDisable(table.getSquares()[i][j] != ' ');
            }
        }
    }

    private void handleGridButton(int i, int j) throws IOException {
        buttons[i][j].setText(String.valueOf(service.getCurrent().getSymbol()));
        buttons[i][j].setDisable(true);
        service.doMove(i, j);
    }

    private void blockGrid() {
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                buttons[i][j].setDisable(true);
            }
        }
    }

    @FXML
    protected void handleBackButton() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("main-view.fxml"));
        AnchorPane pane = fxmlLoader.load();
        fxmlLoader.<MainViewController>getController().build(service);
        setScene(pane);
    }

    @Override
    public void update(Object... resources) {
        Platform.runLater(() -> {
            if (resources[0] instanceof ServerFlag) {
                if (resources[0] == ServerFlag.YOUR_MOVE_FLAG) {
                    turnText.setText("YOUR TURN");
                    turnText.getStyleClass().setAll("your-turn-text");
                    updateGrid((Table) resources[1]);
                }
                else if (resources[0] == ServerFlag.OPPONENT_MOVE_FLAG) {
                    turnText.setText("OPPONENT'S TURN");
                    turnText.getStyleClass().setAll("opponent-turn-text");
                    blockGrid();
                }
                else if (resources[0] == ServerFlag.WIN_FLAG) {
                    turnText.setText("YOU WON!");
                    turnText.getStyleClass().setAll("win-text");
                }
                else if (resources[0] == ServerFlag.LOSE_FLAG) {
                    turnText.setText("YOU LOST!");
                    turnText.getStyleClass().setAll("lose-text");
                }
                else if (resources[0] == ServerFlag.DRAW_FLAG) {
                    turnText.setText("DRAW!");
                    turnText.getStyleClass().setAll("draw-text");
                }
                if (resources[0] == ServerFlag.WIN_FLAG || resources[0] == ServerFlag.LOSE_FLAG
                        || resources[0] == ServerFlag.DRAW_FLAG) {
                    updateGrid((Table) resources[1]);
                    blockGrid();
                    try {
                        service.disconnect();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    backButton.setVisible(true);
                    stage.requestFocus();
                }
            }
        });
    }
}
