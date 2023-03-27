package cz.tstrecha.timetracker.service.impl;

import cz.tstrecha.timetracker.config.JwtAuthenticationFilter;
import cz.tstrecha.timetracker.constant.AccountType;
import cz.tstrecha.timetracker.constant.ErrorTypeCode;
import cz.tstrecha.timetracker.constant.UserRole;
import cz.tstrecha.timetracker.controller.exception.PermissionException;
import cz.tstrecha.timetracker.controller.exception.UserInputException;
import cz.tstrecha.timetracker.dto.*;
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

import java.time.OffsetDateTime;
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
                throw new UserInputException("Person has to have first name and last name filled in.",
                        ErrorTypeCode.PERSON_FIRST_LAST_NAME_MISSING,
                        "UserRegistrationRequestDTO");
            }
        } else if (registrationRequest.getAccountType() == AccountType.COMPANY) {
            if (Strings.isEmpty(registrationRequest.getCompanyName())) {
                throw new UserInputException("Company has to have company name filled in.", ErrorTypeCode.COMPANY_NAME_MISSING, "UserRegistrationRequestDTO");
            }
        }

        if (userRepository.existsByEmail(registrationRequest.getEmail())) {
            throw new UserInputException("User with this email already exists.", ErrorTypeCode.USER_EMAIL_EXISTS, "UserRegistrationRequestDTO");
        }
        if (IntStream.of(0, registrationRequest.getPassword().length() - 1).noneMatch(i -> Character.isDigit(registrationRequest.getPassword().charAt(i)))) {
            throw new UserInputException("Password should contain at least 1 digit.", ErrorTypeCode.PASSWORD_DOESNT_CONTAIN_DIGIT, "UserRegistrationRequestDTO");
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
            throw new UserInputException("Relationship already exists", ErrorTypeCode.RELATIONSHIP_ALREADY_EXISTS, "RelationshipCreateUpdateRequestDTO");
        }
        var relation = relationshipMapper.fromRequest(request, from, to);
        relation = userRelationshipRepository.save(relation);
        return relationshipMapper.toDTOFromReceiving(relation);
    }

    @Override
    public RelationshipDTO createRelationship(RelationshipCreateUpdateRequestDTO request, LoggedUser loggedUser, UserContext userContext) {
        if (!Objects.equals(userContext.getId(), loggedUser.getId()) || !Objects.equals(userContext.getId(), request.getFromId())) {
            throw new PermissionException("You can only create relationship for yourself.", ErrorTypeCode.USER_ATTEMPTS_TO_CREATE_RELATIONSHIP_FOR_SOMEONE_ELSE, "RelationshipCreateUpdateRequestDTO");
        }
        return transactionRunner.runInNewTransaction(() -> createRelationship(request));
    }

    @Override
    @Transactional
    public RelationshipDTO updateRelationship(RelationshipCreateUpdateRequestDTO request, LoggedUser loggedUser, UserContext userContext) {
        if (!Objects.equals(userContext.getId(), loggedUser.getId())) {
            throw new PermissionException("You can only edit your relationships.",ErrorTypeCode.USER_ATTEMPTS_TO_UPDATE_RELATIONSHIP_FOR_SOMEONE_ELSE, "RelationshipCreateUpdateRequestDTO");
        }
        if (!Objects.equals(userContext.getId(), userRelationshipRepository.findById(request.getId()).orElseThrow().getFrom().getId())) {
            throw new UserInputException("You cannot edit a relationship between other users",
                    ErrorTypeCode.REALTIONSHIP_EDIT_WITHOUT_PERMISSION,
                    "RelationshipCreateUpdateRequestDTO");
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
                .filter(relation -> relation.getActiveFrom().isBefore(OffsetDateTime.now()) &&
                        (relation.getActiveTo() == null || relation.getActiveTo().isAfter(OffsetDateTime.now())))
                .findFirst()
                .orElseThrow(() -> new PermissionException("User dont have permission to change context to id [" + id + "]", ErrorTypeCode.USER_DOESNT_HAVE_PERMISSION_TO_CHANGE_CONTEXT));

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
        return new LoginResponseDTO(true, token, refreshToken);
    }

    @Override
    @Transactional
    public LoginResponseDTO changePassword(PasswordChangeDTO passwordChangeDTO, UserContext userContext) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userContext.getEmail(), passwordChangeDTO.getPassword()));
        var user = userRepository.findByEmail(userContext.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("No user exists for email [" + userContext.getEmail() + "]"));
        if (IntStream.of(0, passwordChangeDTO.getNewPassword().length() - 1).noneMatch(i -> Character.isDigit(passwordChangeDTO.getNewPassword().charAt(i)))) {
            throw new UserInputException("Password should contain at least 1 digit.", ErrorTypeCode.PASSWORD_DOESNT_CONTAIN_DIGIT, "PasswordChangeDTO");
        }
        var passwordHashed = passwordEncoder.encode(passwordChangeDTO.getNewPassword());
        user.setPasswordHashed(passwordHashed);
        userRepository.save(user);

        var token = authenticationService.generateToken(user, null);
        var refreshToken = authenticationService.generateRefreshToken(user.getId(), user.getId());
        return new LoginResponseDTO(true, token, refreshToken);
    }


}
