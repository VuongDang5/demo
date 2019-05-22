/**
 * Created by: tuyennta
 * Created on: 11/05/2019 20:02
 */

package vn.vccorp.servicemonitoring.security;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;

@Target({METHOD, TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@PreAuthorize("hasAnyAuthority(T(vn.vccorp.servicemonitoring.enumtype.Role).ADMIN, " +
        "T(vn.vccorp.servicemonitoring.enumtype.Role).OWNER) " +
        "&& @CustomPermissionEvaluator.forService(#currentUserId, #serviceId)")
public @interface OwnerAuthorize {
    String currentUserId();
    String serviceId();
}
