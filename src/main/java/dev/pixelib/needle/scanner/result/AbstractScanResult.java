package dev.pixelib.needle.scanner.result;

import dev.pixelib.needle.api.Wired;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class AbstractScanResult {

    protected abstract Object doCreate(Object... parameters);

    @SneakyThrows
    public Object create(Object... parameters) {
        return doCreate(parameters);
    }

    public abstract Collection<Class<?>> getDependencies();

    public abstract Class<?> getResultType();

    public Collection<Class<?>> getWiredDependencies() {
        List<Class<?>> dependencies = new ArrayList<>();
        for (Field declaredField : getResultType().getDeclaredFields()) {
            if (!declaredField.isAnnotationPresent(Wired.class)) continue;

            dependencies.add(declaredField.getType());
        }

        return dependencies;
    }

    public void setFields(Object instance, Object... parameters) {
        for (Field declaredField : getResultType().getDeclaredFields()) {
            if (!declaredField.isAnnotationPresent(Wired.class)) continue;
            declaredField.setAccessible(true);

            try {
                declaredField.set(instance, getMatchingParameters(declaredField.getType(), parameters));
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    private Object getMatchingParameters(Class<?> type, Object... paramaters) {
        for (Object paramater : paramaters) {
            if (paramater.getClass().equals(type)) return paramater;
        }

        throw new IllegalStateException("Cannot find matching parameters for " + type.getName());
    }
}
