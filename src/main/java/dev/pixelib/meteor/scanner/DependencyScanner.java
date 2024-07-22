package dev.pixelib.meteor.scanner;

public class DependencyScanner {

    public DependencyScanner(ClassLoader classLoader) {
        classLoader.getDefinedPackage().getAnnotations()
    }
}
