package cz.tstrecha.timetracker.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum TaskField {

    ID("id"),
    CUSTOM_ID("customId"),
    NAME_SIMPLE("nameSimple"),
    NOTE("note"),
    DESCRIPTION("description"),
    STATUS("status"),
    ESTIMATE("estimate"),
    ACTIVE("active"),
    UPDATED_AT("updatedAt");

    @Getter
    private final String fieldName;
}

