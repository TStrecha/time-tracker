package cz.tstrecha.timetracker.util;

public class FilterUtils {

    public static String enrichLikeStatements(String input){
        return "%" + input.toLowerCase() + "%";
    }
}
