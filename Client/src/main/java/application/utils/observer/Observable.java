package application.utils.observer;

import java.util.*;

public abstract class Observable {
    private final List<Observer> observers;

    protected Observable() {
        observers = new ArrayList<>();
    }

    public void addObserver(Observer observer) {
        observers.add(observer);
    }

    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }

    public void removeObservers() {
        observers.clear();
    }

    protected void notifyObservers(Object... resources) {
        observers.forEach(observer -> observer.update(resources));
    }
}
