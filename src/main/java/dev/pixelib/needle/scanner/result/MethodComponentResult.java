package dev.pixelib.needle.scanner.result;

import lombok.SneakyThrows;

import java.lang.reflect.Method;
import java.util.*;

public class MethodComponentResult extends AbstractScanResult {

    private final Class<?> parentClass;
    private final Method creationMethod;

    public MethodComponentResult(Class<?> parentClass, Method creationMethod) {
        this.parentClass = parentClass;
        this.creationMethod = creationMethod;
    }

    @Override
    @SneakyThrows
    protected Object doCreate(Object... parameters) {
        creationMethod.setAccessible(true);
        Object created = creationMethod.invoke(parameters[0], parameters.length > 1 ? Arrays.copyOfRange(parameters, 1, creationMethod.getParameterCount() + 1) : new Object[]{});

        setFields(created, parameters);

        return created;
    }

    @Override
    public Collection<Class<?>> getDependencies() {
        List<Class<?>> dependencies = new ArrayList<>();
        dependencies.add(parentClass);
        dependencies.addAll(Arrays.asList(creationMethod.getParameterTypes()));
        dependencies.addAll(getWiredDependencies());
        return Collections.unmodifiableList(dependencies);
    }

    @Override
    public Class<?> getResultType() {
        return creationMethod.getReturnType();
    }
}
