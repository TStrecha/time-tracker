package cz.tstrecha.timetracker.dto;

import cz.tstrecha.timetracker.constant.AccountType;
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
    private AccountType accountType;
    private OffsetDateTime activeFrom;
    private OffsetDateTime activeTo;
    private boolean secureValues;
}
