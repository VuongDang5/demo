/**
 * Created by: tuyennta
 * Created on: 06/06/2019 10:01
 */

package vn.vccorp.servicemonitoring.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = {PathValidator.class})
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Path {
    String message() default "Path is not valid, path must be started with /data";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
