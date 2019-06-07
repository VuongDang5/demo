package vn.vccorp.servicemonitoring.dto;
import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
//DTO de lay rieng username tu UserDTO cua owner
public class UserInformationDTO {
    private String username;

}
