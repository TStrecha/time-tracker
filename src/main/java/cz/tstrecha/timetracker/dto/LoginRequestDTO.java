package cz.tstrecha.timetracker.dto;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequestDTO {

    @Email
    private String email;
    @Length(min = 8)
    private String password;
}
