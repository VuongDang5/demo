/**
 * Created by: tuyennta
 * Created on: 10/05/2019 22:16
 */

package vn.vccorp.servicemonitoring.security;

import lombok.Getter;
import lombok.Setter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "default.config")
@Getter
@Setter
public class RootConfig {
	
	private Integer id;
	
    private String reportSchedule;

    private Integer ramLimit;

    private Integer cpuLimit;

    private Integer gpuLimit;

    private Integer diskLimit;
}
