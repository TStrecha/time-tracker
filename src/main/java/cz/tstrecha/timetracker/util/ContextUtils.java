package cz.tstrecha.timetracker.util;

import cz.tstrecha.timetracker.controller.exception.PermissionException;
import cz.tstrecha.timetracker.dto.UserContext;
import lombok.experimental.UtilityClass;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

@UtilityClass
public class ContextUtils {

    public Optional<UserContext> retrieveContext() {
        var principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(principal instanceof UserContext){
            var context = (UserContext) principal;
            return Optional.of(context);
        }
        return Optional.empty();
    }

    public UserContext retrieveContextMandatory() {
        return retrieveContext()
                .orElseThrow(() -> new IllegalStateException("Context was not present in security holder but was marked as mandatory requirement."));
    }

    public boolean hasPermissions(List<String> permissions, String requiredPermission){
        if(permissions.isEmpty()){
            return false;
        }
        return permissions.stream().anyMatch(permission -> permissionSatisfiesCondition(permission, requiredPermission));
    }

    public boolean hasPermissions(UserContext userContext, String requiredPermission){
        return hasPermissions(userContext.getActivePermissions(), requiredPermission);
    }

    public boolean permissionSatisfiesCondition(String permission, String requiredPermission) {
        if (permission.equals(requiredPermission)) {
            return true;
        }
        if (permission.equals("*") || permission.equals("*.*")) {
            return true;
        }
        if (!permission.contains(".") || !requiredPermission.contains(".")) {
            throw new PermissionException("Permission or required permission didn't match required pattern: [module.action]");
        }
        String[] splitPermission = permission.split("\\.");
        String permissionModulePart = splitPermission[0];
        String permissionActionPart = splitPermission[1];

        String[] splitRequiredPermission = requiredPermission.split("\\.");
        String requiredModulePart = splitRequiredPermission[0];
        String requiredActionPart = splitRequiredPermission[1];

        if (permissionModulePart.equals("*")) {
            return permissionActionPart.equals(requiredActionPart);
        } else if (permissionActionPart.equals("*")) {
            return permissionModulePart.equals(requiredModulePart);
        }
        return false;
    }
}
