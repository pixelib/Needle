package dev.pixelib.meteor.utils;

import lombok.experimental.UtilityClass;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@UtilityClass
public class ReflectionUtils {

    public <T extends Annotation> void callMethodWithAnnotation(Class<T> annotation, Object object) throws InvocationTargetException, IllegalAccessException {
        Method[] methods = object.getClass().getDeclaredMethods();

        for (Method method : methods) {
            T declaredAnnotation = method.getDeclaredAnnotation(annotation);
            if (declaredAnnotation == null) {
                continue;
            }
            method.invoke(object);
            return;
        }
    }
}
