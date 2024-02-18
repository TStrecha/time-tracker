package cz.tstrecha.timetracker.service;

import cz.tstrecha.timetracker.dto.UserContext;

public interface PermissionService {

    boolean hasPermission(UserContext context, Object entity, String permission);

    boolean hasPermission(UserContext context, String entityType, Object targetId, String permission);
}
