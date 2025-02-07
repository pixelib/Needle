package dev.pixelib.needle;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class NeedleSettings {

    public static NeedleSettings getDefaultSettings() {
        return new NeedleSettings();
    }

    private NeedleSettings() {
    }

    private boolean shutdownHookAutoRegister = true;
}
