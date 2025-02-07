package sample;

import dev.pixelib.meteor.api.Component;

@Component
public class A {

    @Component
    B create() {
        return new B();
    }
}
