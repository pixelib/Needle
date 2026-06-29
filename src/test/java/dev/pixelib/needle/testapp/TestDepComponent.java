package dev.pixelib.needle.testapp;

import dev.pixelib.needle.api.Component;
import dev.pixelib.needle.api.Wired;

@Component
public class TestDepComponent {

    @Wired
    private TestSimpleComponent dep;

    public TestSimpleComponent getDep() {
        return dep;
    }
}
