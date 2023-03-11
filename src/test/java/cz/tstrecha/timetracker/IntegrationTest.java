package cz.tstrecha.timetracker;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.tstrecha.timetracker.constant.AccountType;
import cz.tstrecha.timetracker.constant.UserRole;
import cz.tstrecha.timetracker.dto.UserRegistrationRequestDTO;
import cz.tstrecha.timetracker.repository.TaskRepository;
import cz.tstrecha.timetracker.repository.UserRelationshipRepository;
import cz.tstrecha.timetracker.repository.UserRepository;
import cz.tstrecha.timetracker.repository.UserSettingsRepository;
import cz.tstrecha.timetracker.service.TransactionRunner;
import cz.tstrecha.timetracker.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@ActiveProfiles("test")
public abstract class IntegrationTest {

    private final static String USER_EMAIL = "test@case.";
    private final static String USER_PASSWORD = "testcase";
    private final static String USER_FIRST_NAME = "Test";
    private final static String USER_LAST_NAME = "Case";

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected ObjectMapper objectMapper;

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

    @BeforeEach
    public void init(){
        cleanUpDB();
    }

    public List<Long> mockUsers(int amount){
        List<Long> output = new ArrayList<>();
        for (int i = 1; i <= amount; i++) {
            var request = createUserRequest(USER_EMAIL + i, USER_PASSWORD + i, USER_FIRST_NAME + i, USER_LAST_NAME + i);
            transactionRunner.runInNewTransaction(() -> {
                output.add(userService.createUser(request, UserRole.USER).getId());

            });
        }
        return output;
    }

    protected void cleanUpDB() {
        transactionRunner.runInNewTransaction(() -> {
            taskRepository.deleteAll();
            relationshipRepository.deleteAll();
            userSettingsRepository.deleteAll();
            userRepository.deleteAll();
        });
    }

    private UserRegistrationRequestDTO createUserRequest(String email,
                                                         String password,
                                                         String firstName,
                                                         String lastName){
        var request = new UserRegistrationRequestDTO();
        request.setEmail(email);
        request.setPassword(password);
        request.setFirstName(firstName);
        request.setLastName(lastName);
        request.setAccountType(AccountType.PERSON);
        return request;
    }
}
