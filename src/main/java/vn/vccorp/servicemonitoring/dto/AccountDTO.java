/**
 * Created by: tuyennta
 * Created on: 09/05/2019 16:05
 */

package vn.vccorp.servicemonitoring.dto;

import lombok.*;
import org.springframework.data.annotation.Id;
import vn.vccorp.servicemonitoring.enumtype.Role;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccountDTO {
    protected Integer id;
    private String email;
    private String name;
    private String username;
    private List<Role> roles;
    private String password;
    private String phone;
}
