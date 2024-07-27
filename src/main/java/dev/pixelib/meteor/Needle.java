package dev.pixelib.meteor;

import dev.pixelib.meteor.api.PreDestroy;
import dev.pixelib.meteor.scanner.DependencyScanner;
import dev.pixelib.meteor.utils.ReflectionUtils;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Needle {

    private final Logger log;

    private final Class<?> appClass;
    private final Reflections reflections;
    private final List<Object> components = new ArrayList<>();

    public static Needle init(Class<?> app) {
        Needle needle = new Needle(app);
        needle.init();
        return needle;
    }

    private Needle(Class<?> app) {
        this.log = Logger.getLogger(Needle.class.getSimpleName() + "-" + app.getSimpleName());
        this.appClass = app;
        this.reflections = new Reflections(app.getPackage().getName());
    }

    private void init() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> components.forEach(this::invokeShutdown), "needle-shutdown-hook"));

        DependencyScanner dependencyScanner = new DependencyScanner(this.reflections);
        dependencyScanner.scan();
    }

    private void invokeShutdown(Object object) {
        try {
            ReflectionUtils.callMethodWithAnnotation(PreDestroy.class, object);
        } catch (InvocationTargetException | IllegalAccessException e) {
            log.log(Level.SEVERE, String.format("Could not call PreDestroy method on %s", object.getClass().getSimpleName()), e);
        }
    }
}
