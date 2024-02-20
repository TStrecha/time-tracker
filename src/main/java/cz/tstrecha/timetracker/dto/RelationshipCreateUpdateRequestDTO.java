package cz.tstrecha.timetracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RelationshipCreateUpdateRequestDTO {

    private Long id;

    private Long toId;

    private List<String> permissions;

    private OffsetDateTime activeFrom = OffsetDateTime.now();
    private OffsetDateTime activeTo;

    private boolean secureValues;

}
