/**
 * Created by: tuyennta
 * Created on: 06/06/2019 09:58
 */

package vn.vccorp.servicemonitoring.validator;

import org.springframework.beans.factory.annotation.Value;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class PathValidator implements ConstraintValidator<Path, String> {

    @Value("${path.server-prefix}")
    private String prefix;

    @Override
    public boolean isValid(String path, ConstraintValidatorContext constraintValidatorContext) {
        return path.startsWith(prefix);
    }
}
