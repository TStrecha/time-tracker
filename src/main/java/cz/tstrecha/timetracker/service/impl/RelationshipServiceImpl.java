package cz.tstrecha.timetracker.service.impl;

import cz.tstrecha.timetracker.constant.ErrorTypeCode;
import cz.tstrecha.timetracker.constant.UserRole;
import cz.tstrecha.timetracker.controller.exception.UserInputException;
import cz.tstrecha.timetracker.dto.ContextUserDTO;
import cz.tstrecha.timetracker.dto.LoginResponseDTO;
import cz.tstrecha.timetracker.dto.RelationshipCreateUpdateRequestDTO;
import cz.tstrecha.timetracker.dto.RelationshipDTO;
import cz.tstrecha.timetracker.dto.UserContext;
import cz.tstrecha.timetracker.dto.mapper.RelationshipMapper;
import cz.tstrecha.timetracker.dto.mapper.UserMapper;
import cz.tstrecha.timetracker.repository.UserRelationshipRepository;
import cz.tstrecha.timetracker.repository.UserRepository;
import cz.tstrecha.timetracker.service.AuthenticationService;
import cz.tstrecha.timetracker.service.ContextService;
import cz.tstrecha.timetracker.service.RelationshipService;
import cz.tstrecha.timetracker.service.UserRetrievalService;
import cz.tstrecha.timetracker.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class RelationshipServiceImpl implements RelationshipService {

    private final UserRepository userRepository;
    private final UserRelationshipRepository userRelationshipRepository;

    private final UserRetrievalService userRetrievalService;

    private final RelationshipMapper relationshipMapper;
    private final UserMapper userMapper;
    private final ContextService contextService;
    private final AuthenticationService authenticationService;

    @Override
    public List<ContextUserDTO> getActiveContexts(UserContext userContext) {
        if(userContext.getRole() == UserRole.ADMIN) {
            return userRepository.findAll().stream().map(userMapper::userToContextUserDTO).toList();
        }

        return userRetrievalService.getLoggedUserFromContext(userContext).getActiveRelationshipsReceiving().stream().map(userMapper::userRelationshipEntityToContextUserDTO).toList();
    }

    @Override
    @Transactional
    public LoginResponseDTO changeContext(Long id, UserContext userContext) {
        var user = userRetrievalService.getLoggedUserFromContext(userContext);
        var newContext = contextService.getContextFromUser(user, id);

        var token = authenticationService.generateToken(user, newContext);
        var refreshToken = authenticationService.generateRefreshToken(user.getId(), newContext.getId());

        return new LoginResponseDTO(true, token, refreshToken);
    }

    @Override
    @Transactional
    public RelationshipDTO createRelationship(RelationshipCreateUpdateRequestDTO request, UserContext userContext) {
        var from = userRetrievalService.getUserFromContext(userContext);
        var to = userRepository.findById(request.getToId())
                .orElseThrow(() -> new EntityNotFoundException(STR."User entity not found by to id [\{request.getToId()}]"));

        if (userRelationshipRepository.existsByFromAndTo(from, to)) {
            throw new UserInputException("Relationship already exists", ErrorTypeCode.RELATIONSHIP_ALREADY_EXISTS, RelationshipCreateUpdateRequestDTO.class);
        }

        var relation = relationshipMapper.fromRequest(request, from, to);
        relation = userRelationshipRepository.save(relation);
        return relationshipMapper.toDTOFromReceiving(relation);
    }

    @Override
    @Transactional
    public RelationshipDTO updateRelationship(RelationshipCreateUpdateRequestDTO request, UserContext userContext) {
        var relation = userRelationshipRepository.findById(request.getId())
                .orElseThrow(() -> new EntityNotFoundException(STR."Relationship entity not found by id [\{request.getId()}]"));

        relationshipMapper.updateRelationship(request, relation);
        relation = userRelationshipRepository.save(relation);

        return relationshipMapper.toDTOFromReceiving(relation);
    }
}
