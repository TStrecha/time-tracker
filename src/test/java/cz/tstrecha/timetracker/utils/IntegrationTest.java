package cz.tstrecha.timetracker.utils;

import cz.tstrecha.timetracker.constant.AccountType;
import cz.tstrecha.timetracker.constant.UserRole;
import cz.tstrecha.timetracker.dto.UserContext;
import cz.tstrecha.timetracker.dto.UserRegistrationRequestDTO;
import cz.tstrecha.timetracker.dto.mapper.UserMapper;
import cz.tstrecha.timetracker.repository.TaskRepository;
import cz.tstrecha.timetracker.repository.UserRelationshipRepository;
import cz.tstrecha.timetracker.repository.UserRepository;
import cz.tstrecha.timetracker.repository.UserSettingsRepository;
import cz.tstrecha.timetracker.repository.entity.UserEntity;
import cz.tstrecha.timetracker.repository.entity.UserRelationshipEntity;
import cz.tstrecha.timetracker.service.AuthenticationService;
import cz.tstrecha.timetracker.service.TransactionRunner;
import cz.tstrecha.timetracker.service.UserService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.OffsetDateTime;
import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public abstract class IntegrationTest {

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected UserMapper userMapper;

    @Autowired
    protected TransactionTemplate transactionTemplate;

    @Autowired
    protected TransactionRunner transactionRunner;

    @Autowired
    protected UserRelationshipRepository relationshipRepository;
    @Autowired
    protected UserSettingsRepository userSettingsRepository;
    @Autowired
    protected UserRepository userRepository;
    @Autowired
    protected TaskRepository taskRepository;

    @Autowired
    protected UserService userService;

    @Autowired
    protected AuthenticationService authenticationService;

    private static UserEntity primaryUser;
    private static UserEntity secondaryUser;
    private static UserEntity tertiaryUser;
    private static UserEntity adminUser;

    @BeforeEach
    public void init(){
        primaryUser = createUser(createUserRequest("primaryUser@mail.com", "Password1", "Primary", "User"), UserRole.USER);
        secondaryUser = createUser(createUserRequest("secondaryUser@mail.com", "Password2", "Secondary", "User"), UserRole.USER);
        tertiaryUser = createUser(createUserRequest("tertiaryUser@mail.com", "Password3", "Tertiary", "User"), UserRole.USER);
        adminUser = createUser(createUserRequest("adminUser@mail.com", "AdminPassword1", "Administrator", "User"), UserRole.ADMIN);
    }

    public UserAuthorizationContextHolder ofPrimaryUser() {
        return of(primaryUser());
    }

    public UserAuthorizationContextHolder of(UserEntity user) {
        return authorizationOf(user);
    }

    public UserAuthorizationContextHolder authorizationOf(UserEntity user) {
        return ofUserLoggedAs(user, null);
    }

    private UserAuthorizationContextHolder ofUserLoggedAs(UserEntity user, UserEntity loggedAs) {
        var loggedAsContextUser = userMapper.userToContextUserDTO(loggedAs);

        var token = authenticationService.generateToken(user, loggedAsContextUser);
        var context = authenticationService.extractClaims(token).map(authenticationService::getUserContext)
                .orElseThrow(() -> new IllegalArgumentException("Token extraction failed."));

        return new UserAuthorizationContextHolder(this, user, context, token);
    }

    public UserEntity createUser(UserRegistrationRequestDTO request, UserRole role) {
        var user = userService.createUser(request, role);

        return userRepository.findById(user.getId()).orElse(null);
    }

    public void createRelationshipBetweenUsers(UserEntity from, UserEntity to) {
        if(from == to) {
            return;
        }

        var relationship = new UserRelationshipEntity(null, from, to, List.of("*"),
                OffsetDateTime.now().minusDays(1), OffsetDateTime.now().plusDays(1), false);

        relationship = relationshipRepository.save(relationship);

        from.getUserRelationshipGiving().add(relationship);
        to.getUserRelationshipReceiving().add(relationship);

        userRepository.save(from);
        userRepository.save(to);
    }

    private UserRegistrationRequestDTO createUserRequest(String email, String password, String firstName, String lastName){
        var request = new UserRegistrationRequestDTO();
        request.setEmail(email);
        request.setPassword(password);
        request.setFirstName(firstName);
        request.setLastName(lastName);
        request.setAccountType(AccountType.PERSON);
        return request;
    }

    public static UserEntity primaryUser() {
        return primaryUser;
    }

    public static UserEntity secondaryUser() {
        return secondaryUser;
    }

    public static UserEntity tertiaryUser() {
        return tertiaryUser;
    }

    public static UserEntity adminUser() {
        return adminUser;
    }


    @AllArgsConstructor
    public static final class UserAuthorizationContextHolder {

        private final IntegrationTest integrationTestRef;

        private final UserEntity currentUser;

        @Getter
        private final UserContext context;

        @Getter
        private final String token;

        public UserAuthorizationContextHolder loggedAs(UserEntity loggedAs) {
            if(loggedAs == null) {
                return this;
            }

            integrationTestRef.createRelationshipBetweenUsers(loggedAs, this.currentUser);

            return integrationTestRef.ofUserLoggedAs(this.currentUser, loggedAs);
        }
    }
}
