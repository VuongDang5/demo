package vn.vccorp.servicemonitoring.dto;

import lombok.*;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Min;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LogServiceDTO {

    protected Integer serviceId;

    @Min(0)
    private Integer start = 0;

    @Min(0)
    private Integer end = 0;
}
