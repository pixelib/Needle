package dev.pixelib.needle.scanner.result;

import dev.pixelib.needle.api.Component;
import dev.pixelib.needle.api.PostConstruct;
import dev.pixelib.needle.api.Wired;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ClassComponentResult")
class ClassComponentResultTest {

    @Test
    @DisplayName("should create instance of the target class")
    void shouldCreateInstance() {
        ClassComponentResult result = new ClassComponentResult(SimpleComponent.class);

        Object instance = result.create();

        assertNotNull(instance);
        assertInstanceOf(SimpleComponent.class, instance);
    }

    @Test
    @DisplayName("should pass constructor parameters during creation")
    void shouldPassConstructorParameters() {
        ClassComponentResult result = new ClassComponentResult(ConstructorDepComponent.class);

        Object instance = result.create("hello");

        assertNotNull(instance);
        assertEquals("hello", ((ConstructorDepComponent) instance).getDep());
    }

    @Test
    @DisplayName("should create instance with largest constructor when multiple exist")
    void shouldUseLargestConstructor() {
        ClassComponentResult result = new ClassComponentResult(MultiConstructorComponent.class);

        Object instance = result.create("a", "b");

        assertNotNull(instance);
        assertInstanceOf(MultiConstructorComponent.class, instance);
    }

    @Test
    @DisplayName("should invoke PostConstruct method after creation")
    void shouldInvokePostConstruct() {
        ClassComponentResult result = new ClassComponentResult(PostConstructComponent.class);

        Object instance = result.create();

        assertTrue(((PostConstructComponent) instance).isInitialized());
    }

    @Test
    @DisplayName("should inject Wired fields after creation")
    void shouldInjectWiredFields() {
        ClassComponentResult result = new ClassComponentResult(WiredFieldComponent.class);

        Object instance = result.create("field-value");

        assertNotNull(instance);
        assertEquals("field-value", ((WiredFieldComponent) instance).getDep());
    }

    @Test
    @DisplayName("getDependencies should include constructor params and wired fields")
    void getDependenciesShouldIncludeConstructorAndWiredParams() {
        ClassComponentResult result = new ClassComponentResult(ComplexComponent.class);

        Collection<Class<?>> deps = result.getDependencies();

        assertTrue(deps.contains(String.class));
        assertTrue(deps.contains(Integer.class));
    }

    @Test
    @DisplayName("getResultType should return the creation class")
    void getResultTypeShouldReturnCreationClass() {
        ClassComponentResult result = new ClassComponentResult(SimpleComponent.class);

        assertEquals(SimpleComponent.class, result.getResultType());
    }

    @Component
    static class SimpleComponent {
    }

    static class ConstructorDepComponent {
        private final String dep;

        ConstructorDepComponent(String dep) {
            this.dep = dep;
        }

        String getDep() {
            return dep;
        }
    }

    static class MultiConstructorComponent {
        MultiConstructorComponent() {
        }

        MultiConstructorComponent(String a, String b) {
        }
    }

    static class PostConstructComponent {
        private boolean initialized = false;

        @PostConstruct
        void init() {
            initialized = true;
        }

        boolean isInitialized() {
            return initialized;
        }
    }

    static class WiredFieldComponent {
        @Wired
        private String dep;

        String getDep() {
            return dep;
        }
    }

    static class ComplexComponent {
        ComplexComponent(String s) {
        }

        @Wired
        private Integer number;
    }
}
