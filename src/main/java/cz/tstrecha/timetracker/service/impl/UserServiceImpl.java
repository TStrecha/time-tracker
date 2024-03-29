package cz.tstrecha.timetracker.service.impl;

import cz.tstrecha.timetracker.constant.AccountType;
import cz.tstrecha.timetracker.constant.ErrorTypeCode;
import cz.tstrecha.timetracker.constant.UserRole;
import cz.tstrecha.timetracker.controller.exception.UserInputException;
import cz.tstrecha.timetracker.dto.ContextUserDTO;
import cz.tstrecha.timetracker.dto.LoginRequestDTO;
import cz.tstrecha.timetracker.dto.LoginResponseDTO;
import cz.tstrecha.timetracker.dto.PasswordChangeDTO;
import cz.tstrecha.timetracker.dto.RelationshipCreateUpdateRequestDTO;
import cz.tstrecha.timetracker.dto.RelationshipDTO;
import cz.tstrecha.timetracker.dto.UserContext;
import cz.tstrecha.timetracker.dto.UserDTO;
import cz.tstrecha.timetracker.dto.UserRegistrationRequestDTO;
import cz.tstrecha.timetracker.dto.UserUpdateDTO;
import cz.tstrecha.timetracker.dto.mapper.RelationshipMapper;
import cz.tstrecha.timetracker.dto.mapper.UserMapper;
import cz.tstrecha.timetracker.repository.UserRelationshipRepository;
import cz.tstrecha.timetracker.repository.UserRepository;
import cz.tstrecha.timetracker.repository.UserSettingsRepository;
import cz.tstrecha.timetracker.repository.entity.UserEntity;
import cz.tstrecha.timetracker.repository.entity.UserRelationshipEntity;
import cz.tstrecha.timetracker.repository.entity.UserSettingsEntity;
import cz.tstrecha.timetracker.service.AuthenticationService;
import cz.tstrecha.timetracker.service.ContextService;
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

import static java.lang.StringTemplate.STR;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserRelationshipRepository userRelationshipRepository;
    private final UserSettingsRepository userSettingsRepository;

    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final AuthenticationService authenticationService;
    private final ContextService contextService;

    private final UserMapper userMapper;
    private final RelationshipMapper relationshipMapper;

    private final TransactionRunner transactionRunner;

    @Override
    @Transactional
    public UserDTO createUser(UserRegistrationRequestDTO registrationRequest, UserRole role) {
        validateNames(registrationRequest.getAccountType(),
                registrationRequest.getFirstName(),
                registrationRequest.getLastName(),
                registrationRequest.getCompanyName());

        if (userRepository.existsByEmail(registrationRequest.getEmail())) {
            throw new UserInputException("User with this email already exists.", ErrorTypeCode.USER_EMAIL_EXISTS, UserRegistrationRequestDTO.class);
        }

        validatePassword(registrationRequest.getPassword());

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
    public RelationshipDTO createRelationship(RelationshipCreateUpdateRequestDTO request, UserContext userContext) {
        var from = getUserFromContext(userContext);
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
        if (!Objects.equals(userContext.getId(), userRelationshipRepository.findById(request.getId()).orElseThrow().getFrom().getId())) {
            throw new UserInputException("You cannot edit a relationship between other users",
                    ErrorTypeCode.RELATIONSHIP_EDIT_WITHOUT_PERMISSION,
                    RelationshipCreateUpdateRequestDTO.class);
        }

        var relation = userRelationshipRepository.findById(request.getId())
                .orElseThrow(() -> new EntityNotFoundException(STR."Relationship entity not found by id [\{request.getId()}]"));
        relationshipMapper.updateRelationship(request, relation);
        relation = userRelationshipRepository.save(relation);
        return relationshipMapper.toDTOFromReceiving(relation);
    }

    @Override
    @Transactional
    public LoginResponseDTO changeContext(Long id, UserContext userContext) {
        var user = getLoggedUserFromContext(userContext);
        var newContext = contextService.getContextFromUser(user, id);

        var token = authenticationService.generateToken(user, newContext);
        var refreshToken = authenticationService.generateRefreshToken(user.getId(), newContext.getId());

        return new LoginResponseDTO(true, token, refreshToken);
    }

    @Override
    @Transactional
    public LoginResponseDTO changeUserDetails(UserUpdateDTO userUpdateDTO, UserContext userContext) {
        validateNames(userUpdateDTO.getAccountType(),
                userUpdateDTO.getFirstName(),
                userUpdateDTO.getLastName(),
                userUpdateDTO.getCompanyName());

        var user = userRepository.findByEmail(userContext.getEmail()).orElseThrow();
        userMapper.updateUser(userUpdateDTO, user);
        userRepository.save(user);

        return generateLoginResponseDTO(user, userContext.getLoggedAs());
    }

    @Override
    public LoginResponseDTO loginUser(LoginRequestDTO loginRequest) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
        var user = userRepository.findByEmail(loginRequest.getEmail()).orElseThrow(() -> new UsernameNotFoundException(STR."No user exists for email [\{loginRequest.getEmail()}]"));
        return generateLoginResponseDTO(user, null);
    }

    @Override
    @Transactional
    public LoginResponseDTO changePassword(PasswordChangeDTO passwordChangeDTO, UserContext userContext) {
        var user = authenticateAndRetrieveUser(userContext.getEmail(), passwordChangeDTO.getPassword());
        validatePassword(passwordChangeDTO.getPassword());

        var passwordHashed = passwordEncoder.encode(passwordChangeDTO.getNewPassword());
        user.setPasswordHashed(passwordHashed);
        userRepository.save(user);

        return generateLoginResponseDTO(user, userContext.getLoggedAs());
    }

    @Override
    public List<ContextUserDTO> getActiveRelationships(UserContext userContext) {
        if(userContext.getRole() == UserRole.ADMIN) {
            return userRepository.findAll().stream().map(userMapper::userToContextUserDTO).toList();
        }

        return getLoggedUserFromContext(userContext).getActiveRelationshipsReceiving().stream().map(userMapper::userRelationshipEntityToContextUserDTO).toList();
    }

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
                .orElseThrow(() -> new IllegalArgumentException(String.format("User was not found from a valid context with id [%s]", id)));
    }

    private LoginResponseDTO generateLoginResponseDTO(UserEntity user, ContextUserDTO contextUserDTO){
        var token = authenticationService.generateToken(user, contextUserDTO);
        var refreshToken = authenticationService.generateRefreshToken(user.getId(), contextUserDTO == null ? user.getId() : contextUserDTO.getId());
        return new LoginResponseDTO(true, token, refreshToken);
    }

    private void validateNames(AccountType accountType, String firstName, String lastName, String companyName){
        if (accountType == AccountType.PERSON) {
            if (Strings.isEmpty(firstName) || Strings.isEmpty(lastName)) {
                throw new UserInputException("Person has to have first name and last name filled in.",
                        ErrorTypeCode.PERSON_FIRST_LAST_NAME_MISSING,
                        UserUpdateDTO.class);
            }
        } else if(accountType == AccountType.COMPANY && (Strings.isEmpty(companyName))){
                throw new UserInputException("Company has to have company name filled in.",
                        ErrorTypeCode.COMPANY_NAME_MISSING,
                        UserUpdateDTO.class);
        }
    }

    private void validatePassword(String password){
        if (IntStream.of(0, password.length() - 1).noneMatch(i -> Character.isDigit(password.charAt(i)))) {
            throw new UserInputException("Password should contain at least 1 digit.", ErrorTypeCode.PASSWORD_DOES_NOT_CONTAIN_DIGIT, PasswordChangeDTO.class);
        }
    }

    private UserEntity authenticateAndRetrieveUser(String email, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(STR."No user exists for email [\{email}]"));
    }
}
