package cz.tstrecha.timetracker.dto;

import cz.tstrecha.timetracker.repository.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class LoggedUser extends ContextUserDTO {

    private UserEntity userEntity;
}
