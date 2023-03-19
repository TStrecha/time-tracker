package cz.tstrecha.timetracker.config;

import cz.tstrecha.timetracker.constant.ErrorTypeCode;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.ResourceBundle;

@Component
public class ErrorCodeResolver {

    private final Locale defaultLocale = new Locale.Builder().setLanguage("cs").setRegion("CZ").build();

    private final ResourceBundle errorCodeBundle = ResourceBundle.getBundle("error_codes", defaultLocale);

    public String resolveException(ErrorTypeCode errorTypeCode){
        return errorCodeBundle.getString(errorTypeCode.getLocalizationCode());
    }
}
