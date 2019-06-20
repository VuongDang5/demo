package vn.vccorp.servicemonitoring.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserServiceDTO {

	private Integer userId;

    private Integer serviceId;

    private String role;

}