package cz.tstrecha.timetracker.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class FilterUtils {

    public static String enrichLikeStatements(String input){
        return "%" + input.toLowerCase() + "%";
    }
}
