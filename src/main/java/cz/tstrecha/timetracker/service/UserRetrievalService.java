package cz.tstrecha.timetracker.service;

import cz.tstrecha.timetracker.dto.UserContext;
import cz.tstrecha.timetracker.repository.entity.UserEntity;

public interface UserRetrievalService {

    /**
     * @param userContext
     * @return
     */
    UserEntity getUserFromContext(UserContext userContext);

    /**
     *
     * @param userContext
     * @return
     */
    UserEntity getLoggedUserFromContext(UserContext userContext);
}
