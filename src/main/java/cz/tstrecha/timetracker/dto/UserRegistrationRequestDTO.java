package cz.tstrecha.timetracker.dto;

import cz.tstrecha.timetracker.constant.AccountType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRegistrationRequestDTO {

    @NotNull
    @Email
    private String email;

    private String firstName;
    private String lastName;
    private String companyName;

    @NotNull
    @Size(min = 8)
    private String password;

    @NotNull
    private AccountType accountType;
}
