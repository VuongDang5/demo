/**
 * Created by: tuyennta
 * Created on: 15/05/2019 13:49
 */

package vn.vccorp.servicemonitoring.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.Size;

@Getter
@Setter
@Entity
@Table
public class Configuration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Size(max = 100)
    private String reportSchedule;

    @Size(max = 100)
    private String healthCheckSchedule;

    private Integer ramLimit;

    private Integer cpuLimit;

    private Integer gpuLimit;

    private Integer diskLimit;
}
