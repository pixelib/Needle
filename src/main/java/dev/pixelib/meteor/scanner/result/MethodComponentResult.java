package dev.pixelib.meteor.scanner.result;

import lombok.SneakyThrows;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class MethodComponentResult extends AbstractScanResult {

    private final Object parentInstance;
    private final Method creationMethod;

    public MethodComponentResult(Object parentInstance, Method creationMethod) {
        this.parentInstance = parentInstance;
        this.creationMethod = creationMethod;
    }


    @Override
    @SneakyThrows
    protected Object doCreate(Object... parameters) {
        creationMethod.setAccessible(true);
        return creationMethod.invoke(parentInstance, parameters);
    }

    @Override
    public Collection<Class<?>> getDependencies() {
        List<Class<?>> dependencies = Arrays.asList(creationMethod.getParameterTypes());
        dependencies.add(parentInstance.getClass());
        return dependencies;
    }
}
