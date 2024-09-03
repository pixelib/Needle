package dev.pixelib.meteor.scanner;

import dev.pixelib.meteor.api.Component;
import dev.pixelib.meteor.scanner.result.AbstractScanResult;
import dev.pixelib.meteor.scanner.result.ClassComponentResult;
import dev.pixelib.meteor.scanner.result.MethodComponentResult;
import org.reflections.Reflections;

import java.lang.reflect.Method;
import java.util.*;

public class DependencyScanner {

    private final Reflections reflections;

    private final Map<Class<?>, AbstractScanResult> results;
    private final Map<Class<?>, Integer> dependencyCount;

    public DependencyScanner(Reflections reflections) {
        this.reflections = reflections;
        this.results = new HashMap<>();
        this.dependencyCount = new HashMap<>();
    }

    public List<AbstractScanResult> findAllComponents() {
        Set<Class<?>> classes = this.reflections.getTypesAnnotatedWith(Component.class);

        classes.forEach(this::saveClassComponent);

        classes.forEach(this::findAllMethodComponents);

        ensureNoMissingDependencies();
        ensureNoCircularDependency();

        countDependencies();

        return dependencyCount.entrySet().stream()
                .sorted(Comparator.comparingInt(Map.Entry::getValue))
                .map(entry -> results.get(entry.getKey()))
                .toList();
    }

    private void saveClassComponent(Class<?> aClass) {
        this.results.put(aClass, new ClassComponentResult(aClass));
    }


    private void countDependencies() {
        for (AbstractScanResult value : results.values()) {
            if (!dependencyCount.containsKey(value.getResultType())) {
                dependencyCount.put(value.getResultType(), getDependencyCount(value));
            }
        }
    }

    private int getDependencyCount(AbstractScanResult result) {
        if (result == null) {
            throw new IllegalStateException("Cannot get dependency count for null result");
        }
        int count = 0;
        for (Class<?> dependency : result.getDependencies()) {
            count += dependencyCount.computeIfAbsent(dependency, t -> getDependencyCount(results.get(t)));
        }

        return count;
    }

    private void findAllMethodComponents(Class<?> clazz) {
        for (Method declaredMethod : clazz.getDeclaredMethods()) {
            if (declaredMethod.isAnnotationPresent(Component.class)) {
                MethodComponentResult methodComponentResult = new MethodComponentResult(clazz, declaredMethod);

                results.put(methodComponentResult.getResultType(), methodComponentResult);

                findAllMethodComponents(methodComponentResult.getResultType());
            }
        }
    }

    public void ensureNoCircularDependency() {
        Set<Class<?>> visited = new HashSet<>();
        Set<Class<?>> stack = new HashSet<>();
        for (AbstractScanResult result : results.values()) {
            if (checkDependency(result, visited, stack)) {
                throw new IllegalStateException("Circular dependency found: " + String.join(" -> ", stack.stream().map(Class::getSimpleName).toList()));
            }
        }
    }

    public void ensureNoMissingDependencies() {
        for (AbstractScanResult result : results.values()) {
            if (result.getDependencies().contains(result.getResultType())) {
                throw new IllegalStateException(result.getResultType().getSimpleName() + " can not be dependent on itself");
            }

            for (Class<?> dependency : result.getDependencies()) {
                if (!results.containsKey(dependency)) {
                    throw new IllegalStateException(dependency.getSimpleName() + " dependency " + dependency.getSimpleName() + " not found for" + result.getResultType().getSimpleName());
                }
            }
        }
    }

    private boolean checkDependency(AbstractScanResult result, Set<Class<?>> visited, Set<Class<?>> stack) {
        if (stack.contains(result.getResultType())) {
            return true;
        }
        if (visited.contains(result.getResultType())) {
            return false;
        }

        visited.add(result.getResultType());
        stack.add(result.getResultType());

        for (Class<?> dependency : result.getDependencies()) {
            if (checkDependency(results.get(dependency), visited, stack)) {
                return true;
            }
        }

        stack.remove(result.getResultType());
        return false;
    }
}