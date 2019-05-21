/**
 * Created by: tuyennta
 * Created on: 14/05/2019 17:20
 */

package vn.vccorp.servicemonitoring.entity;

import lombok.Getter;
import lombok.Setter;
import vn.vccorp.servicemonitoring.enumtype.Status;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@Entity
@Table(indexes = {
        @Index(columnList = "name"),
        @Index(columnList = "serverId"),
        @Index(columnList = "serverPort"),
        @Index(columnList = "project"),
        @Index(columnList = "status")
})
public class Service {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    @Size(max = 10)
    private String serverPort;

    @NotBlank
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

    @NotBlank
    private Date startTime;

    @NotBlank
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

    @OneToMany(mappedBy = "service")
    private List<ServiceManagement> services;

    @OneToMany(mappedBy = "service")
    private List<Snapshot> snapshots;

    @OneToMany(mappedBy = "service")
    private List<IssueTracking> issues;
}
