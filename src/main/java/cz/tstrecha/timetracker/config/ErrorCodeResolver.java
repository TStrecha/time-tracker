package cz.tstrecha.timetracker.config;

import cz.tstrecha.timetracker.constant.ErrorTypeCode;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.ResourceBundle;

@Component
public class ErrorCodeResolver {

    private ResourceBundle errorCodeBundle;

    public String resolveException(ErrorTypeCode errorTypeCode){
        Locale locale = LocaleContextHolder.getLocale();
        if (ClassLoader.getSystemResource("error_codes_" + locale + ".properties") == null){
            locale = new Locale.Builder().setLanguage("cs").setRegion("CZ").build();
        }
        errorCodeBundle = ResourceBundle.getBundle("error_codes", locale);
        return errorCodeBundle.getString(errorTypeCode.getLocalizationCode());
    }
}
