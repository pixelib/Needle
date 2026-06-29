package dev.pixelib.needle.testapp;

import dev.pixelib.needle.api.Component;
import dev.pixelib.needle.api.Named;
import dev.pixelib.needle.api.Wired;

@Component
public class TestWiredNamedConsumer {

    @Wired
    @Named("english")
    private String greeting;

    public String getGreeting() {
        return greeting;
    }
}
