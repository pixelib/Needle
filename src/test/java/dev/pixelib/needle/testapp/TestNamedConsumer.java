package dev.pixelib.needle.testapp;

import dev.pixelib.needle.api.Component;
import dev.pixelib.needle.api.Named;

@Component
public record TestNamedConsumer(String greeting) {

    public TestNamedConsumer(@Named("english") String greeting) {
        this.greeting = greeting;
    }
}
