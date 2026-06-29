package dev.pixelib.needle;

import dev.pixelib.needle.testapp.*;
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

        Map<Class<?>, Map<String, Object>> components = needle.getComponents();
        assertFalse(components.isEmpty());
        assertTrue(components.containsKey(TestSimpleComponent.class));
        assertTrue(components.containsKey(TestDepComponent.class));
    }

    @Test
    @DisplayName("should return same instance via getComponent and components map")
    void shouldReturnConsistentInstances() {
        Needle needle = Needle.init(TestApp.class);

        TestSimpleComponent fromGet = needle.getComponent(TestSimpleComponent.class);
        Object fromMap = needle.getComponents().get(TestSimpleComponent.class).get("");

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

    @Test
    @DisplayName("should inject named component via @Named on constructor parameter")
    void shouldInjectNamedConstructorParameter() {
        Needle needle = Needle.init(TestApp.class);

        TestNamedConsumer consumer = needle.getComponent(TestNamedConsumer.class);
        assertNotNull(consumer);
        assertEquals("Hello", consumer.greeting());
    }

    @Test
    @DisplayName("should retrieve named component by type and name")
    void shouldGetComponentByName() {
        Needle needle = Needle.init(TestApp.class);

        String english = needle.getComponent(String.class, "english");
        assertEquals("Hello", english);

        String french = needle.getComponent(String.class, "french");
        assertEquals("Bonjour", french);
    }

    @Test
    @DisplayName("should return null for unknown named component")
    void shouldReturnNullForUnknownName() {
        Needle needle = Needle.init(TestApp.class);

        assertNull(needle.getComponent(String.class, "nonexistent"));
    }

    @Test
    @DisplayName("should inject named component via @Wired @Named on field")
    void shouldInjectWiredNamedField() {
        Needle needle = Needle.init(TestApp.class);

        TestWiredNamedConsumer consumer = needle.getComponent(TestWiredNamedConsumer.class);
        assertNotNull(consumer);
        assertEquals("Hello", consumer.getGreeting());
    }

    @Test
    @DisplayName("should store named beans in components map")
    void shouldStoreNamedBeans() {
        Needle needle = Needle.init(TestApp.class);

        Map<String, Object> namedStrings = needle.getComponents().get(String.class);
        assertNotNull(namedStrings);
        assertEquals("Hello", namedStrings.get("english"));
        assertEquals("Bonjour", namedStrings.get("french"));
    }

    private static class UnregisteredClass {
    }
}
