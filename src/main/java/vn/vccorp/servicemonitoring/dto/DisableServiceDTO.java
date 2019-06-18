package vn.vccorp.servicemonitoring.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DisableServiceDTO {
    @NotBlank
    private Date expDate;
}
