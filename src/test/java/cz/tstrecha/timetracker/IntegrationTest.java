package cz.tstrecha.timetracker;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.tstrecha.timetracker.repository.UserRelationshipRepository;
import cz.tstrecha.timetracker.repository.UserRepository;
import cz.tstrecha.timetracker.repository.UserSettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Arrays;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public abstract class IntegrationTest {

    private static final String TEST_PROFILE = "testprod";
    private static final String TEST_LOCAL_PROFILE = "testlocal";

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

    @Autowired
    private ConfigurableEnvironment env;

    @BeforeEach
    public void init(){
        cleanUpDB();
        if(!Arrays.asList(env.getActiveProfiles()).contains(TEST_PROFILE)){
            env.setActiveProfiles(TEST_LOCAL_PROFILE);
        }
    }

    protected void cleanUpDB() {
        transactionTemplate.executeWithoutResult(a -> {
            relationshipRepository.deleteAll();
            userSettingsRepository.deleteAll();
            userRepository.deleteAll();
        });
    }


}
