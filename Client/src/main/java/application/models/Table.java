package application.models;

public class Table {
    private final char[][] squares;

    public Table() {
        squares = new char[3][3];
    }

    public Table(byte[] squares) {
        this();
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                this.squares[i][j] = (char) squares[3 * i + j];
            }
        }
    }

    public char[][] getSquares() {
        return squares;
    }
}
