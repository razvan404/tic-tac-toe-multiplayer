package application.models;

public class ModelsFactory {
    public static Player createPlayer(String name) throws ValidationException {
        if (name == null || "".equals(name)) {
            throw new ValidationException("The name cannot be null!");
        }
        if (name.length() > 24) {
            throw new ValidationException("The length of the name couldn't be longer than 24 characters!");
        }
        if (name.length() < 3) {
            throw new ValidationException("The length of the name should be bigger than 3 characters!");
        }
        return new Player(name);
    }
}
