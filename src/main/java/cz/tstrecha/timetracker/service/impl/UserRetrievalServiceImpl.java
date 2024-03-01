package cz.tstrecha.timetracker.service.impl;

import cz.tstrecha.timetracker.dto.UserContext;
import cz.tstrecha.timetracker.repository.UserRepository;
import cz.tstrecha.timetracker.repository.entity.UserEntity;
import cz.tstrecha.timetracker.service.UserRetrievalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserRetrievalServiceImpl implements UserRetrievalService {

    private final UserRepository userRepository;

    @Override
    public UserEntity getUserFromContext(UserContext userContext) {
        return findUserById(userContext.getCurrentUserId());
    }

    @Override
    public UserEntity getLoggedUserFromContext(UserContext userContext) {
        return findUserById(userContext.getId());
    }

    private UserEntity findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(STR."User was not found from a valid context with id [\{id}]"));
    }
}
