package dev.pixelib.needle.scanner.result;

import dev.pixelib.needle.api.Component;
import dev.pixelib.needle.api.Named;
import dev.pixelib.needle.api.Wired;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

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

    @Test
    @DisplayName("getName should return @Component value from method")
    void getNameShouldReturnComponentValue() throws Exception {
        Method factoryMethod = NamedFactory.class.getDeclaredMethod("createNamed");
        MethodComponentResult result = new MethodComponentResult(NamedFactory.class, factoryMethod);

        assertEquals("namedBean", result.getName());
    }

    @Test
    @DisplayName("getName should return empty when @Component has no value on method")
    void getNameShouldReturnEmptyWhenNoValue() throws Exception {
        Method factoryMethod = FactoryHost.class.getDeclaredMethod("createSimple");
        MethodComponentResult result = new MethodComponentResult(FactoryHost.class, factoryMethod);

        assertEquals("", result.getName());
    }

    @Test
    @DisplayName("getDependencyNames should include parent as empty and method params with @Named")
    void getDependencyNamesShouldIncludeNamedParams() throws Exception {
        Method factoryMethod = UnnamedFactory.class.getDeclaredMethod("create", String.class);
        MethodComponentResult result = new MethodComponentResult(UnnamedFactory.class, factoryMethod);

        List<String> names = result.getDependencyNames();

        assertEquals(2, names.size()); // parent + 1 method param
        assertEquals("", names.get(0)); // parent - no @Named
        assertEquals("prefix", names.get(1)); // method param with @Named
    }

    @Test
    @DisplayName("getDependencyNames should include wired field names for named wired fields")
    void getDependencyNamesShouldIncludeWiredFieldNames() throws Exception {
        Method factoryMethod = WiredNamedFactory.class.getDeclaredMethod("createWiredNamed");
        MethodComponentResult result = new MethodComponentResult(WiredNamedFactory.class, factoryMethod);

        List<String> names = result.getDependencyNames();

        assertEquals(2, names.size()); // parent + 1 wired field
        assertEquals("", names.get(0)); // parent - no @Named
        assertEquals("suffix", names.get(1)); // wired field with @Named
    }

    static class NamedFactory {
        @Component("namedBean")
        String createNamed() {
            return "named";
        }
    }

    static class UnnamedFactory {
        @Component
        String create(@Named("prefix") String p) {
            return p;
        }
    }

    public static class WiredNamedTarget {
        @Wired
        @Named("suffix")
        private String value;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
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

    static class WiredNamedFactory {
        @Component
        WiredNamedTarget createWiredNamed() {
            return new WiredNamedTarget();
        }
    }
}
