package dev.pixelib.needle.scanner;

import dev.pixelib.needle.api.Component;
import dev.pixelib.needle.scanner.result.AbstractScanResult;
import dev.pixelib.needle.scanner.result.ClassComponentResult;
import dev.pixelib.needle.scanner.result.MethodComponentResult;
import org.reflections.Reflections;

import java.lang.reflect.Method;
import java.util.*;

public class DependencyScanner {

    private final Reflections reflections;

    private final Map<Class<?>, List<AbstractScanResult>> results;
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

        return results.values().stream()
                .flatMap(List::stream)
                .sorted(Comparator.comparingInt(r -> dependencyCount.getOrDefault(r.getResultType(), 0)))
                .toList();
    }

    private void saveClassComponent(Class<?> aClass) {
        ClassComponentResult result = new ClassComponentResult(aClass);
        checkForDuplicate(result.getResultType(), result);
        results.computeIfAbsent(aClass, k -> new ArrayList<>()).add(result);
    }

    private void checkForDuplicate(Class<?> type, AbstractScanResult newResult) {
        List<AbstractScanResult> existingList = results.get(type);
        if (existingList == null) return;

        String newName = newResult.getName();

        for (AbstractScanResult existing : existingList) {
            String existingName = existing.getName();

            if (newName.isEmpty() || existingName.isEmpty()) {
                throw new IllegalStateException(
                        "Duplicate component of type '" + type.getSimpleName() + "' found. " +
                                "Both " + describeSource(existing) + " and " + describeSource(newResult) +
                                " produce the same type. Use @Component(\"name\") with unique names to disambiguate."
                );
            }

            if (existingName.equals(newName)) {
                throw new IllegalStateException(
                        "Duplicate component name '" + newName + "' for type '" + type.getSimpleName() + "'. " +
                                "Both " + describeSource(existing) + " and " + describeSource(newResult) +
                                " use the same @Component name."
                );
            }
        }
    }

    private String describeSource(AbstractScanResult result) {
        if (result instanceof ClassComponentResult classResult) {
            return classResult.creationClass.getSimpleName();
        } else if (result instanceof MethodComponentResult methodResult) {
            return methodResult.getDeclaringClass().getSimpleName() + "#" + methodResult.getMethodName();
        }
        return result.getResultType().getSimpleName();
    }


    private void countDependencies() {
        for (List<AbstractScanResult> resultList : results.values()) {
            AbstractScanResult first = resultList.getFirst();
            if (!dependencyCount.containsKey(first.getResultType())) {
                dependencyCount.put(first.getResultType(), getDependencyCount(first));
            }
        }
    }

    private int getDependencyCount(AbstractScanResult result) {
        if (result == null) {
            throw new IllegalStateException("Cannot get dependency count for null result");
        }
        Integer cached = dependencyCount.get(result.getResultType());
        if (cached != null) {
            return cached;
        }

        int count = result.getDependencies().size();
        for (Class<?> dependency : result.getDependencies()) {
            List<AbstractScanResult> depResults = results.get(dependency);
            AbstractScanResult depResult = depResults != null ? depResults.getFirst() : null;
            count += getDependencyCount(depResult);
        }

        dependencyCount.put(result.getResultType(), count);
        return count;
    }

    private void findAllMethodComponents(Class<?> clazz) {
        for (Method declaredMethod : clazz.getDeclaredMethods()) {
            if (declaredMethod.isAnnotationPresent(Component.class)) {
                MethodComponentResult methodComponentResult = new MethodComponentResult(clazz, declaredMethod);

                checkForDuplicate(methodComponentResult.getResultType(), methodComponentResult);
                results.computeIfAbsent(methodComponentResult.getResultType(), k -> new ArrayList<>()).add(methodComponentResult);

                findAllMethodComponents(methodComponentResult.getResultType());
            }
        }
    }

    public void ensureNoCircularDependency() {
        Set<Class<?>> visited = new HashSet<>();
        Set<Class<?>> stack = new HashSet<>();
        for (List<AbstractScanResult> resultList : results.values()) {
            for (AbstractScanResult result : resultList) {
                if (checkDependency(result, visited, stack)) {
                    throw new IllegalStateException("Circular dependency found: " + String.join(" -> ", stack.stream().map(Class::getSimpleName).toList()));
                }
            }
        }
    }

    public void ensureNoMissingDependencies() {
        for (List<AbstractScanResult> resultList : results.values()) {
            for (AbstractScanResult result : resultList) {
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
    }

    private boolean checkDependency(AbstractScanResult result, Set<Class<?>> visited, Set<Class<?>> stack) {
        if (result == null) return false;
        if (stack.contains(result.getResultType())) {
            return true;
        }
        if (visited.contains(result.getResultType())) {
            return false;
        }

        visited.add(result.getResultType());
        stack.add(result.getResultType());

        for (Class<?> dependency : result.getDependencies()) {
            List<AbstractScanResult> depResults = results.get(dependency);
            AbstractScanResult depResult = depResults != null ? depResults.getFirst() : null;
            if (checkDependency(depResult, visited, stack)) {
                return true;
            }
        }

        stack.remove(result.getResultType());
        return false;
    }
}
