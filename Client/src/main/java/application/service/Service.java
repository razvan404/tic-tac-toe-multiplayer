package application.service;

import application.models.ModelsFactory;
import application.models.Player;
import application.models.Table;
import application.models.ValidationException;
import application.repository.GameRepository;
import application.utils.Config;
import application.utils.observer.Observable;
import application.utils.server.ServerConnection;
import application.utils.server.ServerFlag;

import java.io.IOException;

public class Service extends Observable {
    private Player current;
    private Player adversary;
    private GameRepository repository;
    private ServerConnection connection;

    public Service() {}

    public Player getCurrent() {
        return current;
    }

    public Player getAdversary() {
        return adversary;
    }

    public void connect(String nickname) throws IOException, ValidationException {
        current = ModelsFactory.createPlayer(nickname);
        connection = new ServerConnection(
                Config.getProperty("SERVER_IP"),
                Short.parseShort(Config.getProperty("SERVER_PORT")));
        repository = new GameRepository(connection);
        repository.sendPlayerInfo(current);

        new Thread(() -> {
            try {
                adversary = repository.getOtherPlayerInfo();
                if (adversary == null) {
                    notifyObservers(ServerFlag.WAIT_FLAG);
                    adversary = repository.getOtherPlayerInfo();
                }

                byte symbol = repository.getPlayerType();
                current.setSymbol(symbol);
                if (symbol == 'X') {
                    adversary.setSymbol('0');
                } else {
                    adversary.setSymbol('X');
                }
                notifyObservers(ServerFlag.START_FLAG);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    public void disconnect() throws IOException {
        current = null;
        adversary = null;
        repository = null;
        connection.closeConnection();
        connection = null;
        removeObservers();
    }

    public void nextMove() {
        new Thread(() -> {
            try {
                ServerFlag flag = repository.getServerFlag();
                if (flag == ServerFlag.OPPONENT_MOVE_FLAG) {
                    notifyObservers(ServerFlag.OPPONENT_MOVE_FLAG);
                    flag = repository.getServerFlag();
                }
                if (flag == ServerFlag.YOUR_MOVE_FLAG) {
                    Table table = repository.getTable();
                    notifyObservers(ServerFlag.YOUR_MOVE_FLAG, table);
                }
                else if (flag == ServerFlag.DRAW_FLAG || flag == ServerFlag.LOSE_FLAG || flag == ServerFlag.WIN_FLAG) {
                    Table table = repository.getTable();
                    notifyObservers(flag, table);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    public void doMove(int i, int j) throws IOException {
        repository.sendMove((short) (3 * i + j));
        nextMove();
    }
}
