/**
 * Created by: tuyennta
 * Created on: 20/05/2019 10:03
 */

package vn.vccorp.servicemonitoring.dto;

import lombok.Getter;
import lombok.Setter;
import vn.vccorp.servicemonitoring.enumtype.Status;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Date;

@Getter
@Setter
public class ServiceDTO {

    private Integer id;

    @NotBlank
    @Size(max = 50)
    private String name;

    @NotBlank
    @Size(max = 1000)
    private String description;

    @NotBlank
    @Size(max = 15)
    private String serverId;

    @NotBlank
    @Size(max = 10)
    private String serverPort;

    @Size(max = 10)
    private String PID;

    @NotBlank
    @Size(max = 200)
    private String deployDir;

    @NotBlank
    @Size(max = 200)
    private String logDir;

    @NotBlank
    @Size(max = 100)
    private String logFile;

    @NotBlank
    @Size(max = 15)
    private String language;

    @Size(max = 100)
    private String mainJar;

    @Size(max = 100)
    private String originalJar;

    @Size(max = 100)
    private String dependencies;

    @NotBlank
    @Size(max = 1000)
    private String deployCommand;

    private Integer ramLimit;

    private Integer cpuLimit;

    private Integer gpuLimit;

    private Integer diskLimit;

    @Enumerated(EnumType.STRING)
    @NotBlank
    @Size(max = 10)
    private Status status;

    private Date startTime;

    private Date lastCheckTime;

    @NotBlank
    @Size(max = 100)
    private String project;

    @Size(max = 200)
    private String apiEndpoint;

    @Size(max = 200)
    private String kongMapping;

    @Size(max = 2000)
    private String note;
}
