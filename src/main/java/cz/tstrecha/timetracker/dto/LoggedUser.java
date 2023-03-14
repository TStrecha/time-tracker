package cz.tstrecha.timetracker.dto;

import cz.tstrecha.timetracker.repository.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoggedUser extends ContextUserDTO {

    private UserEntity userEntity;
}
