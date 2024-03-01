package cz.tstrecha.timetracker.integration;

import cz.tstrecha.timetracker.repository.entity.UserEntity;
import cz.tstrecha.timetracker.utils.IntegrationTest;
import cz.tstrecha.timetracker.config.JwtAuthenticationFilter;
import cz.tstrecha.timetracker.constant.AccountType;
import cz.tstrecha.timetracker.constant.ErrorTypeCode;
import cz.tstrecha.timetracker.constant.SecretMode;
import cz.tstrecha.timetracker.dto.LoginRequestDTO;
import cz.tstrecha.timetracker.dto.LoginResponseDTO;
import cz.tstrecha.timetracker.dto.UserContext;
import cz.tstrecha.timetracker.dto.UserRegistrationRequestDTO;
import cz.tstrecha.timetracker.dto.mapper.UserMapper;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Base64;
import java.util.List;

import static cz.tstrecha.timetracker.utils.RequestBuilder.buildRequest;
import static cz.tstrecha.timetracker.utils.assertions.UserInputExceptionHandler.handleUserInputException;

class AuthIT extends IntegrationTest {

    private final static String AUTH_API_BASE_PATH = "/auth";

    private final static String USER_EMAIL = "test@mail.com";
    private final static String USER_PASSWORD = "testcase0";
    private final static String USER_FIRST_NAME = "Test";
    private final static String USER_LAST_NAME = "Case";
    private final static String COMPANY_NAME = "Time Tracker s.r.o.";

    @Autowired
    private UserMapper userMapper;

    @Test
    @SneakyThrows
    @Transactional
    void should_CreateUser_When_ValidRequestGiven() {
        var userRegistrationRequest = createUserRegistrationRequest(AccountType.PERSON);

        var apiResult = buildRequest(HttpMethod.POST, STR."\{AUTH_API_BASE_PATH}/register")
                .withBody(userRegistrationRequest)
                .performWith(mvc)
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturn()
                .getResponse();

        var createdUserId = objectMapper.readValue(apiResult.getContentAsString(), Long.class);
        var user = userRepository.findById(createdUserId).orElse(null);

        assertUser(userRegistrationRequest, user);
    }

    @Test
    @SneakyThrows
    @Transactional
    void should_LoginUser_When_ValidCredentialsGiven() {
        var token = registerUserAndGetToken(createUserRegistrationRequest(AccountType.PERSON));

        var chunks = token.replace(JwtAuthenticationFilter.AUTHORIZATION_HEADER_BEARER_PREFIX, "").split("\\.");
        var decoder = Base64.getUrlDecoder();

        var header = new String(decoder.decode(chunks[0]));
        var payload = new String(decoder.decode(chunks[1]));

        var algorithmHeader = objectMapper.readValue(header, AlgorithmHeader.class);
        Assertions.assertEquals(SignatureAlgorithm.HS256, algorithmHeader.alg);

        var contextFromToken = objectMapper.readValue(payload, ContextWrapper.class).getUser();

        var userEntity = userRepository.findById(contextFromToken.getId()).orElse(null);

        Assertions.assertNotNull(userEntity);

        var ownRelationship = userEntity.getUserRelationshipReceiving().stream()
                .filter(r -> r.getFrom().getId().equals(contextFromToken.getId()))
                .findFirst()
                .orElseThrow();

        Assertions.assertEquals(userMapper.toContext(userEntity, userMapper.userRelationshipEntityToContextUserDTO(ownRelationship)), contextFromToken);
    }

    @Test
    @SneakyThrows
    @Transactional
    void should_BeUnauthorized_When_InvalidCredentialsGiven() {
        var userRegistrationRequest = createUserRegistrationRequest(AccountType.PERSON);

        buildRequest(HttpMethod.POST, STR."\{AUTH_API_BASE_PATH}/register")
                .withBody(userRegistrationRequest)
                .performWith(mvc)
                .andExpect(MockMvcResultMatchers.status().isCreated());

        var loginRequest = new LoginRequestDTO(USER_EMAIL, STR."\{USER_PASSWORD}fail");

        buildRequest(HttpMethod.POST, STR."\{AUTH_API_BASE_PATH}/login")
                .withBody(loginRequest)
                .performWith(mvc)
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    @SneakyThrows
    @Transactional
    void should_BeUnprocessableEntity_When_UserWithEmailAlreadyExists() {
        var userRegistrationRequest = createUserRegistrationRequest(AccountType.PERSON);

        buildRequest(HttpMethod.POST, STR."\{AUTH_API_BASE_PATH}/register")
                .withBody(userRegistrationRequest)
                .performWith(mvc)
                .andExpect(MockMvcResultMatchers.status().isCreated());

        buildRequest(HttpMethod.POST, STR."\{AUTH_API_BASE_PATH}/register")
                .withBody(userRegistrationRequest)
                .performWith(mvc)
                .andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
                .andDo(handleUserInputException(ErrorTypeCode.USER_EMAIL_EXISTS));
    }

    @Test
    @SneakyThrows
    @Transactional
    void should_BeUnprocessableEntity_When_FirstNameNotPresentForUserAccount() {
        var userRegistrationRequest = createUserRegistrationRequest(AccountType.PERSON);
        userRegistrationRequest.setFirstName(null);

        buildRequest(HttpMethod.POST, STR."\{AUTH_API_BASE_PATH}/register")
                .withBody(userRegistrationRequest)
                .performWith(mvc)
                .andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
                .andDo(handleUserInputException(ErrorTypeCode.PERSON_FIRST_LAST_NAME_MISSING));
    }

    @Test
    @SneakyThrows
    @Transactional
    void should_BeUnprocessableEntity_When_LastNameNotPresentForUserAccount() {
        var userRegistrationRequest = createUserRegistrationRequest(AccountType.PERSON);
        userRegistrationRequest.setLastName(null);

        buildRequest(HttpMethod.POST, STR."\{AUTH_API_BASE_PATH}/register")
                .withBody(userRegistrationRequest)
                .performWith(mvc)
                .andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
                .andDo(handleUserInputException(ErrorTypeCode.PERSON_FIRST_LAST_NAME_MISSING));
    }

    @Test
    @SneakyThrows
    @Transactional
    void should_BeUnprocessableEntity_When_PasswordDoesntContainDigit() {
        var userRegistrationRequest = createUserRegistrationRequest(AccountType.PERSON);
        userRegistrationRequest.setPassword("without_digit");

        buildRequest(HttpMethod.POST, STR."\{AUTH_API_BASE_PATH}/register")
                .withBody(userRegistrationRequest)
                .performWith(mvc)
                .andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
                .andDo(handleUserInputException(ErrorTypeCode.PASSWORD_DOES_NOT_CONTAIN_DIGIT));
    }

    @Test
    @SneakyThrows
    @Transactional
    void should_BeUnprocessableEntity_When_MissingCompanyNameForCompanyAccount() {
        var userRegistrationRequest = createUserRegistrationRequest(AccountType.COMPANY);

        buildRequest(HttpMethod.POST, STR."\{AUTH_API_BASE_PATH}/register")
                .withBody(userRegistrationRequest)
                .performWith(mvc)
                .andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
                .andDo(handleUserInputException(ErrorTypeCode.COMPANY_NAME_MISSING));
    }

    @Test
    @SneakyThrows
    @Transactional
    void should_RegisterUser_When_ValidCompanyAccountRequestGiven() {
        var userRegistrationRequest = createUserRegistrationRequest(AccountType.COMPANY);
        userRegistrationRequest.setCompanyName(COMPANY_NAME);
        userRegistrationRequest.setFirstName(null);
        userRegistrationRequest.setLastName(null);

        var apiResult = buildRequest(HttpMethod.POST, STR."\{AUTH_API_BASE_PATH}/register")
                .withBody(userRegistrationRequest)
                .performWith(mvc)
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturn()
                .getResponse();

        var createdUserId = objectMapper.readValue(apiResult.getContentAsString(), Long.class);
        var user = userRepository.findById(createdUserId).orElse(null);

        assertUser(userRegistrationRequest, user);
    }

    @Test
    @SneakyThrows
    void should_RefreshAccessTokenAndKeepContext_When_ValidRefreshTokenGiven(){
        var userRegistrationRequest = createUserRegistrationRequest(AccountType.PERSON);
        var registeredToken = registerUserAndGetToken(userRegistrationRequest).split("\\.")[1];

        var loginRequest = new LoginRequestDTO();
        loginRequest.setEmail(USER_EMAIL);
        loginRequest.setPassword(USER_PASSWORD);

        var loginApiResponse = buildRequest(HttpMethod.POST, STR."\{AUTH_API_BASE_PATH}/login")
                .withBody(loginRequest)
                .performWith(mvc)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse();

        var loginResponseDTO = objectMapper.readValue(loginApiResponse.getContentAsString(), LoginResponseDTO.class);

        var tokenRefreshResponse = buildRequest(HttpMethod.POST, STR."\{AUTH_API_BASE_PATH}/refresh")
                .withBody(loginResponseDTO.getRefreshToken())
                .performWith(mvc)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse();

        var decoder = Base64.getUrlDecoder();

        var token = tokenRefreshResponse.getContentAsString().split("\\.")[1];

        var expectedUserContext = objectMapper.readValue(new String(decoder.decode(registeredToken)), UserContext.class);
        var actualUserContext = objectMapper.readValue(new String(decoder.decode(token)), UserContext.class);

        Assertions.assertEquals(expectedUserContext, actualUserContext);
    }

    @Test
    @SneakyThrows
    void should_beUnauthorized_When_ExpiredRefreshTokenGiven() {
        var refreshToken = "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjUxOSwiYXV0aG9yaXplZEFzVXNlcklkIjo1NTIsImlhdCI6MTY3ODk5MDU2NCwiZXhwIjoxNjc5MDc2OTY0fQ.3_qWnyc9weSct8eEXn5u7fohYw6TV6SSkRXiqyE8K_A";

        buildRequest(HttpMethod.POST, STR."\{AUTH_API_BASE_PATH}/refresh")
                .withBody(refreshToken)
                .performWith(mvc)
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    @SneakyThrows
    void should_BeUnauthorized_When_InvalidRefreshTokenGiven() {
        var refreshToken = "000.abcdefghijklmnopqrstuvwxyz.signature";

        buildRequest(HttpMethod.POST, STR."\{AUTH_API_BASE_PATH}/refresh")
                .withBody(refreshToken)
                .performWith(mvc)
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    private UserRegistrationRequestDTO createUserRegistrationRequest(AccountType accountType){
        var request = new UserRegistrationRequestDTO();
        request.setEmail(USER_EMAIL);
        request.setPassword(USER_PASSWORD);
        request.setFirstName(USER_FIRST_NAME);
        request.setLastName(USER_LAST_NAME);
        request.setAccountType(accountType);
        return request;
    }

    private String registerUserAndGetToken(UserRegistrationRequestDTO registrationRequest) throws Exception {
        buildRequest(HttpMethod.POST, STR."\{AUTH_API_BASE_PATH}/register")
                .withBody(registrationRequest)
                .performWith(mvc)
                .andExpect(MockMvcResultMatchers.status().isCreated());

        var loginRequest = new LoginRequestDTO(USER_EMAIL, USER_PASSWORD);

        var loginResponse = buildRequest(HttpMethod.POST, STR."\{AUTH_API_BASE_PATH}/login")
                .withBody(loginRequest)
                .performWith(mvc)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse();

        var loginData = objectMapper.readValue(loginResponse.getContentAsString(), LoginResponseDTO.class);

        Assertions.assertTrue(loginData.isSuccess());
        Assertions.assertNotNull(loginData.getRefreshToken());
        Assertions.assertNotNull(loginData.getAuthToken());

        return loginData.getAuthToken();
    }

    private void assertUser(UserRegistrationRequestDTO userRegistrationRequest, UserEntity user) {
        // Basic properties

        Assertions.assertNotNull(user);
        Assertions.assertNotNull(user.getId());

        Assertions.assertNull(user.getModifiedAt());

        Assertions.assertEquals(SecretMode.NONE, user.getSecretMode());
        Assertions.assertEquals(userRegistrationRequest.getEmail(), user.getEmail());
        Assertions.assertEquals(userRegistrationRequest.getAccountType(), user.getAccountType());

        Assertions.assertEquals(userRegistrationRequest.getCompanyName(), user.getCompanyName());
        Assertions.assertEquals(userRegistrationRequest.getFirstName(), user.getFirstName());
        Assertions.assertEquals(userRegistrationRequest.getLastName(), user.getLastName());

        if(userRegistrationRequest.getAccountType() == AccountType.PERSON) {
            Assertions.assertEquals(STR."\{userRegistrationRequest.getFirstName()} \{userRegistrationRequest.getLastName()}", user.getDisplayName());
        } else if(userRegistrationRequest.getAccountType() == AccountType.COMPANY) {
            Assertions.assertEquals(userRegistrationRequest.getCompanyName(), user.getDisplayName());
        }

        // Relationships

        Assertions.assertEquals(1, user.getUserRelationshipReceiving().size());
        var relationshipReceiving = user.getUserRelationshipReceiving().getFirst();

        Assertions.assertEquals(user.getId(), relationshipReceiving.getFrom().getId());
        Assertions.assertEquals(List.of("*"), relationshipReceiving.getPermissions());
        Assertions.assertNotNull(relationshipReceiving.getActiveFrom());
        Assertions.assertNull(relationshipReceiving.getActiveTo());

        Assertions.assertEquals(1, user.getUserRelationshipGiving().size());
        var relationshipGiving = user.getUserRelationshipGiving().getFirst();

        Assertions.assertEquals(user.getId(), relationshipGiving.getTo().getId());
        Assertions.assertEquals(List.of("*"), relationshipGiving.getPermissions());
        Assertions.assertNotNull(relationshipGiving.getActiveFrom());
        Assertions.assertNull(relationshipGiving.getActiveTo());

        // Settings

        Assertions.assertEquals(1, user.getSettings().size());

        var settings = user.getSettings().getFirst();
        Assertions.assertNotNull(settings.getValidFrom());
        Assertions.assertNull(settings.getValidTo());
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class ContextWrapper {

        private UserContext user;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class AlgorithmHeader {

        private SignatureAlgorithm alg;
    }
}
