package dev.pixelib.needle;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("NeedleSettings")
class NeedleSettingsTest {

    @Test
    @DisplayName("should create default settings with shutdown hook enabled")
    void shouldCreateDefaultSettings() {
        NeedleSettings settings = NeedleSettings.getDefaultSettings();

        assertTrue(settings.isShutdownHookAutoRegister());
    }

    @Test
    @DisplayName("should allow disabling shutdown hook")
    void shouldAllowDisablingShutdownHook() {
        NeedleSettings settings = NeedleSettings.getDefaultSettings();
        settings.setShutdownHookAutoRegister(false);

        assertFalse(settings.isShutdownHookAutoRegister());
    }
}
