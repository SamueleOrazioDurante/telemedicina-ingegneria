package it.univr.telemedicina.logic;

public interface AlertSubject {
    void registerObserver(AlertObserver observer);
    void removeObserver(AlertObserver observer);
    void notifyObservers(String alertMessage);
}
