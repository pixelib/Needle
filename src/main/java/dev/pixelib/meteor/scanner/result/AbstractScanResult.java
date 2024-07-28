package dev.pixelib.meteor.scanner.result;

import lombok.SneakyThrows;

import java.util.Collection;

public abstract class AbstractScanResult {

    protected abstract Object doCreate(Object... parameters);

    @SneakyThrows
    public Object create(Object... parameters) {
        return doCreate(parameters);
    }

    public abstract Collection<Class<?>> getDependencies();

    public abstract Class<?> getResultType();
}
