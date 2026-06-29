package dev.pixelib.needle;

import dev.pixelib.needle.testapp.TestApp;
import dev.pixelib.needle.testapp.TestDepComponent;
import dev.pixelib.needle.testapp.TestSimpleComponent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Needle")
class NeedleTest {

    @Test
    @DisplayName("should initialize and create components from package scan")
    void shouldInitializeAndCreateComponents() {
        Needle needle = Needle.init(TestApp.class);

        assertNotNull(needle.getComponent(TestSimpleComponent.class));
        assertNotNull(needle.getComponent(TestDepComponent.class));
    }

    @Test
    @DisplayName("should wire dependencies between components")
    void shouldWireDependencies() {
        Needle needle = Needle.init(TestApp.class);

        TestDepComponent depComp = needle.getComponent(TestDepComponent.class);
        assertNotNull(depComp.getDep());
    }

    @Test
    @DisplayName("should populate components map with all scanned types")
    void shouldPopulateComponentsMap() {
        Needle needle = Needle.init(TestApp.class);

        Map<Class<?>, Object> components = needle.getComponents();
        assertFalse(components.isEmpty());
        assertTrue(components.containsKey(TestSimpleComponent.class));
        assertTrue(components.containsKey(TestDepComponent.class));
    }

    @Test
    @DisplayName("should return same instance via getComponent and components map")
    void shouldReturnConsistentInstances() {
        Needle needle = Needle.init(TestApp.class);

        TestSimpleComponent fromGet = needle.getComponent(TestSimpleComponent.class);
        Object fromMap = needle.getComponents().get(TestSimpleComponent.class);

        assertSame(fromGet, fromMap);
    }

    @Test
    @DisplayName("should accept custom settings via consumer")
    void shouldAcceptCustomSettings() {
        Needle needle = Needle.init(TestApp.class, settings -> settings.setShutdownHookAutoRegister(false));

        assertNotNull(needle.getComponent(TestSimpleComponent.class));
    }

    @Test
    @DisplayName("should return null for unregistered component type")
    void shouldReturnNullForUnregisteredType() {
        Needle needle = Needle.init(TestApp.class);

        assertNull(needle.getComponent(UnregisteredClass.class));
    }

    private static class UnregisteredClass {
    }
}
