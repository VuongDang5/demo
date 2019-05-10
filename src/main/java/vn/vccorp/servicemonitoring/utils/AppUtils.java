package vn.vccorp.servicemonitoring.utils;

import com.google.gson.Gson;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Name: tuyennta
 * Date: 08/05/2019.
 * Time: 12:48.
 */
public class AppUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppUtils.class);

    public static void returnResponse(HttpServletResponse response, Object object) {
        try {
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Access-Control-Allow-Credentials", "true");
            response.setHeader("Access-Control-Allow-Methods", "POST, GET, HEAD, OPTIONS");
            response.setHeader("Access-Control-Allow-Headers", "Origin, Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers");
            String json = new Gson().toJson(object);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(json);
        } catch (IOException e) {
        }
    }

    public static PageRequest createPageRequest(Integer page, Integer limit, Sort sort) {
        PageRequest pageRequest;
        if (null == page || 0 > page) {
            page = 0;
        }
        if (null == limit || 1 > limit) {
            limit = 10;
        }
        if (null == sort) {
            pageRequest = PageRequest.of(page, limit);
        } else {
            pageRequest = PageRequest.of(page, limit, sort);
        }
        return pageRequest;
    }

    public static Integer extractDigitsFromString(String str) {
        try {
            if (!StringUtils.isEmpty(str)) {
                Integer value = Integer.valueOf(str.replaceAll("\\D+", ""));
                if (str.toLowerCase().contains("b")) {
                    value *= 1000000000;
                } else if (str.toLowerCase().contains("m")) {
                    value *= 1000000;
                } else if (str.toLowerCase().contains("k")) {
                    value *= 1000;
                }
                return value;
            }
        } catch (Exception e) {
        }
        return 0;
    }

    public static boolean isValidEmailAddress(String email) {
        return EmailValidator.getInstance().isValid(email);
    }

    public static boolean isValidWebsiteAddress(String website) {
        return UrlValidator.getInstance().isValid(website);
    }

    public static boolean isValidFbMessenger(String fbMessenger) {
        if (StringUtils.isEmpty(fbMessenger)) {
            return false;
        }
        return fbMessenger.contains("m.me/");
    }

    public static boolean isValidYoutube(String youtube) {
        if (StringUtils.isEmpty(youtube)) {
            return false;
        }
        return youtube.contains("youtube.com");
    }

    public static boolean checkPasswordByRule(String password) {
        return password != null && password.length() >= 8 && Pattern.matches("[a-zA-Z]+[0-9]+", password);
    }

    public static Map<String, String> getQueryParams(String url) {
        Map<String, String> result = new HashMap<>();
        try {
            List<NameValuePair> params = URLEncodedUtils.parse(new URI(url), Charset.forName("UTF-8"));

            for (NameValuePair param : params) {
                result.put(param.getName(), param.getValue());
            }
        } catch (Exception e) {
            LOGGER.error("queryQueryParams function {}", e);
        }
        return result;
    }
}
