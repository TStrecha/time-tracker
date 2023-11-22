package cz.tstrecha.timetracker.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum ErrorTypeCode {

    PERSON_FIRST_LAST_NAME_MISSING("PersonFirstLastNameMissing"),
    COMPANY_NAME_MISSING("CompanyNameMissing"),
    USER_EMAIL_EXISTS("UserEmailExists"),
    PASSWORD_DOES_NOT_CONTAIN_DIGIT("PasswordDoesNotContainDigit"),
    RELATIONSHIP_ALREADY_EXISTS("RelationshipAlreadyExists"),
    RELATIONSHIP_EDIT_WITHOUT_PERMISSION("RelationshipEditWithoutPermission"),
    USER_NOT_FOUND_BY_ID("UserNotFoundById"),
    TASK_NOT_FOUND_BY_ID("TaskNotFoundById"),
    TASK_IS_NOT_ACTIVE("TaskIsNotActive"),
    TASK_ALREADY_DONE("TaskAlreadyDone"),
    VALID_FROM_AFTER_VALID_TO("ValidFromIsAfterValidTo"),
    INTERSECTS_WITH_OTHER_SETTINGS("SettingsIntersect"),
    SETTING_NOT_FOUND_BY_ID("SettingNotFoundById"),
    SETTING_NO_LONGER_VALID("SettingNoLongerValid"),
    SETTING_NAME_NOT_UNIQUE("SettingNameIsNotUnique"),
    USER_DOES_NOT_HAVE_ALL_PERMISSIONS("UserDoesNotHaveAllPermissions"),
    USER_DOES_NOT_HAVE_ONE_PERMISSION("UserDoesNotHaveOnePermission"),
    USER_DOES_NOT_HAVE_REQUIRED_PERMISSIONS("UserDoesNotHaveRequiredPermissions"),
    USER_DOES_NOT_HAVE_PERMISSION_TO_CHANGE_CONTEXT("UserDoesNotHavePermissionToChangeContext"),
    ATTEMPTED_TO_CREATE_RELATIONSHIP_FOR_SOMEONE_ELSE("AttemptedToCreateRelationshipForSomeoneElse"),
    ATTEMPTED_TO_UPDATE_RELATIONSHIP_FOR_SOMEONE_ELSE("AttemptedToUpdateRelationshipForSomeoneElse"),
    PERMISSION_DID_NOT_MATCH_REQUIRED_PATTERN("PermissionDidNotMatchRequiredPattern");

    @Getter
    private String localizationCode;
}
