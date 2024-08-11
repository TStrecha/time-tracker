package cz.tstrecha.timetracker.service;

import cz.tstrecha.timetracker.dto.ContextUserDTO;
import cz.tstrecha.timetracker.repository.entity.UserEntity;

public interface ContextService {

    ContextUserDTO getContextFromUser(UserEntity user, Long authorizedAs);
}
