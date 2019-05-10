package vn.vccorp.servicemonitoring.validator;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Name: tuyennta
 * Date: 08/05/2019.
 * Time: 12:48.
 */
public class AccountValidator implements Validator {
    @Override
    public boolean supports(Class<?> aClass) {
        return AccountValidator.class.equals(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {

    }
}
