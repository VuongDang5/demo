package vn.vccorp.servicemonitoring.dto.ServiceDetailsDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserInfo {
    private String name;
    private String username;
    private String email;
    private String phone;
    private String role;
}
