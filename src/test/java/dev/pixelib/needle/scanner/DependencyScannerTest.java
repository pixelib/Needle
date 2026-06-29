package dev.pixelib.needle.scanner;

import dev.pixelib.needle.api.Component;
import dev.pixelib.needle.api.Wired;
import dev.pixelib.needle.scanner.result.AbstractScanResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reflections.Reflections;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DependencyScanner")
class DependencyScannerTest {

    @Mock
    private Reflections reflections;

    private DependencyScanner scanner;

    @BeforeEach
    void setUp() {
        scanner = new DependencyScanner(reflections);
    }

    @Test
    @DisplayName("should return empty list when no components found")
    void shouldReturnEmptyForNoComponents() {
        when(reflections.getTypesAnnotatedWith(Component.class)).thenReturn(Set.of());

        List<AbstractScanResult> results = scanner.findAllComponents();

        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("should find single component with no dependencies")
    void shouldFindSingleComponent() {
        when(reflections.getTypesAnnotatedWith(Component.class)).thenReturn(Set.of(SimpleComponent.class));

        List<AbstractScanResult> results = scanner.findAllComponents();

        assertEquals(1, results.size());
        assertEquals(SimpleComponent.class, results.getFirst().getResultType());
    }

    @Test
    @DisplayName("should find components with field-injected dependencies")
    void shouldFindFieldDepComponents() {
        when(reflections.getTypesAnnotatedWith(Component.class))
                .thenReturn(Set.of(SimpleComponent.class, FieldDepComponent.class));

        List<AbstractScanResult> results = scanner.findAllComponents();

        assertEquals(2, results.size());
    }

    @Test
    @DisplayName("should detect circular dependencies")
    void shouldDetectCircularDependency() {
        when(reflections.getTypesAnnotatedWith(Component.class))
                .thenReturn(Set.of(CircularA.class, CircularB.class));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> scanner.findAllComponents());
        assertTrue(ex.getMessage().contains("Circular dependency found"));
    }

    @Test
    @DisplayName("should detect self-dependency")
    void shouldDetectSelfDependency() {
        when(reflections.getTypesAnnotatedWith(Component.class))
                .thenReturn(Set.of(SelfDepComponent.class));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> scanner.findAllComponents());
        assertTrue(ex.getMessage().contains("can not be dependent on itself"));
    }

    @Test
    @DisplayName("should detect missing dependencies")
    void shouldDetectMissingDependency() {
        when(reflections.getTypesAnnotatedWith(Component.class))
                .thenReturn(Set.of(MissingDepComponent.class));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> scanner.findAllComponents());
        assertTrue(ex.getMessage().contains("not found for"));
    }

    @Test
    @DisplayName("should order components by dependency count (lowest first)")
    void shouldOrderByDependencyCount() {
        when(reflections.getTypesAnnotatedWith(Component.class))
                .thenReturn(Set.of(SimpleComponent.class, FieldDepComponent.class, ChainComponent.class));

        List<AbstractScanResult> results = scanner.findAllComponents();

        assertEquals(3, results.size());
        assertEquals(SimpleComponent.class, results.get(0).getResultType());
        assertEquals(FieldDepComponent.class, results.get(1).getResultType());
        assertEquals(ChainComponent.class, results.get(2).getResultType());
    }

    @Test
    @DisplayName("should handle components with no annotated methods")
    void shouldHandleComponentsWithNoMethodComponents() {
        when(reflections.getTypesAnnotatedWith(Component.class))
                .thenReturn(Set.of(SimpleComponent.class));

        List<AbstractScanResult> results = scanner.findAllComponents();

        assertEquals(1, results.size());
    }

    @Component
    static class SimpleComponent {
    }

    @Component
    static class FieldDepComponent {
        @Wired
        private SimpleComponent dep;
    }

    @Component
    static class ChainComponent {
        @Wired
        private FieldDepComponent dep;
    }

    @Component
    static class CircularA {
        @Wired
        private CircularB dep;
    }

    @Component
    static class CircularB {
        @Wired
        private CircularA dep;
    }

    @Component
    static class SelfDepComponent {
        @Wired
        private SelfDepComponent dep;
    }

    @Component
    static class MissingDepComponent {
        @Wired
        private UnknownComponent dep;
    }

    static class UnknownComponent {
    }
}
