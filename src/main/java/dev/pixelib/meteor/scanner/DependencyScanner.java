package dev.pixelib.meteor.scanner;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Set;
import java.util.stream.Collectors;

public class DependencyScanner {

    private final ClassLoader classLoader;

    public DependencyScanner(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public DependencyScanner() {
        this.classLoader = ClassLoader.getSystemClassLoader();
    }

    public Set<String> findAllClassesUsingClassLoader(Class<?> startClass) {
        return findAllClassesUsingClassLoader(startClass.getPackageName());
    }

    public Set<String> findAllClassesUsingClassLoader(String packageName) {
        InputStream stream = classLoader.getResourceAsStream(packageName.replaceAll("[.]", "/"));
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        return reader.lines()
                .filter(line -> line.endsWith(".class"))
                .collect(Collectors.toSet());
    }
}
