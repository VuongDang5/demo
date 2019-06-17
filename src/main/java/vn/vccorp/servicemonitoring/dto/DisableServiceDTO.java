package vn.vccorp.servicemonitoring.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DisableServiceDTO {
    private Integer serviceId;
    @NotBlank
    private String expDate;
}
