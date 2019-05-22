package vn.vccorp.servicemonitoring.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Locale;

/**
 * Name: tuyennta
 * Date: 08/05/2019.
 * Time: 12:48.
 */
@Component
public class Messages {

    private static final Logger LOGGER = LoggerFactory.getLogger(Messages.class);

    @Autowired
    private MessageSource messageSource;

    private MessageSourceAccessor accessor;

    @PostConstruct
    private void init() {
        accessor = new MessageSourceAccessor(messageSource, Locale.ENGLISH);
    }

    public String get(String code) {
        try {
            return accessor.getMessage(code, LocaleContextHolder.getLocale());
        } catch (Exception e) {
            LOGGER.error("Get message occurs exception: {}", e);
            return code;
        }
    }

    public String get(String code, Locale locale) {
        try {
            return accessor.getMessage(code, locale);
        } catch (Exception e) {
            LOGGER.error("Get message occurs exception: {}", e);
            return code;
        }
    }

    public String get(String code, Object[] args) {
        try {
            return accessor.getMessage(code, args, LocaleContextHolder.getLocale());
        } catch (Exception e) {
            LOGGER.error("Get message occurs exception: {}", e);
            return code;
        }
    }

//    public String get(String code, String args) {
//        try {
//            final String[] params = new String[]{args};
//            return accessor.getMessage(code, params, LocaleContextHolder.getLocale());
//        } catch (Exception e) {
//            LOGGER.error("Get message occurs exception: {}", e);
//            return code;
//        }
//    }

    public String get(String code, Object[] args, Locale locale) {
        try {
            return accessor.getMessage(code, args, locale);
        } catch (Exception e) {
            LOGGER.error("Get message occurs exception: {}", e);
            return code;
        }
    }
}
