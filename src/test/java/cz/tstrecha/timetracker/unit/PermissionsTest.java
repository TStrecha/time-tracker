package cz.tstrecha.timetracker.unit;

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
    void should_HavePermission_When_ValidPermissionIsPresent(String permission) {
        var permissions = List.of(permission);

        Assertions.assertTrue(ContextUtils.hasPermissions(permissions, "user.read"));
    }

    @Test
    void should_HavePermission_When_AtLeastOneValidPermissionIsPresent() {
        var permissions = List.of("report.read", "report.create", "user.read", "user.update", "settings.read", "settings.update");

        Assertions.assertTrue(ContextUtils.hasPermissions(permissions, "settings.read"));
        Assertions.assertFalse(ContextUtils.hasPermissions(permissions, "api.read"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"api,create", "settings*", "api/read", "report@read", "task"})
    void should_ThrowException_When_InvalidPermissionFormatGiven(String permission) {
        var permissions = List.of(permission);
        var apiReadPermission = List.of("api.read");

        Assertions.assertThrows(PermissionException.class, () -> ContextUtils.hasPermissions(permissions, "*"));
        Assertions.assertThrows(PermissionException.class, () -> ContextUtils.hasPermissions(apiReadPermission, permission));
    }
}
