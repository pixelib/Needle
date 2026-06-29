package dev.pixelib.needle.scanner.result;

import dev.pixelib.needle.api.Wired;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AbstractScanResult")
class AbstractScanResultTest {

    @Test
    @DisplayName("create should delegate to doCreate")
    void createShouldDelegateToDoCreate() {
        AbstractScanResult result = new TestScanResult();

        Object created = result.create("arg");

        assertEquals("created:arg", created);
    }

    @Test
    @DisplayName("getWiredDependencies should return empty for class with no Wired fields")
    void getWiredDependenciesShouldReturnEmptyForNoWiredFields() {
        AbstractScanResult result = new TestScanResult();

        Collection<Class<?>> deps = result.getWiredDependencies();

        assertTrue(deps.isEmpty());
    }

    @Test
    @DisplayName("getWiredDependencies should return field types for class with Wired fields")
    void getWiredDependenciesShouldReturnWiredFieldTypes() {
        AbstractScanResult result = new AbstractScanResult() {
            @Override
            protected Object doCreate(Object... parameters) {
                return new WiredTarget();
            }

            @Override
            public Collection<Class<?>> getDependencies() {
                return List.of();
            }

            @Override
            public Class<?> getResultType() {
                return WiredTarget.class;
            }

            @Override
            public String getName() {
                return "";
            }
        };

        Collection<Class<?>> deps = result.getWiredDependencies();

        assertEquals(1, deps.size());
        assertTrue(deps.contains(String.class));
    }

    @Test
    @DisplayName("setFields should inject matching parameters into Wired fields")
    void setFieldsShouldInjectMatchingParameters() {
        AbstractScanResult result = new AbstractScanResult() {
            @Override
            protected Object doCreate(Object... parameters) {
                return new WiredTarget();
            }

            @Override
            public Collection<Class<?>> getDependencies() {
                return List.of();
            }

            @Override
            public Class<?> getResultType() {
                return WiredTarget.class;
            }

            @Override
            public String getName() {
                return "";
            }
        };

        WiredTarget target = new WiredTarget();
        result.setFields(target, "injected-value");

        assertEquals("injected-value", target.getStringVal());
    }

    @Test
    @DisplayName("setFields should throw when no matching parameter found for Wired field")
    void setFieldsShouldThrowWhenNoMatchingParameter() {
        AbstractScanResult result = new AbstractScanResult() {
            @Override
            protected Object doCreate(Object... parameters) {
                return new WiredTarget();
            }

            @Override
            public Collection<Class<?>> getDependencies() {
                return List.of();
            }

            @Override
            public Class<?> getResultType() {
                return WiredTarget.class;
            }

            @Override
            public String getName() {
                return "";
            }
        };

        WiredTarget target = new WiredTarget();
        assertThrows(IllegalStateException.class, () -> result.setFields(target, 42));
    }

    private static class TestScanResult extends AbstractScanResult {
        @Override
        protected Object doCreate(Object... parameters) {
            return "created:" + (parameters.length > 0 ? parameters[0] : "");
        }

        @Override
        public Collection<Class<?>> getDependencies() {
            return List.of();
        }

        @Override
        public Class<?> getResultType() {
            return String.class;
        }

        @Override
        public String getName() {
            return "";
        }
    }

    public static class WiredTarget {
        @Wired
        private String stringVal;

        public String getStringVal() {
            return stringVal;
        }
    }
}
