package cz.tstrecha.timetracker.annotation;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Repeatable(PermissionChecks.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface PermissionCheck {

    String value();

}
