package dev.pixelib.needle.scanner.result;

import dev.pixelib.needle.api.Named;
import dev.pixelib.needle.api.Wired;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public abstract class AbstractScanResult {

    protected abstract Object doCreate(Object... parameters);

    protected Object doCreate(Object[] params, Map<Class<?>, Map<String, Object>> componentStore) {
        return doCreate(params);
    }

    @SneakyThrows
    public Object create(Object... parameters) {
        return doCreate(parameters);
    }

    public Object create(Map<Class<?>, Map<String, Object>> componentStore) {
        return doCreate(resolveParams(componentStore), componentStore);
    }

    public abstract Collection<Class<?>> getDependencies();

    public List<String> getDependencyNames() {
        return getDependencies().stream().map(d -> "").toList();
    }

    public abstract Class<?> getResultType();

    public abstract String getName();

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

    public void setFields(Object instance, Map<Class<?>, Map<String, Object>> componentStore) {
        for (Field declaredField : getResultType().getDeclaredFields()) {
            if (!declaredField.isAnnotationPresent(Wired.class)) continue;
            declaredField.setAccessible(true);

            try {
                Named named = declaredField.getAnnotation(Named.class);
                String name = named != null ? named.value() : "";
                declaredField.set(instance, resolveFromStore(componentStore, declaredField.getType(), name));
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    protected Object getMatchingParameters(Class<?> type, Object... paramaters) {
        for (Object paramater : paramaters) {
            if (paramater.getClass().equals(type)) return paramater;
        }

        throw new IllegalStateException("Cannot find matching parameters for " + type.getName());
    }

    protected Object resolveFromStore(Map<Class<?>, Map<String, Object>> store, Class<?> type, String name) {
        Map<String, Object> namedBeans = store.get(type);
        if (namedBeans == null || namedBeans.isEmpty()) {
            throw new IllegalStateException("No component of type " + type.getSimpleName() + " found for " + getResultType().getSimpleName());
        }

        if (name.isEmpty()) {
            if (namedBeans.size() > 1) {
                throw new IllegalStateException(
                        "Multiple beans of type " + type.getSimpleName() + " found for " + getResultType().getSimpleName() +
                                ". Use @Named to disambiguate."
                );
            }
            return namedBeans.values().iterator().next();
        }

        Object bean = namedBeans.get(name);
        if (bean == null) {
            throw new IllegalStateException("No bean named '" + name + "' of type " + type.getSimpleName() + " found for " + getResultType().getSimpleName());
        }
        return bean;
    }

    private Object[] resolveParams(Map<Class<?>, Map<String, Object>> store) {
        List<Class<?>> depTypes = new ArrayList<>(getDependencies());
        List<String> depNames = getDependencyNames();
        Object[] params = new Object[depTypes.size()];
        for (int i = 0; i < depTypes.size(); i++) {
            params[i] = resolveFromStore(store, depTypes.get(i), depNames.get(i));
        }
        return params;
    }
}
