package cz.tstrecha.timetracker.security;

import cz.tstrecha.timetracker.IntegrationTest;
import cz.tstrecha.timetracker.annotation.CustomPermissionCheck;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.prepost.PreFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;

@Slf4j
class SecurityIT extends IntegrationTest {

    public static List<String> IGNORED_DEFINITIONS = List.of(
            "org.springdoc.webmvc.api.OpenApiWebMvcResource#openapiJson(HttpServletRequest, String, Locale)",
            "org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController#error(HttpServletRequest)",
            "org.springdoc.webmvc.ui.SwaggerWelcomeWebMvc#redirectToUi(HttpServletRequest)",
            "org.springdoc.webmvc.ui.SwaggerConfigResource#openapiJson(HttpServletRequest)",
            "org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController#errorHtml(HttpServletRequest, HttpServletResponse)",
            "org.springdoc.webmvc.api.OpenApiWebMvcResource#openapiYaml(HttpServletRequest, String, Locale)"
    );

    @Autowired
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    @Test
    void testEndpointSecurity() {
        var softly = new SoftAssertions();

        requestMappingHandlerMapping.getHandlerMethods().forEach((mapping, handler) -> {
            var apiResult = verifyHandler(mapping, handler);

            softly.assertThat(apiResult.status.valid).withFailMessage(() -> getFailureMessage(apiResult)).isTrue();
        });

        softly.assertAll();
    }

    private ApiCheckResult verifyHandler(RequestMappingInfo mapping, HandlerMethod handler) {
        if(IGNORED_DEFINITIONS.contains(handler.toString())) {
            return new ApiCheckResult(mapping, handler, CheckResultStatus.IGNORED, "This endpoint is ignored because it is defined in other sources.");
        }

        if (Arrays.stream(handler.getMethod().getAnnotations()).anyMatch(annotation -> annotation instanceof CustomPermissionCheck)) {
            return new ApiCheckResult(mapping, handler, CheckResultStatus.CUSTOM_PERMISSION_CHECK, null);
        }

        var preAuthorizeAnnotation = getAnnotation(handler, PreAuthorize.class);
        if(preAuthorizeAnnotation != null) {
            return handleAnnotationValue(mapping, handler, preAuthorizeAnnotation.annotationType(), preAuthorizeAnnotation.value());
        }

        var preFilterAnnotation = getAnnotation(handler, PreFilter.class);
        if(preFilterAnnotation != null) {
            return handleAnnotationValue(mapping, handler, preFilterAnnotation.annotationType(), preFilterAnnotation.value());
        }

        var postFilterAnnotation = getAnnotation(handler, PostFilter.class);
        if(postFilterAnnotation != null) {
            return handleAnnotationValue(mapping, handler, postFilterAnnotation.annotationType(), postFilterAnnotation.value());
        }

        return new ApiCheckResult(mapping, handler, CheckResultStatus.MISSING, "Method lacks any permission annotation.");
    }

    private <T> T getAnnotation(HandlerMethod handler, Class<T> clazz) {
        return Arrays.stream(handler.getMethod().getAnnotations())
                .filter(annotation -> annotation.annotationType().isAssignableFrom(clazz))
                .map(clazz::cast)
                .findFirst()
                .orElse(null);
    }

    private ApiCheckResult handleAnnotationValue(RequestMappingInfo mapping, HandlerMethod handler, Class<? extends Annotation> annotationType, String annotationValue) {
        if(annotationValue.contains("hasPermission")) {
            return new ApiCheckResult(mapping, handler, CheckResultStatus.FULLY_AUTHORIZED, null);
        } else if(annotationValue.contains("hasRole")) {
            return new ApiCheckResult(mapping, handler, CheckResultStatus.ROLE_RESTRICTED, null);
        }

        return new ApiCheckResult(mapping, handler, CheckResultStatus.INSUFFICIENT_SECURITY, STR."Method has spring permission annotation @\{annotationType.getSimpleName()}, but doesn't check for neither role or hasPermission.");
    }

    private String getFailureMessage(ApiCheckResult apiResult) {
        System.out.println(apiResult.handler.toString());

        return STR." Endpoint \{apiResult.getMapping().toString()} failed security test. Status: \{apiResult.getStatus()} Message: \{apiResult.getMessage()}";
    }

    @Getter
    @AllArgsConstructor
    private enum CheckResultStatus {
        FULLY_AUTHORIZED(true),
        ROLE_RESTRICTED(true),
        CUSTOM_PERMISSION_CHECK(true),
        IGNORED(true),
        INSUFFICIENT_SECURITY(false),
        MISSING(false);

        private final boolean valid;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class ApiCheckResult {
        private RequestMappingInfo mapping;
        private HandlerMethod handler;
        private CheckResultStatus status;
        private String message;
    }
}
