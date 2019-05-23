/**
 * Created by: tuyennta
 * Created on: 09/05/2019 16:05
 */

package vn.vccorp.servicemonitoring.dto;

import lombok.*;
import org.springframework.data.annotation.Id;
import vn.vccorp.servicemonitoring.enumtype.Role;
import vn.vccorp.servicemonitoring.validator.EnumValidator;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConfigurationDTO {

	private Integer id;

	private String reportSchedule;

    private Integer ramLimit;

    private Integer cpuLimit;

    private Integer gpuLimit;

    private Integer diskLimit;
}
