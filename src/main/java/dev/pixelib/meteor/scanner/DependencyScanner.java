package dev.pixelib.meteor.scanner;

import dev.pixelib.meteor.api.Component;
import dev.pixelib.meteor.scanner.result.AbstractScanResult;
import dev.pixelib.meteor.scanner.result.MethodComponentResult;
import org.reflections.Reflections;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DependencyScanner {

    private final Reflections reflections;

    private final Map<Class<?>, AbstractScanResult> results;
    private final Map<Class<?>, Integer> dependencyCount;

    public DependencyScanner(Reflections reflections) {
        this.reflections = reflections;
        this.results = new HashMap<>();
        this.dependencyCount = new HashMap<>();
    }

    public void findAllComponents() {
    }

    private void findAllAnnotatedClasses() {
        Set<Class<?>> classes = this.reflections.getTypesAnnotatedWith(Component.class);

        classes.forEach(this::findAllMethodComponents);
    }

    private void findAllMethodComponents(Class<?> clazz) {
        for (Method declaredMethod : clazz.getDeclaredMethods()) {
            if (declaredMethod.isAnnotationPresent(Component.class)) {
                MethodComponentResult methodComponentResult = new MethodComponentResult(clazz, declaredMethod);
                
            }
        }
    }
}
