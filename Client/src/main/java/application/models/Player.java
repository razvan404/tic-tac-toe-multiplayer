package application.models;

public class Player {
    private final String name;
    private char symbol;

    public Player(String name) {
        this.name = name;
    }

    public Player(byte[] nameBytes) {
        this(new String(nameBytes));
    }

    public String getName() {
        return name;
    }

    public void setSymbol(byte symbol) {
        this.symbol = (char) symbol;
    }

    public void setSymbol(char symbol) {
        this.symbol = symbol;
    }

    public char getSymbol() {
        return symbol;
    }
}
