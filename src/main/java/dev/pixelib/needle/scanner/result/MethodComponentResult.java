package dev.pixelib.needle.scanner.result;

import dev.pixelib.needle.api.Component;
import dev.pixelib.needle.api.Named;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
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
    @SneakyThrows
    protected Object doCreate(Object[] params, Map<Class<?>, Map<String, Object>> componentStore) {
        creationMethod.setAccessible(true);
        Object parent = params[0];
        Object[] methodParams = params.length > 1 ? Arrays.copyOfRange(params, 1, creationMethod.getParameterCount() + 1) : new Object[]{};

        Object created = creationMethod.invoke(parent, methodParams);
        setFields(created, componentStore);

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
    public List<String> getDependencyNames() {
        List<String> names = new ArrayList<>();
        names.add(""); // parent class - no @Named

        for (Parameter param : creationMethod.getParameters()) {
            Named named = param.getAnnotation(Named.class);
            names.add(named != null ? named.value() : "");
        }

        Field[] fields = getResultType().getDeclaredFields();
        for (Field wiredField : fields) {
            if (!wiredField.isAnnotationPresent(dev.pixelib.needle.api.Wired.class)) continue;
            Named named = wiredField.getAnnotation(Named.class);
            names.add(named != null ? named.value() : "");
        }

        return names;
    }

    @Override
    public Class<?> getResultType() {
        return creationMethod.getReturnType();
    }

    @Override
    public String getName() {
        Component component = creationMethod.getAnnotation(Component.class);
        if (component != null && !component.value().isEmpty()) {
            return component.value();
        }
        return "";
    }

    public Class<?> getDeclaringClass() {
        return parentClass;
    }

    public String getMethodName() {
        return creationMethod.getName();
    }
}
