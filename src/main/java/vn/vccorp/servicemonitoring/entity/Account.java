/**
 * Created by: tuyennta
 * Created on: 09/05/2019 16:05
 */

package vn.vccorp.servicemonitoring.entity;

import lombok.*;
import vn.vccorp.servicemonitoring.enumtype.Role;
import javax.persistence.*;
import javax.validation.constraints.Email;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "account", uniqueConstraints = {
        @UniqueConstraint(columnNames = {
                "username"
        }),
        @UniqueConstraint(columnNames = {
                "email"
        })
})
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Email
    private String email;
    private String name;
    private String username;
    @ElementCollection
    private List<Role> roles;
    private String password;
    private String phone;
    private String token;
}
