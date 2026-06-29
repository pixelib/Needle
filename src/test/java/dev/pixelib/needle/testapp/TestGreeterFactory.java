package dev.pixelib.needle.testapp;

import dev.pixelib.needle.api.Component;

@Component
public class TestGreeterFactory {

    @Component("english")
    public String createEnglish() {
        return "Hello";
    }

    @Component("french")
    public String createFrench() {
        return "Bonjour";
    }
}
