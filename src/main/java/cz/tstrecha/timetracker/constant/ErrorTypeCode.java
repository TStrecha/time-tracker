package cz.tstrecha.timetracker.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum ErrorTypeCode {
    PERSON_FIRST_LAST_NAME_MISSING("PersonFirstLastNameMissing"),
    COMPANY_NAME_MISSING("CompanyNameMissing"),
    USER_EMAIL_EXISTS("UserEmailExists"),
    PASSWORD_DOESNT_CONTAIN_DIGIT("PasswordDoesntContainDigit"),
    RELATIONSHIP_ALREADY_EXISTS("RelationshipAlreadyExists"),
    REALTIONSHIP_EDIT_WITHOUT_PERMISSION("RelationshipEditWithoutPermission"),
    USER_NOT_FOUND_BY_ID("UserNotFoundById"),
    USER_DOESNT_HAVE_ALL_PERMISSIONS("UserDoesntHaveAllPermissions"),
    USER_DOESNT_HAVE_ONE_PERMISSION("UserDoesntHaveOnePermission"),
    USER_DOESNT_HAVE_REQUIRED_PERMISSIONS("UserDoesntHaveRequiredPermissions"),
    USER_DOESNT_HAVE_PERMISSION_TO_CHANGE_CONTEXT("UserDoesntHavePermissionToChangeContext"),
    USER_ATTEMPTS_TO_CREATE_RELATIONSHIP_FOR_SOMEONE_ELSE("UserAttemptsToCreateRelationshipForSomeoneElse"),
    USER_ATTEMPTS_TO_UPDATE_RELATIONSHIP_FOR_SOMEONE_ELSE("UserAttemptsToUpdateRelationshipForSomeoneElse"),
    PERMISSION_DIDNT_MATCH_REQUIRED_PATTERN("PermissionDidntMatchRequiredPattern");

    @Getter
    private String localizationCode;
}
