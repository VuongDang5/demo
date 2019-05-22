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
@PreAuthorize("hasAnyAuthority(T(vn.vccorp.servicemonitoring.enumtype.Role).USER, " +
        "T(vn.vccorp.servicemonitoring.enumtype.Role).MAINTAINER)")
public @interface MaintainerAuthorize {
}
