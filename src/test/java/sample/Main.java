package sample;

import dev.pixelib.meteor.Needle;

public class Main {

    public static void main(String[] args) {
        Needle init = Needle.init(Main.class);

        System.out.println("Found");
        init.getComponents().forEach((key, value) -> {
            System.out.println(key.getSimpleName() + ":" + value.toString());
        });
    }
}
