package cz.tstrecha.timetracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RelationshipDTO {

    private Long oppositeUserId;
    private String displayName;

    private List<String> permissions;

    private OffsetDateTime activeFrom;
    private OffsetDateTime activeTo;

    private boolean secureValues;

}
