package it.univr.telemedicina;

/**
 * Launcher class required by the Maven Shade plugin.
 * JavaFX applications packaged as fat JARs need a non-Application main class.
 */
public class Main {
    public static void main(String[] args) {
        App.main(args);
    }
}
