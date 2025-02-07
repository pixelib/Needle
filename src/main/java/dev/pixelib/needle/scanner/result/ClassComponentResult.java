package dev.pixelib.needle.scanner.result;

import dev.pixelib.needle.api.PostConstruct;
import dev.pixelib.needle.utils.ReflectionUtils;
import lombok.SneakyThrows;

import java.lang.reflect.Constructor;
import java.util.*;

public class ClassComponentResult extends AbstractScanResult {

    public final Class<?> creationClass;

    public ClassComponentResult(Class<?> creationClass) {
        this.creationClass = creationClass;
    }

    @Override
    @SneakyThrows
    protected Object doCreate(Object... parameters) {
        Object object = getCreationConstructor().newInstance(parameters);
        ReflectionUtils.callMethodWithAnnotation(PostConstruct.class, object);

        setFields(object, parameters);

        return object;
    }

    @Override
    public Collection<Class<?>> getDependencies() {
        List<Class<?>> paramDependencies = Arrays.asList(getCreationConstructor().getParameterTypes());
        paramDependencies.addAll(getWiredDependencies());
        return Collections.unmodifiableList(paramDependencies);
    }

    @Override
    public Class<?> getResultType() {
        return creationClass;
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
