package cz.tstrecha.timetracker.core;


import cz.tstrecha.timetracker.controller.exception.PermissionException;
import cz.tstrecha.timetracker.util.ContextUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

public class PermissionsTest {

    @ParameterizedTest
    @ValueSource(strings = {"*", "*.*", "user.*", "*.read", "user.read"})
    public void test01_simple_success(String permission) {
        List<String> permissions = List.of(permission);
        Assertions.assertTrue(ContextUtils.hasPermissions(permissions, "user.read"));
    }

    @Test
    public void test02_morePermissions_success() {
        List<String> permissions = List.of("report.read", "report.create", "user.read", "user.update", "settings.read", "settings.update");
        Assertions.assertTrue(ContextUtils.hasPermissions(permissions, "settings.read"));
        Assertions.assertFalse(ContextUtils.hasPermissions(permissions, "api.read"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"settings.*", "*.update", "settings.update"})
    public void test03_simple_success(String permission) {
        List<String> permissions = List.of(permission);
        Assertions.assertTrue(ContextUtils.hasPermissions(permissions, "settings.update"));
        Assertions.assertFalse(ContextUtils.hasPermissions(permissions, "report.read"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"api,create", "settings*", "api/read", "report@read"})
    public void test04_simple_fail(String permission) {
        List<String> permissions = List.of(permission);
        Assertions.assertThrows(PermissionException.class, () -> ContextUtils.hasPermissions(permissions, "*"));
        Assertions.assertThrows(PermissionException.class, () -> ContextUtils.hasPermissions(List.of("api.read"), permission));
    }
}
