package dev.pixelib.meteor.scanner;

import dev.pixelib.meteor.api.Component;
import java.util.Set;
import org.reflections.Reflections;

public class DependencyScanner {

    private final Reflections reflections;

    public DependencyScanner(Reflections reflections) {
        this.reflections = reflections;
    }

    public Set<Class<?>> scan() {
        return this.reflections.getTypesAnnotatedWith(Component.class);
    }
}
