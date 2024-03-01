package cz.tstrecha.timetracker.service.impl;

import cz.tstrecha.timetracker.dto.UserContext;
import cz.tstrecha.timetracker.service.EntityResolverService;
import cz.tstrecha.timetracker.service.PermissionService;
import cz.tstrecha.timetracker.util.ContextUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final EntityResolverService entityResolverService;

    @Override
    public boolean hasPermission(UserContext context, Object entity, String permission) {
        if (entity instanceof UserContext) {
            return ContextUtils.hasPermissions(context, permission);
        }

        throw new IllegalArgumentException(STR."Entity type of type [\{entity.getClass().getSimpleName()}] is not supported.");
    }

    @Override
    public boolean hasPermission(UserContext context, String entityType, Object targetId, String permission) {
        if(targetId instanceof Long id) {
            return ContextUtils.hasPermissions(context, permission) && entityResolverService.resolveUserIds(entityType, id).contains(context.getCurrentUserId());
        }

        throw new IllegalArgumentException(STR."Target id of type [\{targetId.getClass().getSimpleName()}] is not supported.");
    }
}
