/**
 * Created by: tuyennta
 * Created on: 09/05/2019 16:05
 */

package vn.vccorp.servicemonitoring.dto;

import lombok.*;
import org.springframework.data.annotation.Id;
import vn.vccorp.servicemonitoring.enumtype.Role;
import vn.vccorp.servicemonitoring.validator.EnumValidator;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDTO {
    protected Integer id;
    @Email(message = "{user.email.invalid}")
    @NotBlank(message = "{user.email.not-empty}")
    @Size(max = 50, message = "{user.email.max}")
    private String email;

    @NotBlank(message = "{user.name.not-empty}")
    @Size(max = 50, message = "{user.name.max}")
    private String name;

    @Size(max = 10, message = "{user.username.max}")
    private String username;

    @Enumerated(EnumType.STRING)
    @EnumValidator(enumClass = Role.class, ignoreCase = true, message = "{user.role}")
    private String role;

    @Size(max = 50, message = "{user.password.max}")
    private String password;

    @Size(max = 15, message = "{user.phone.max}")
    @NotBlank(message = "{user.phone.not-empty}")
    private String phone;
}
