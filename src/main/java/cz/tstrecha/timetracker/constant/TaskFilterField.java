package cz.tstrecha.timetracker.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum TaskFilterField {

    ID("id"),
    CUSTOM_ID("customId"),
    NAME_SIMPLE("nameSimple"),
    NOTE("note"),
    DESCRIPTION("description"),
    STATUS("status"),
    ESTIMATE("estimate"),
    ACTIVE("active"),
    CREATED_AT("updatedAt"),
    UPDATED_AT("updatedAt");

    @Getter
    private final String fieldName;
}

