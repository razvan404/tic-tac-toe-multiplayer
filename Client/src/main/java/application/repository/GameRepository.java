package application.repository;

import application.models.Player;
import application.models.Table;
import application.utils.observer.Observable;
import application.utils.server.ServerConnection;
import application.utils.server.ServerFlag;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class GameRepository extends Observable {
    private final DataInputStream socketIn;
    private final DataOutputStream socketOut;
    public GameRepository(ServerConnection serverConnection) throws IOException {
        socketIn = serverConnection.getInputStream();
        socketOut = serverConnection.getOutputStream();
    }

    public void sendPlayerInfo(Player player) throws IOException {
        socketOut.writeShort(player.getName().length());
        socketOut.write(player.getName().getBytes());
        socketOut.flush();
    }

    public ServerFlag getServerFlag() throws IOException {
        return ServerFlag.values()[socketIn.readShort()];
    }

    public Player getOtherPlayerInfo() throws IOException {
        if (getServerFlag() == ServerFlag.WAIT_FLAG) {
            return null;
        }
        short nickSize = socketIn.readShort();
        byte[] nickBytes = socketIn.readNBytes(nickSize);
        return new Player(nickBytes);
    }

    public byte getPlayerType() throws IOException {
        return socketIn.readByte();
    }

    public Table getTable() throws IOException {
        return new Table(socketIn.readNBytes(9));
    }

    public void sendMove(short move) throws IOException {
        socketOut.writeShort(move);
        socketOut.flush();
    }
}
