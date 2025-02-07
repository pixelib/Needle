package sample;

import dev.pixelib.needle.api.Wired;
import lombok.Getter;
import sample.secret.C;

        @Getter
        public class B {

            @Wired
            private C depC;

        }
