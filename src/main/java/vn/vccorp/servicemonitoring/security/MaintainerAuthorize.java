/**
 * Created by: tuyennta
 * Created on: 11/05/2019 18:02
 */

package vn.vccorp.servicemonitoring.security;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@PreAuthorize("hasAnyAuthority(T(vn.vccorp.servicemonitoring.enumtype.Role).ADMIN, " +
        "T(vn.vccorp.servicemonitoring.enumtype.Role).OWNER," +
        "T(vn.vccorp.servicemonitoring.enumtype.Role).MAINTAINER) " +
        "&& @CustomPermissionEvaluator.forService(T(vn.vccorp.servicemonitoring.enumtype.Role).MAINTAINER, #serviceId)")
public @interface MaintainerAuthorize {
    String serviceId();
}
