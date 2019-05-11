/**
 * Created by: tuyennta
 * Created on: 10/05/2019 22:16
 */

package vn.vccorp.servicemonitoring.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import vn.vccorp.servicemonitoring.enumtype.Role;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "default.account")
@Getter
@Setter
public class RootUser {
    private String username;
    private String name;
    private String email;
    private String password;
    private List<Role> roles;
}
