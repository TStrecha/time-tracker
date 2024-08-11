package cz.tstrecha.timetracker.service.impl;

import cz.tstrecha.timetracker.constant.ErrorTypeCode;
import cz.tstrecha.timetracker.constant.UserRole;
import cz.tstrecha.timetracker.controller.exception.UserInputException;
import cz.tstrecha.timetracker.dto.ContextUserDTO;
import cz.tstrecha.timetracker.dto.RelationshipCreateUpdateRequestDTO;
import cz.tstrecha.timetracker.dto.RelationshipDTO;
import cz.tstrecha.timetracker.dto.UserContext;
import cz.tstrecha.timetracker.dto.mapper.RelationshipMapper;
import cz.tstrecha.timetracker.dto.mapper.UserMapper;
import cz.tstrecha.timetracker.repository.UserRelationshipRepository;
import cz.tstrecha.timetracker.repository.UserRepository;
import cz.tstrecha.timetracker.service.RelationshipService;
import cz.tstrecha.timetracker.service.UserRetrievalService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RelationshipServiceImpl implements RelationshipService {

    private final UserRepository userRepository;
    private final UserRelationshipRepository userRelationshipRepository;

    private final UserRetrievalService userRetrievalService;

    private final RelationshipMapper relationshipMapper;
    private final UserMapper userMapper;

    @Override
    public List<ContextUserDTO> getActiveContexts(UserContext userContext) {
        if(userContext.getRole() == UserRole.ADMIN) {
            return userRepository.findAll().stream().map(userMapper::userToContextUserDTO).toList();
        }

        return userRetrievalService.getLoggedUserFromContext(userContext)
                .getActiveRelationshipsReceiving().stream()
                .map(userMapper::userRelationshipEntityToContextUserDTO)
                .toList();
    }

    @Override
    public List<RelationshipDTO> getAllRelationships(UserContext userContext) {
        return userRetrievalService.getLoggedUserFromContext(userContext)
                .getUserRelationshipGiving().stream()
                .map(relationshipMapper::toDTOFromReceiving)
                .toList();
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
