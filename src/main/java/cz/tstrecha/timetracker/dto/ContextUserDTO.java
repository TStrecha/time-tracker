package cz.tstrecha.timetracker.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContextUserDTO {

    private Long id;
    private String email;
    private String fullName;
    private OffsetDateTime activeFrom;
    private OffsetDateTime activeTo;
    private boolean secureValues;
}
