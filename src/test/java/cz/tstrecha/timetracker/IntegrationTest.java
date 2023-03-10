package cz.tstrecha.timetracker;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.tstrecha.timetracker.repository.UserRelationshipRepository;
import cz.tstrecha.timetracker.repository.UserRepository;
import cz.tstrecha.timetracker.repository.UserSettingsRepository;
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
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@ActiveProfiles("test")
public abstract class IntegrationTest {

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected TransactionTemplate transactionTemplate;

    @Autowired
    protected UserRelationshipRepository relationshipRepository;
    @Autowired
    protected UserSettingsRepository userSettingsRepository;
    @Autowired
    protected UserRepository userRepository;

    @BeforeEach
    public void init(){
        cleanUpDB();
    }

    protected void cleanUpDB() {
        transactionTemplate.executeWithoutResult(a -> {
            relationshipRepository.deleteAll();
            userSettingsRepository.deleteAll();
            userRepository.deleteAll();
        });
    }


}
