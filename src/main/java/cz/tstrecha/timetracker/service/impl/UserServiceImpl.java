package cz.tstrecha.timetracker.service.impl;

import cz.tstrecha.timetracker.config.JwtAuthenticationFilter;
import cz.tstrecha.timetracker.constant.AccountType;
import cz.tstrecha.timetracker.constant.UserRole;
import cz.tstrecha.timetracker.controller.exception.PermissionException;
import cz.tstrecha.timetracker.controller.exception.UserInputException;
import cz.tstrecha.timetracker.dto.LoggedUser;
import cz.tstrecha.timetracker.dto.LoginRequestDTO;
import cz.tstrecha.timetracker.dto.LoginResponseDTO;
import cz.tstrecha.timetracker.dto.RelationshipCreateUpdateRequestDTO;
import cz.tstrecha.timetracker.dto.RelationshipDTO;
import cz.tstrecha.timetracker.dto.UserContext;
import cz.tstrecha.timetracker.dto.UserDTO;
import cz.tstrecha.timetracker.dto.UserRegistrationRequestDTO;
import cz.tstrecha.timetracker.dto.mapper.RelationshipMapper;
import cz.tstrecha.timetracker.dto.mapper.UserMapper;
import cz.tstrecha.timetracker.repository.UserRelationshipRepository;
import cz.tstrecha.timetracker.repository.UserRepository;
import cz.tstrecha.timetracker.repository.UserSettingsRepository;
import cz.tstrecha.timetracker.repository.entity.UserRelationshipEntity;
import cz.tstrecha.timetracker.repository.entity.UserSettingsEntity;
import cz.tstrecha.timetracker.service.AuthenticationService;
import cz.tstrecha.timetracker.service.TransactionRunner;
import cz.tstrecha.timetracker.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserRelationshipRepository userRelationshipRepository;
    private final UserSettingsRepository userSettingsRepository;

    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final AuthenticationService authenticationService;

    private final UserMapper userMapper;
    private final RelationshipMapper relationshipMapper;

    private final TransactionRunner transactionRunner;

    @Override
    @Transactional
    public UserDTO createUser(UserRegistrationRequestDTO registrationRequest, UserRole role) {
        if (registrationRequest.getAccountType() == AccountType.PERSON) {
            if (Strings.isEmpty(registrationRequest.getFirstName()) || Strings.isEmpty(registrationRequest.getLastName())) {
                throw new UserInputException("Person has to have first name and last name filled in.");
            }
        } else if (registrationRequest.getAccountType() == AccountType.COMPANY) {
            if (Strings.isEmpty(registrationRequest.getCompanyName())) {
                throw new UserInputException("Company has to have company name filled in.");
            }
        }

        if (userRepository.existsByEmail(registrationRequest.getEmail())) {
            throw new UserInputException("User with this email already exists.");
        }
        if (IntStream.of(0, registrationRequest.getPassword().length() - 1).noneMatch(i -> Character.isDigit(registrationRequest.getPassword().charAt(i)))) {
            throw new UserInputException("Password should contain at least 1 digit.");
        }

        var passwordHashed = passwordEncoder.encode(registrationRequest.getPassword());
        var registeredUser = userMapper.fromRegistrationRequest(registrationRequest, passwordHashed, role);

        var ownRelation = new UserRelationshipEntity();
        ownRelation.setPermissions(List.of("*"));
        ownRelation.setFrom(registeredUser);
        ownRelation.setTo(registeredUser);
        ownRelation.setSecureValues(false);

        registeredUser.setUserRelationshipGiving(List.of(ownRelation));
        registeredUser.setUserRelationshipReceiving(List.of(ownRelation));

        var settings = new UserSettingsEntity();
        settings.setUser(registeredUser);
        registeredUser.setSettings(List.of(settings));

        var savedUser = userRepository.save(registeredUser);
        userRelationshipRepository.save(ownRelation);
        userSettingsRepository.save(settings);

        return userMapper.toDTO(savedUser);
    }

    @Override
    @Transactional
    public RelationshipDTO createRelationship(RelationshipCreateUpdateRequestDTO request) {
        var from = userRepository.findById(request.getFromId())
                .orElseThrow(() -> new EntityNotFoundException("User entity not found by from id [" + request.getFromId() + "]"));
        var to = userRepository.findById(request.getToId())
                .orElseThrow(() -> new EntityNotFoundException("User entity not found by to id [" + request.getToId() + "]"));

        if (userRelationshipRepository.existsByFromAndTo(from, to)) {
            throw new UserInputException("Relationship already exists");
        }
        var relation = relationshipMapper.fromRequest(request, from, to);
        relation = userRelationshipRepository.save(relation);
        return relationshipMapper.toDTOFromReceiving(relation);
    }

    @Override
    public RelationshipDTO createRelationship(RelationshipCreateUpdateRequestDTO request, LoggedUser loggedUser, UserContext userContext) {
        if (!Objects.equals(userContext.getId(), loggedUser.getId()) || !Objects.equals(userContext.getId(), request.getFromId())) {
            throw new PermissionException("You can only create relationship for yourself.");
        }
        return transactionRunner.runInNewTransaction(() -> createRelationship(request));
    }

    @Override
    @Transactional
    public RelationshipDTO updateRelationship(RelationshipCreateUpdateRequestDTO request, LoggedUser loggedUser, UserContext userContext) {
        if (!Objects.equals(userContext.getId(), loggedUser.getId())) {
            throw new PermissionException("You can only edit your relationships.");
        }
        if (!Objects.equals(userContext.getId(), userRelationshipRepository.findById(request.getId()).orElseThrow().getFrom().getId())) {
            throw new UserInputException("You cannot edit a relationship between other users");
        }

        var relation = userRelationshipRepository.findById(request.getId())
                .orElseThrow(() -> new EntityNotFoundException("Relationship entity not found by id [" + request.getId() + "]"));
        relationshipMapper.updateRelationship(request, relation);
        relation = userRelationshipRepository.save(relation);
        return relationshipMapper.toDTOFromReceiving(relation);
    }

    @Override
    @Transactional
    public LoginResponseDTO changeContext(Long id, UserContext userContext) {
        var contextUserDTO = userContext.getRelationshipsReceiving().stream()
                .filter(relation -> relation.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new PermissionException("User dont have permission to change context to id [" + id + "]"));

        var userEntity = userRepository.findById(userContext.getId()).orElseThrow();

        var token = authenticationService.generateToken(userEntity, contextUserDTO);
        var refreshToken = authenticationService.generateRefreshToken(userEntity.getId(), contextUserDTO.getId());
        return new LoginResponseDTO(true,
                JwtAuthenticationFilter.AUTHORIZATION_HEADER_BEARER_PREFIX + token,
                JwtAuthenticationFilter.AUTHORIZATION_HEADER_BEARER_PREFIX + refreshToken);
    }

    @Override
    public LoginResponseDTO loginUser(LoginRequestDTO loginRequest) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
        var user = userRepository.findByEmail(loginRequest.getEmail()).orElseThrow(() -> new UsernameNotFoundException("No user exists for email [" + loginRequest.getEmail() + "]"));
        var token = authenticationService.generateToken(user, null);
        var refreshToken = authenticationService.generateRefreshToken(user.getId(), user.getId());
        return new LoginResponseDTO(true,
                JwtAuthenticationFilter.AUTHORIZATION_HEADER_BEARER_PREFIX + token,
                JwtAuthenticationFilter.AUTHORIZATION_HEADER_BEARER_PREFIX + refreshToken);
    }
}
