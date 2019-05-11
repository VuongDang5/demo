/**
 * Created by: tuyennta
 * Created on: 10/05/2019 20:22
 */

package vn.vccorp.servicemonitoring.enumtype;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {
    ADMIN,
    MAINTAINER,
    OWNER,
    USER;

    @Override
    public String toString() {
        return name();
    }

    @Override
    public String getAuthority() {
        return name();
    }
}
