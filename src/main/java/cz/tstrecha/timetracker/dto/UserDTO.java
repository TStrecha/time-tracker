package cz.tstrecha.timetracker.dto;

import cz.tstrecha.timetracker.constant.AccountType;
import cz.tstrecha.timetracker.constant.SecretMode;
import cz.tstrecha.timetracker.constant.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {

    private Long id;

    private String email;
    private String firstName;
    private String lastName;
    private String companyName;

    private String displayName;

    private UserRole role;
    private AccountType accountType;
    private SecretMode secretMode;

    private OffsetDateTime createdAt;
    private OffsetDateTime modifiedAt;

    private List<RelationshipDTO> relationsReceiving;
    private List<RelationshipDTO> relationsGiving;
}
