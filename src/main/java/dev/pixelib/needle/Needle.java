package dev.pixelib.needle;

import dev.pixelib.needle.api.PreDestroy;
import dev.pixelib.needle.scanner.DependencyScanner;
import dev.pixelib.needle.scanner.result.AbstractScanResult;
import dev.pixelib.needle.utils.ReflectionUtils;
import lombok.Getter;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Needle {

    private final Logger log;

    private final NeedleSettings settings;
    private final Class<?> appClass;
    private final Reflections reflections;

    @Getter
    private final Map<Class<?>, Map<String, Object>> components = new HashMap<>();

    public static Needle init(Class<?> app) {
        return init(app, unused -> {
        });
    }

    public static Needle init(Class<?> app, Consumer<NeedleSettings> settings) {
        NeedleSettings needleSettings = NeedleSettings.getDefaultSettings();
        settings.accept(needleSettings);
        Needle needle = new Needle(app, needleSettings);
        needle.createInstances();
        return needle;
    }

    private Needle(Class<?> app, NeedleSettings settings) {
        this.settings = settings;
        this.log = Logger.getLogger(Needle.class.getSimpleName() + "-" + app.getSimpleName());
        this.appClass = app;
        this.reflections = new Reflections(app.getPackage().getName());
    }

    public <T> T getComponent(Class<T> clazz) {
        Map<String, Object> namedBeans = components.get(clazz);
        if (namedBeans == null || namedBeans.isEmpty()) return null;
        if (namedBeans.size() == 1) return clazz.cast(namedBeans.values().iterator().next());

        Object unnamed = namedBeans.get("");
        if (unnamed != null) return clazz.cast(unnamed);

        throw new IllegalStateException("Multiple named beans of type " + clazz.getSimpleName()
                + " found. Use getComponent(Class, String) or @Named to select one.");
    }

    public <T> T getComponent(Class<T> clazz, String name) {
        Map<String, Object> namedBeans = components.get(clazz);
        if (namedBeans == null) return null;
        return clazz.cast(namedBeans.get(name));
    }

    private void createInstances() {
        if (settings.isShutdownHookAutoRegister()) {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                for (Map<String, Object> namedBeans : components.values()) {
                    namedBeans.values().forEach(this::invokeShutdown);
                }
            }, "needle-shutdown-hook"));
        }

        DependencyScanner dependencyScanner = new DependencyScanner(this.reflections);
        List<AbstractScanResult> scannedComponents = dependencyScanner.findAllComponents();

        for (AbstractScanResult scannedComponent : scannedComponents) {
            Object instance = scannedComponent.create(components);

            components.computeIfAbsent(scannedComponent.getResultType(), k -> new HashMap<>())
                    .put(scannedComponent.getName(), instance);
        }
    }

    private void invokeShutdown(Object object) {
        try {
            ReflectionUtils.callMethodWithAnnotation(PreDestroy.class, object);
        } catch (InvocationTargetException | IllegalAccessException e) {
            log.log(Level.SEVERE, String.format("Could not call PreDestroy method on %s", object.getClass().getSimpleName()), e);
        }
    }
}
