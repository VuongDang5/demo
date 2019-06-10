/**
 * Created by: tuyennta
 * Created on: 20/05/2019 10:03
 */

package vn.vccorp.servicemonitoring.dto;

import lombok.*;
import vn.vccorp.servicemonitoring.enumtype.Status;
import vn.vccorp.servicemonitoring.validator.Path;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceDTO {

    private Integer id;

    @NotBlank
    @Size(max = 50)
    private String name;

    @NotBlank
    @Size(max = 1000)
    private String description;

    @NotNull
    private Integer serverId;

    @Size(max = 10)
    private String serverPort;

    @Size(max = 10)
    private String pid;

    @NotBlank
    @Size(max = 200)
    @Path
    private String deployDir;

    public String getDeployDir(){
        return deployDir.endsWith("/") ? deployDir : deployDir + "/";
    }

    @NotBlank
    @Size(max = 200)
    @Path
    private String logDir;

    public String getLogDir(){
        return logDir.endsWith("/") ? logDir : logDir + "/";
    }

    @NotBlank
    @Size(max = 100)
    private String logFile;

    @Size(max = 15)
    private String language;

    @NotBlank
    @Size(max = 1000)
    private String deployCommand;

    private Float ramLimit;

    private Float cpuLimit;

    private Float gpuLimit;

    private Float diskLimit;

    @Enumerated(EnumType.STRING)
    private Status status;

    @NotNull
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

    private List<Integer> maintainerIds;

    @NotNull
    private Integer ownerId;
}
