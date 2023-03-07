package cz.tstrecha.timetracker.service.impl;

import cz.tstrecha.timetracker.config.AppConfig;
import cz.tstrecha.timetracker.constant.AccountType;
import cz.tstrecha.timetracker.constant.UserRole;
import cz.tstrecha.timetracker.dto.RelationshipCreateUpdateRequestDTO;
import cz.tstrecha.timetracker.dto.UserRegistrationRequestDTO;
import cz.tstrecha.timetracker.repository.UserRepository;
import cz.tstrecha.timetracker.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

@Slf4j
@Component
@RequiredArgsConstructor
public class OnApplicationStartHandler {

    private final UserService userService;

    private final Environment environment;

    private final UserRepository userRepository;

    private final AppConfig appConfig;

    @EventListener(ApplicationReadyEvent.class)
    public void createUsersWhenEmptyDatabase() {
        TimeZone.setDefault(TimeZone.getTimeZone(appConfig.getDefaultTimeZone()));
        if(Arrays.stream(environment.getActiveProfiles()).noneMatch(profile -> profile.equals("prod")) && userRepository.count() == 0) {
            log.warn("Creating mock users.");

            var timeTrackerRegistrationRequest = new UserRegistrationRequestDTO();
            timeTrackerRegistrationRequest.setEmail("admin@timetracker.com");
            timeTrackerRegistrationRequest.setCompanyName("Time Tracker s.r.o.");
            timeTrackerRegistrationRequest.setAccountType(AccountType.COMPANY);
            timeTrackerRegistrationRequest.setPassword("appadministrator123");

            var authorizedUserRegistrationRequest = new UserRegistrationRequestDTO();
            authorizedUserRegistrationRequest.setEmail("strechatomas@outlook.com");
            authorizedUserRegistrationRequest.setFirstName("Tomáš");
            authorizedUserRegistrationRequest.setLastName("Střecha");
            authorizedUserRegistrationRequest.setAccountType(AccountType.PERSON);
            authorizedUserRegistrationRequest.setPassword("appuser123");

            var timeTrackerCreatedUser = userService.createUser(timeTrackerRegistrationRequest, UserRole.ADMIN);
            var authorizedCreatedUser = userService.createUser(authorizedUserRegistrationRequest, UserRole.USER);

            userService.createRelationship(new RelationshipCreateUpdateRequestDTO(timeTrackerCreatedUser.getId(), authorizedCreatedUser.getId(),
                    List.of("*"), OffsetDateTime.now(), null, false));
        }
    }

}
