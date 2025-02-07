package dev.pixelib.meteor;

import dev.pixelib.meteor.api.PreDestroy;
import dev.pixelib.meteor.scanner.DependencyScanner;
import dev.pixelib.meteor.scanner.result.AbstractScanResult;
import dev.pixelib.meteor.utils.ReflectionUtils;
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
    private final Map<Class<?>, Object> components = new HashMap<>();

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
        return clazz.cast(components.get(clazz));
    }

    private void createInstances() {
        if (settings.isShutdownHookAutoRegister()) {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> components.values().forEach(this::invokeShutdown), "needle-shutdown-hook"));
        }

        DependencyScanner dependencyScanner = new DependencyScanner(this.reflections);
        List<AbstractScanResult> scannedComponents = dependencyScanner.findAllComponents();

        for (AbstractScanResult scannedComponent : scannedComponents) {
            Object[] requiredDependencies = scannedComponent.getDependencies().stream().map(components::get).toArray(Object[]::new);

            components.put(scannedComponent.getResultType(), scannedComponent.create(requiredDependencies));
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
