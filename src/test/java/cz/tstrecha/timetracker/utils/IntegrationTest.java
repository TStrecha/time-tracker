package cz.tstrecha.timetracker.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import cz.tstrecha.timetracker.service.AuthenticationService;
import cz.tstrecha.timetracker.service.TransactionRunner;
import cz.tstrecha.timetracker.service.UserService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public abstract class IntegrationTest {

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected ObjectMapper objectMapper;

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

    protected static UserEntity primaryUser;
    protected static UserEntity secondaryUser;
    protected static UserEntity tertiaryUser;
    protected static UserEntity adminUser;

    @BeforeEach
    public void init(){
        primaryUser = createUser(createUserRequest("primaryUser@mail.com", "Password1", "Primary", "User"), UserRole.USER);
        secondaryUser = createUser(createUserRequest("secondaryUser@mail.com", "Password2", "Secondary", "User"), UserRole.USER);
        tertiaryUser = createUser(createUserRequest("tertiaryUser@mail.com", "Password3", "Tertiary", "User"), UserRole.USER);
        adminUser = createUser(createUserRequest("adminUser@mail.com", "AdminPassword1", "Administrator", "User"), UserRole.ADMIN);
    }

    public UserEntity createUser(UserRegistrationRequestDTO request, UserRole role) {
        var user = userService.createUser(request, role);

        return userRepository.findById(user.getId()).orElse(null);
    }

    public TokenHolder getPrimaryUserAuthorizationToken() {
        return getAuthorizationToken(primaryUser, null);
    }

    public TokenHolder getAuthorizationToken(UserEntity user) {
        return getAuthorizationToken(user, null);
    }

    public TokenHolder getAuthorizationToken(UserEntity user, UserEntity loggedAs) {
        var loggedAsContextUser = userMapper.userToContextUserDTO(loggedAs);

        var token = authenticationService.generateToken(user, loggedAsContextUser);
        var context = authenticationService.extractClaims(token).map(authenticationService::getUserContext)
                .orElseThrow(() -> new IllegalArgumentException("Token extraction failed."));

        return new TokenHolder(context, token);
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

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static final class TokenHolder {

        private UserContext context;
        private String token;
    }
}
