package cz.tstrecha.timetracker.service.impl;

import cz.tstrecha.timetracker.constant.ErrorTypeCode;
import cz.tstrecha.timetracker.constant.UserRole;
import cz.tstrecha.timetracker.controller.exception.PermissionException;
import cz.tstrecha.timetracker.controller.exception.UserInputException;
import cz.tstrecha.timetracker.dto.ContextUserDTO;
import cz.tstrecha.timetracker.dto.mapper.UserMapper;
import cz.tstrecha.timetracker.repository.UserRepository;
import cz.tstrecha.timetracker.repository.entity.UserEntity;
import cz.tstrecha.timetracker.service.ContextService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContextServiceImpl implements ContextService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public ContextUserDTO getContextFromUser(UserEntity user, Long authorizedAs) {
        if(user.getRole() == UserRole.ADMIN) {
            return userRepository.findById(authorizedAs).map(userMapper::userToContextUserDTO)
                    .orElseThrow(() -> new UserInputException("User not found by id.", ErrorTypeCode.USER_NOT_FOUND_BY_ID));
        } else {
            var relationship = user.getActiveRelationshipsReceiving().stream()
                    .filter(relation -> relation.getFrom().getId().equals(authorizedAs))
                    .findFirst()
                    .orElseThrow(() -> new PermissionException(STR."User doesn't have permission to change context to id [\{authorizedAs}]", ErrorTypeCode.USER_DOES_NOT_HAVE_PERMISSION_TO_CHANGE_CONTEXT));

            return userMapper.userRelationshipEntityToContextUserDTO(relationship);
        }
    }
}
