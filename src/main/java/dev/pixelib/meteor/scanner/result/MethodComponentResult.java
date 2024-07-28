package dev.pixelib.meteor.scanner.result;

import lombok.SneakyThrows;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

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
        // TODO: Created object should also init @Components
        creationMethod.setAccessible(true);
        return creationMethod.invoke(parentClass, parameters);
    }

    @Override
    public Collection<Class<?>> getDependencies() {
        List<Class<?>> dependencies = Arrays.asList(creationMethod.getParameterTypes());
        dependencies.add(parentClass);
        return dependencies;
    }

    @Override
    public Class<?> getResultClass() {
        return creationMethod.getReturnType();
    }
}
