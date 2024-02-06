package cz.tstrecha.timetracker.core;


import cz.tstrecha.timetracker.controller.exception.PermissionException;
import cz.tstrecha.timetracker.util.ContextUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

class PermissionsTest {

    @ParameterizedTest
    @ValueSource(strings = {"*", "*.*", "user.*", "*.read", "user.read"})
    void test01_simple_success(String permission) {
        var permissions = List.of(permission);
        Assertions.assertTrue(ContextUtils.hasPermissions(permissions, "user.read"));
    }

    @Test
    void test02_morePermissions_success() {
        var permissions = List.of("report.read", "report.create", "user.read", "user.update", "settings.read", "settings.update");
        Assertions.assertTrue(ContextUtils.hasPermissions(permissions, "settings.read"));
        Assertions.assertFalse(ContextUtils.hasPermissions(permissions, "api.read"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"settings.*", "*.update", "settings.update"})
    void test03_simple_success(String permission) {
        var permissions = List.of(permission);
        Assertions.assertTrue(ContextUtils.hasPermissions(permissions, "settings.update"));
        Assertions.assertFalse(ContextUtils.hasPermissions(permissions, "report.read"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"api,create", "settings*", "api/read", "report@read"})
    void test04_simple_fail(String permission) {
        var permissions = List.of(permission);
        var apiReadPermission = List.of("api.read");

        Assertions.assertThrows(PermissionException.class, () -> ContextUtils.hasPermissions(permissions, "*"));
        Assertions.assertThrows(PermissionException.class, () -> ContextUtils.hasPermissions(apiReadPermission, permission));
    }
}
