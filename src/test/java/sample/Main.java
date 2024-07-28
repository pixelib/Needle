package sample;

import java.lang.reflect.Constructor;

public class Main {

    public static void main(String[] args) {
        for (Constructor<?> constructor : B.class.getConstructors()) {
            System.out.println(constructor.toString());
        }
    }
}
