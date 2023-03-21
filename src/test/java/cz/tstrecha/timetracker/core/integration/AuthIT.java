package cz.tstrecha.timetracker.core.integration;

import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationFeature;
import cz.tstrecha.timetracker.IntegrationTest;
import cz.tstrecha.timetracker.config.JwtAuthenticationFilter;
import cz.tstrecha.timetracker.constant.AccountType;
import cz.tstrecha.timetracker.constant.Constants;
import cz.tstrecha.timetracker.constant.SecretMode;
import cz.tstrecha.timetracker.constant.UserRole;
import cz.tstrecha.timetracker.controller.exception.UserInputException;
import cz.tstrecha.timetracker.dto.LoginRequestDTO;
import cz.tstrecha.timetracker.dto.LoginResponseDTO;
import cz.tstrecha.timetracker.dto.UserContext;
import cz.tstrecha.timetracker.dto.UserRegistrationRequestDTO;
import cz.tstrecha.timetracker.dto.mapper.UserMapper;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.ServletException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Base64;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class AuthIT extends IntegrationTest {

    private final static String USER_EMAIL = "test@case.0";
    private final static String USER_PASSWORD = "testcase0";
    private final static String USER_FIRST_NAME = "Test";
    private final static String USER_LAST_NAME = "Case0";
    private final static String COMPANY_NAME = "Time Tracker s.r.o.";

    @Autowired
    private UserMapper userMapper;

    @Test
    @SneakyThrows
    @Transactional
    public void test01_createUser_success() {
        var request = createUserRequest();
        var apiResult = mvc.perform(
                        post(Constants.V1_CONTROLLER_ROOT + "auth/register")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturn()
                .getResponse();

        var user = userMapper.toDTO(userRepository.findById(Long.valueOf(apiResult.getContentAsString())).orElseThrow());

        Assertions.assertEquals(request.getAccountType(), user.getAccountType());
        Assertions.assertEquals(request.getFirstName(), user.getFirstName());
        Assertions.assertEquals(request.getLastName(), user.getLastName());
        Assertions.assertEquals(request.getEmail(), user.getEmail());
        Assertions.assertEquals(SecretMode.NONE, user.getSecretMode());
        Assertions.assertEquals(String.format("%s %s", request.getFirstName(), request.getLastName()), user.getDisplayName());
        Assertions.assertNotNull(user.getId());
        Assertions.assertNull(user.getModifiedAt());
        Assertions.assertNull(user.getCompanyName());

        Assertions.assertEquals(1, user.getRelationsReceiving().size());
        Assertions.assertEquals(user.getId(), user.getRelationsReceiving().get(0).getOppositeUserId());
        Assertions.assertEquals(user.getDisplayName(), user.getRelationsReceiving().get(0).getDisplayName());
        Assertions.assertEquals(List.of("*"), user.getRelationsReceiving().get(0).getPermissions());
        Assertions.assertNotNull(user.getRelationsReceiving().get(0).getActiveFrom());
        Assertions.assertNull(user.getRelationsReceiving().get(0).getActiveTo());

        Assertions.assertEquals(1, user.getRelationsGiving().size());
        Assertions.assertEquals(user.getId(), user.getRelationsGiving().get(0).getOppositeUserId());
        Assertions.assertEquals(user.getDisplayName(), user.getRelationsGiving().get(0).getDisplayName());
        Assertions.assertEquals(List.of("*"), user.getRelationsGiving().get(0).getPermissions());
        Assertions.assertNotNull(user.getRelationsGiving().get(0).getActiveFrom());
        Assertions.assertNull(user.getRelationsGiving().get(0).getActiveTo());

        var settings = userRepository.findById(user.getId()).get().getSettings();
        Assertions.assertEquals(1, settings.size());
        Assertions.assertNotNull(settings.get(0).getValidFrom());
        Assertions.assertNull(settings.get(0).getValidTo());
    }
    @Test
    @SneakyThrows
    @Transactional
    public void test02_loginUser_failure() {
        var registrationRequest = createUserRequest();
        mvc.perform(post(Constants.V1_CONTROLLER_ROOT + "auth/register")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturn()
                .getResponse();

        var loginRequest = new LoginRequestDTO();
        loginRequest.setEmail(USER_EMAIL);
        loginRequest.setPassword(USER_PASSWORD + "fail");
        mvc.perform(post(Constants.V1_CONTROLLER_ROOT + "auth/login")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andReturn()
                .getResponse();
    }

    @Test
    @SneakyThrows
    @Transactional
    public void test03_loginUser_success() {
        var token = registerUserAndGetToken(createUserRequest());
        String[] chunks = token.replace(JwtAuthenticationFilter.AUTHORIZATION_HEADER_BEARER_PREFIX, "").split("\\.");
        Base64.Decoder decoder = Base64.getUrlDecoder();

        String header = new String(decoder.decode(chunks[0]));
        String payload = new String(decoder.decode(chunks[1]));

        var algorithmHeader = objectMapper.readValue(header, AlgorithmHeader.class);
        Assertions.assertEquals(SignatureAlgorithm.HS256, algorithmHeader.alg);

        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        var contextFromToken = objectMapper.readValue(payload, ContextWrapper.class).getUser();
        var userEntity = userRepository.findById(contextFromToken.getId()).get();
        var ownRelationship = userEntity.getUserRelationshipReceiving().stream()
                .filter(r -> r.getFrom().getId().equals(contextFromToken.getId()))
                .findFirst()
                .orElseThrow();
        Assertions.assertEquals(userMapper.toContext(userEntity, userMapper.userRelationshipEntityToContextUserDTO(ownRelationship)), contextFromToken);
    }

    @Test
    @SneakyThrows
    @Transactional
    public void test04_getLoggedUserDetails_failure() {
        mvc.perform(get(Constants.V1_CONTROLLER_ROOT + "user")
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andReturn()
                .getResponse();
    }

    @Test
    @SneakyThrows
    @Transactional
    public void test05_getLoggedUserDetails_success() {
        var token = registerUserAndGetToken(createUserRequest());

        var response = mvc.perform(get(Constants.V1_CONTROLLER_ROOT + "user")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .header(JwtAuthenticationFilter.AUTHORIZATION_HEADER_NAME, JwtAuthenticationFilter.AUTHORIZATION_HEADER_BEARER_PREFIX + token))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse();

        var responseUser = objectMapper.readValue(response.getContentAsString(), UserContext.class);
        Assertions.assertNotNull(responseUser);
        Assertions.assertNotNull(responseUser.getId());
        Assertions.assertEquals(responseUser.getId(), responseUser.getLoggedAs().getId());
    }

    @Test
    @SneakyThrows
    @Transactional
    public void test06_registerUser_fail_emailAlreadyExists() {
        var request = createUserRequest();
        userService.createUser(request, UserRole.USER);

        var exception = Assertions.assertThrows(UserInputException.class, () -> userService.createUser(request, UserRole.USER));
        Assertions.assertEquals("User with this email already exists.", exception.getMessage());
    }

    @Test
    @SneakyThrows
    @Transactional
    public void test07_registerUser_fail_companyNameNotPresentForCompanyAccount() {
        var request = createUserRequest();
        request.setAccountType(AccountType.COMPANY);

        var exception = Assertions.assertThrows(UserInputException.class, () -> userService.createUser(request, UserRole.USER));
        Assertions.assertEquals("Company has to have company name filled in.", exception.getMessage());
    }

    @Test
    @SneakyThrows
    @Transactional
    public void test08_registerUser_fail_namesNotPresentForUserAccount() {
        var request = createUserRequest();
        request.setFirstName(null);
        request.setLastName(null);

        var exception = Assertions.assertThrows(UserInputException.class, () -> userService.createUser(request, UserRole.USER));
        Assertions.assertEquals("Person has to have first name and last name filled in.", exception.getMessage());

        request.setFirstName("Test");
        exception = Assertions.assertThrows(UserInputException.class, () -> userService.createUser(request, UserRole.USER));
        Assertions.assertEquals("Person has to have first name and last name filled in.", exception.getMessage());

        request.setFirstName(null);
        request.setLastName("");
        exception = Assertions.assertThrows(UserInputException.class, () -> userService.createUser(request, UserRole.USER));
        Assertions.assertEquals("Person has to have first name and last name filled in.", exception.getMessage());
    }
    @Test
    @SneakyThrows
    @Transactional
    public void test09_registerUser_fail_passwordDoesntContainDigit() {
        var request = createUserRequest();
        request.setPassword("test");

        var exception = Assertions.assertThrows(UserInputException.class, () -> userService.createUser(request, UserRole.USER));
        Assertions.assertEquals("Password should contain at least 1 digit.", exception.getMessage());
    }

    @Test
    @SneakyThrows
    @Transactional
    public void test10_registerUser_success_companyAccount() {
        var request = createUserRequest();
        request.setAccountType(AccountType.COMPANY);
        request.setCompanyName(COMPANY_NAME);
        request.setLastName(null);
        request.setFirstName(null);

        var token = registerUserAndGetToken(request);
        var response = mvc.perform(get(Constants.V1_CONTROLLER_ROOT + "user")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .header(JwtAuthenticationFilter.AUTHORIZATION_HEADER_NAME, JwtAuthenticationFilter.AUTHORIZATION_HEADER_BEARER_PREFIX + token))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse();
        var responseUser = objectMapper.readValue(response.getContentAsString(), UserContext.class);
        Assertions.assertEquals(COMPANY_NAME, responseUser.getName());
    }

    @Test
    @SneakyThrows
    public void test11_refreshUser_success(){
        var registeredToken = registerUserAndGetToken(createUserRequest()).split("\\.")[1];

        var loginRequest = new LoginRequestDTO();
        loginRequest.setEmail(USER_EMAIL);
        loginRequest.setPassword(USER_PASSWORD);
        var loginResponse = mvc.perform(post(Constants.V1_CONTROLLER_ROOT + "auth/login")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse();

        var refreshToken = objectMapper.readValue(loginResponse.getContentAsString(), LoginResponseDTO.class).getRefreshToken();

        var refreshResponse = mvc.perform(
                post(Constants.V1_CONTROLLER_ROOT + "auth/refresh")
                  .contentType(MediaType.APPLICATION_JSON_VALUE)
                  .content(refreshToken))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse();

        Base64.Decoder decoder = Base64.getUrlDecoder();

        var token = refreshResponse.getContentAsString().split("\\.")[1];
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        var actualUserContext = objectMapper.readValue(new String(decoder.decode(token)), UserContext.class);
        var expectedUserContext = objectMapper.readValue(new String(decoder.decode(registeredToken)), UserContext.class);

        Assertions.assertEquals(expectedUserContext, actualUserContext);
    }

    @Test
    @SneakyThrows
    public void test12_refreshUser_failure(){
        var refreshToken = "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjUxOSwiYXV0aG9yaXplZEFzVXNlcklkIjo1NTIsImlhdCI6MTY3ODk5MDU2NCwiZXhwIjoxNjc5MDc2OTY0fQ.3_qWnyc9weSct8eEXn5u7fohYw6TV6SSkRXiqyE8K_A";

        var exception = Assertions.assertThrows(ServletException.class, () -> mvc.perform(
                        post(Constants.V1_CONTROLLER_ROOT + "auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(refreshToken))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError())
                .andReturn()
                .getResponse());

        Assertions.assertTrue(exception.getMessage().contains("JWT signature does not match locally computed signature. JWT validity cannot be asserted and should not be trusted."));
    }

    private UserRegistrationRequestDTO createUserRequest(){
        var request = new UserRegistrationRequestDTO();
        request.setEmail(USER_EMAIL);
        request.setPassword(USER_PASSWORD);
        request.setFirstName(USER_FIRST_NAME);
        request.setLastName(USER_LAST_NAME);
        request.setAccountType(AccountType.PERSON);
        return request;
    }

    private String registerUserAndGetToken(UserRegistrationRequestDTO registrationRequest) throws Exception {
        mvc.perform(post(Constants.V1_CONTROLLER_ROOT + "auth/register")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturn()
                .getResponse();

        var loginRequest = new LoginRequestDTO();
        loginRequest.setEmail(USER_EMAIL);
        loginRequest.setPassword(USER_PASSWORD);
        var loginResponse = mvc.perform(post(Constants.V1_CONTROLLER_ROOT + "auth/login")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse();

        var loginData = objectMapper.readValue(loginResponse.getContentAsString(), LoginResponseDTO.class);
        Assertions.assertTrue(loginData.isSuccess());
        Assertions.assertNotNull(loginData.getRefreshToken());
        Assertions.assertNotNull(loginData.getAuthToken());
        return loginData.getAuthToken();
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ContextWrapper {

        private UserContext user;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AlgorithmHeader {

        private SignatureAlgorithm alg;
    }
}
