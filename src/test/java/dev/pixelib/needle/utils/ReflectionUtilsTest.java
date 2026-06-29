package dev.pixelib.needle.utils;

import dev.pixelib.needle.api.PostConstruct;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ReflectionUtils")
class ReflectionUtilsTest {

    @Test
    @DisplayName("should call method annotated with given annotation")
    void shouldCallPostConstructMethod() throws Exception {
        PostConstructTarget target = new PostConstructTarget();

        ReflectionUtils.callMethodWithAnnotation(PostConstruct.class, target);

        assertTrue(target.called);
    }

    @Test
    @DisplayName("should do nothing when no method has the annotation")
    void shouldDoNothingWhenNoAnnotatedMethod() throws Exception {
        ReflectionUtils.callMethodWithAnnotation(PostConstruct.class, new Object());
    }

    @Test
    @DisplayName("should propagate InvocationTargetException when annotated method throws")
    void shouldThrowWhenAnnotatedMethodThrows() {
        ThrowingTarget target = new ThrowingTarget();

        InvocationTargetException ex = assertThrows(InvocationTargetException.class,
                () -> ReflectionUtils.callMethodWithAnnotation(PostConstruct.class, target));
        assertInstanceOf(RuntimeException.class, ex.getCause());
        assertEquals("fail", ex.getCause().getMessage());
    }

    static class PostConstructTarget {
        boolean called = false;

        @PostConstruct
        void init() {
            called = true;
        }
    }

    static class ThrowingTarget {
        @PostConstruct
        void init() {
            throw new RuntimeException("fail");
        }
    }
}
