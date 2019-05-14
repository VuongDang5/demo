/**
 * Created by: tuyennta
 * Created on: 09/05/2019 16:05
 */

package vn.vccorp.servicemonitoring.entity;

import lombok.*;
import vn.vccorp.servicemonitoring.enumtype.Role;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "user", uniqueConstraints = {
        @UniqueConstraint(columnNames = {
                "username"
        }),
        @UniqueConstraint(columnNames = {
                "email"
        })
})
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @NotBlank(message = "{user.email.not-empty}")
    @Size(max = 50, message = "{user.email.max}")
    private String email;

    @NotBlank(message = "{user.name.not-empty}")
    @Size(max = 50, message = "{user.name.max}")
    private String name;

    @Size(max = 10, message = "{user.username.max}")
    private String username;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Size(max = 61, message = "{user.password.max}")
    private String password;

    @Size(max = 15, message = "{user.phone.max}")
    @NotBlank(message = "{user.phone.not-empty}")
    private String phone;
}
