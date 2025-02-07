package sample;

import dev.pixelib.needle.api.Component;

@Component
public class A {

    @Component
    B create() {
        return new B();
    }
}
