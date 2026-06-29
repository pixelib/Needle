package dev.pixelib.needle.scanner.result;

import dev.pixelib.needle.api.Component;
import dev.pixelib.needle.api.Named;
import dev.pixelib.needle.api.PostConstruct;
import dev.pixelib.needle.api.Wired;
import dev.pixelib.needle.utils.ReflectionUtils;
import lombok.SneakyThrows;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.*;

public class ClassComponentResult extends AbstractScanResult {

    public final Class<?> creationClass;

    public ClassComponentResult(Class<?> creationClass) {
        this.creationClass = creationClass;
    }

    @Override
    @SneakyThrows
    protected Object doCreate(Object... parameters) {
        Constructor<?> constructor = getCreationConstructor();
        Object[] constructorParams = new Object[constructor.getParameterCount()];
        for (int i = 0; i < constructor.getParameterCount(); i++) {
            constructorParams[i] = getMatchingParameters(constructor.getParameterTypes()[i], parameters);
        }
        Object object = constructor.newInstance(constructorParams);
        ReflectionUtils.callMethodWithAnnotation(PostConstruct.class, object);

        setFields(object, parameters);

        return object;
    }

    @Override
    @SneakyThrows
    protected Object doCreate(Object[] params, Map<Class<?>, Map<String, Object>> componentStore) {
        Constructor<?> constructor = getCreationConstructor();
        Object[] constructorParams = Arrays.copyOf(params, constructor.getParameterCount());

        Object object = constructor.newInstance(constructorParams);
        ReflectionUtils.callMethodWithAnnotation(PostConstruct.class, object);

        setFields(object, componentStore);

        return object;
    }

    @Override
    public Collection<Class<?>> getDependencies() {
        List<Class<?>> paramDependencies = new ArrayList<>(Arrays.asList(getCreationConstructor().getParameterTypes()));
        paramDependencies.addAll(getWiredDependencies());
        return Collections.unmodifiableList(paramDependencies);
    }

    @Override
    public List<String> getDependencyNames() {
        List<String> names = new ArrayList<>();
        for (Parameter param : getCreationConstructor().getParameters()) {
            Named named = param.getAnnotation(Named.class);
            names.add(named != null ? named.value() : "");
        }
        for (Field wiredField : getResultType().getDeclaredFields()) {
            if (!wiredField.isAnnotationPresent(Wired.class)) continue;
            Named named = wiredField.getAnnotation(Named.class);
            names.add(named != null ? named.value() : "");
        }
        return names;
    }

    @Override
    public Class<?> getResultType() {
        return creationClass;
    }

    @Override
    public String getName() {
        Component component = creationClass.getAnnotation(Component.class);
        if (component != null && !component.value().isEmpty()) {
            return component.value();
        }
        return "";
    }

    private Constructor<?> getCreationConstructor() {
        Constructor<?> constructor = null;
        for (Constructor<?> newConstructor : creationClass.getDeclaredConstructors()) {
            if (constructor == null || newConstructor.getParameterCount() > constructor.getParameterCount()) {
                constructor = newConstructor;
            }
        }

        if (constructor == null) {
            throw new IllegalStateException("Cannot find constructor for " + creationClass.getName());
        }

        return constructor;
    }
}
