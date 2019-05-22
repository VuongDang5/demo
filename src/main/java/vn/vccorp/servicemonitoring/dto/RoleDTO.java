package vn.vccorp.servicemonitoring.dto;

import lombok.*;
import vn.vccorp.servicemonitoring.enumtype.Role;
import vn.vccorp.servicemonitoring.validator.EnumValidator;

import javax.persistence.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class RoleDTO {

    protected Integer id;

    @Enumerated(EnumType.STRING)
    @EnumValidator(enumClass = Role.class, ignoreCase = true, message = "{user.role}")
    private String role;
}
