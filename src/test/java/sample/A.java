package sample;

import dev.pixelib.meteor.api.Component;
import sample.secret.C;

@Component
public class A {

    @Component
    B create(C c) {
        return new B(c);
    }
}
