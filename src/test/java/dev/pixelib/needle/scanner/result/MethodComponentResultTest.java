package dev.pixelib.needle.scanner.result;

import dev.pixelib.needle.api.Component;
import dev.pixelib.needle.api.Wired;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MethodComponentResult")
class MethodComponentResultTest {

    @Test
    @DisplayName("should create instance via factory method")
    void shouldCreateViaFactoryMethod() throws Exception {
        Method factoryMethod = FactoryHost.class.getDeclaredMethod("createSimple");
        MethodComponentResult result = new MethodComponentResult(FactoryHost.class, factoryMethod);

        Object instance = result.create(new FactoryHost());

        assertNotNull(instance);
        assertEquals("simple", instance);
    }

    @Test
    @DisplayName("should pass method parameters when invoking factory")
    void shouldPassMethodParameters() throws Exception {
        Method factoryMethod = ParameterizedFactory.class.getDeclaredMethod("create", String.class, Integer.class);
        MethodComponentResult result = new MethodComponentResult(ParameterizedFactory.class, factoryMethod);

        Object instance = result.create(new ParameterizedFactory(), "prefix", 42);

        assertNotNull(instance);
        assertEquals("prefix-42", instance);
    }

    @Test
    @DisplayName("should inject Wired fields on factory-created instance")
    void shouldInjectWiredFields() throws Exception {
        Method factoryMethod = FactoryHost.class.getDeclaredMethod("createWiredTarget");
        MethodComponentResult result = new MethodComponentResult(FactoryHost.class, factoryMethod);

        Object instance = result.create(new FactoryHost(), "injected-value");

        assertNotNull(instance);
        assertEquals("injected-value", ((WiredTarget) instance).getValue());
    }

    @Test
    @DisplayName("getDependencies should include parent class, method params, and wired fields")
    void getDependenciesShouldIncludeAll() throws Exception {
        Method factoryMethod = ParameterizedFactory.class.getDeclaredMethod("create", String.class, Integer.class);
        MethodComponentResult result = new MethodComponentResult(ParameterizedFactory.class, factoryMethod);

        Collection<Class<?>> deps = result.getDependencies();

        assertTrue(deps.contains(ParameterizedFactory.class));
        assertTrue(deps.contains(String.class));
        assertTrue(deps.contains(Integer.class));
    }

    @Test
    @DisplayName("getResultType should return factory method return type")
    void getResultTypeShouldReturnMethodReturnType() throws Exception {
        Method factoryMethod = FactoryHost.class.getDeclaredMethod("createSimple");
        MethodComponentResult result = new MethodComponentResult(FactoryHost.class, factoryMethod);

        assertEquals(String.class, result.getResultType());
    }

    static class FactoryHost {
        @Component
        String createSimple() {
            return "simple";
        }

        @Component
        WiredTarget createWiredTarget() {
            return new WiredTarget();
        }
    }

    static class ParameterizedFactory {
        @Component
        String create(String prefix, Integer num) {
            return prefix + "-" + num;
        }
    }

    public static class WiredTarget {
        @Wired
        private String value;

        public String getValue() {
            return value;
        }
    }
}
