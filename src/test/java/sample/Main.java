package sample;

import dev.pixelib.meteor.scanner.DependencyScanner;

public class Main {

    public static void main(String[] args) {
        new DependencyScanner(Main.class);
    }
}
