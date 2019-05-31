/**
 * Created by: tuyennta
 * Created on: 14/05/2019 17:20
 */

package vn.vccorp.servicemonitoring.entity;

import lombok.Getter;
import lombok.Setter;
import vn.vccorp.servicemonitoring.enumtype.Language;
import vn.vccorp.servicemonitoring.enumtype.Status;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@Entity
@Table(
        indexes = {
                @Index(columnList = "name"),
                @Index(columnList = "serverPort"),
                @Index(columnList = "project"),
                @Index(columnList = "pid"),
                @Index(columnList = "status")
        },
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {
                        "pid"
                }),
                @UniqueConstraint(columnNames = {
                        "name"
                })
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

    @Size(max = 10)
    private String serverPort;

    @Size(max = 10)
    private String pid;

    @NotBlank
    @Size(max = 200)
    private String deployDir;

    public String getDeployDir() {
        return deployDir.endsWith("/") ? deployDir : deployDir + "/";
    }

    @NotBlank
    @Size(max = 200)
    private String logDir;

    @NotBlank
    @Size(max = 100)
    private String logFile;

    @Enumerated(EnumType.STRING)
    private Language language;

    @NotBlank
    @Size(max = 1000)
    private String deployCommand;

    private Integer ramLimit;

    private Integer cpuLimit;

    private Integer gpuLimit;

    private Integer diskLimit;

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

    @OneToMany(mappedBy = "service")
    private List<UserService> userServices;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "serverId")
    private Server server;

    @OneToMany(mappedBy = "service")
    private List<Snapshot> snapshots;

    @OneToMany(mappedBy = "service")
    private List<IssueTracking> issues;
}
