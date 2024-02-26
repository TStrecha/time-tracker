package cz.tstrecha.timetracker.dto;

import cz.tstrecha.timetracker.constant.AccountType;
import cz.tstrecha.timetracker.constant.SecretMode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateDTO {
    private AccountType accountType;
    private SecretMode secretMode = SecretMode.NONE;
    private String firstName;
    private String lastName;
    private String companyName;
}
