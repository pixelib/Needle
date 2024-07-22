package dev.pixelib.meteor;

import dev.pixelib.meteor.scanner.DependencyScanner;

public class Main {

    public static void main(String[] args) {
        DependencyScanner dependencyScanner = new DependencyScanner(ClassLoader.getSystemClassLoader());
        System.out.println(dependencyScanner.findAllClassesUsingClassLoader(Main.class));
    }
}
