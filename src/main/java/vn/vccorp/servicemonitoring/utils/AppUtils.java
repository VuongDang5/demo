package vn.vccorp.servicemonitoring.utils;

import com.google.gson.Gson;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.Charset;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
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

    /**
     * Execute command on terminal and return the output
     * @param command   command to execute
     * @return  output of command
     */
    public static List<String> executeCommand(String command) {
        List<String> out = new ArrayList<>();
        String[] args = new String[]{"/bin/bash", "-c", command, "with", "args"};
        ProcessBuilder pb = new ProcessBuilder(args);
        try {
            out = execute(pb);
        } catch (IOException e) {
            LOGGER.error("Exception while executing command: " + command, e);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return out;
    }

    /**
     * Execute script file on terminal
     * @param filePath  file to execute
     * @return  output of script when execute on stdout
     */
    public static List<String> executeScriptFile(String filePath) {
        List<String> out = new ArrayList<>();
        ProcessBuilder pb = new ProcessBuilder(filePath);
        try {
            out = execute(pb);
        } catch (IOException e) {
            LOGGER.error("Exception while executing file: " + filePath, e);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return out;
    }

    private static List<String> execute(ProcessBuilder pb) throws IOException, InterruptedException {
        List<String> out = new ArrayList<>();
        Process proc = pb.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            out.add(line);
        }
        proc.waitFor();
        return out;
    }

    public static Date getStartedDateOfProcess(String serverId, String sshPort, String pid){
        String command = "ssh -p " + sshPort + " " + serverId + " -t 'date -r /proc/" + pid + " --rfc-3339=ns'";
        List<String> out = executeCommand(command);
        if (!out.isEmpty()){
//            return Date.from(OffsetDateTime.parse(out.get(0).replace(" ", "T")).toInstant());
            return LocalDateTime.parse(out.get(0).split("\\.")[0].replace(" ", "T")).toDate();
        }
        return LocalDateTime.now().toDate();
    }
}
