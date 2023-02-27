package cz.tstrecha.timetracker.annotation;

import cz.tstrecha.timetracker.constant.PermissionCheckOperation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface PermissionChecks {

    PermissionCheck[] value();

    PermissionCheckOperation operation() default PermissionCheckOperation.AND;
}
