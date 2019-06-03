package vn.vccorp.servicemonitoring.utils;

/**
 * Name: tuyennta
 * Date: 08/05/2019.
 * Time: 12:48.
 */
public class AppConstants {
    public static final String API_MAPPING = "/api";

    public static final String ERROR_REGEX = "^\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}:\\d{2},\\d+\\s\\[ERROR].+";
    public static final String INFO_REGEX = "^\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}:\\d{2},\\d+\\s\\[INFO].+";
    public static final String DEBUG_REGEX = "^\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}:\\d{2},\\d+\\s\\[DEBUG].+";
    public static final String WARN_REGEX = "^\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}:\\d{2},\\d+\\s\\[WARN].+";
}
