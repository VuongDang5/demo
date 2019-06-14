/**
 * Created by: tuyennta
 * Created on: 14/05/2019 17:20
 */

package vn.vccorp.servicemonitoring.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.test.context.jdbc.Sql;
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
@SqlResultSetMappings(
        {@SqlResultSetMapping(
                name = "ServiceInfoMapping",
                classes = @ConstructorResult(
                        targetClass = vn.vccorp.servicemonitoring.dto.ServiceDetailsDTO.ServiceInfo.class,
                        columns = {
                                @ColumnResult(name = "name", type = String.class),
                                @ColumnResult(name = "description", type = String.class),
                                @ColumnResult(name = "serverPort", type = String.class),
                                @ColumnResult(name = "pid", type = String.class),
                                @ColumnResult(name = "deployDir", type = String.class),
                                @ColumnResult(name = "logDir", type = String.class),
                                @ColumnResult(name = "logFile", type = String.class),
                                @ColumnResult(name = "language", type = String.class),
                                @ColumnResult(name = "deployCommand", type = String.class),
                                @ColumnResult(name = "ramLimit", type = Float.class),
                                @ColumnResult(name = "cpuLimit", type = Float.class),
                                @ColumnResult(name = "gpuLimit", type = Float.class),
                                @ColumnResult(name = "diskLimit", type = Float.class),
                                @ColumnResult(name = "status", type = String.class),
                                @ColumnResult(name = "startTime", type = Date.class),
                                @ColumnResult(name = "lastCheckTime", type = Date.class),
                                @ColumnResult(name = "project", type = String.class),
                                @ColumnResult(name = "apiEndpoint", type = String.class),
                                @ColumnResult(name = "kongMapping", type = String.class),
                                @ColumnResult(name = "note", type = String.class)
                        }
                )
        ),
                @SqlResultSetMapping(
                        name = "ServerInfoMapping",
                        classes = @ConstructorResult(
                                targetClass = vn.vccorp.servicemonitoring.dto.ServiceDetailsDTO.ServerInfo.class,
                                columns = {
                                        @ColumnResult(name = "ip", type = String.class),
                                        @ColumnResult(name = "name", type = String.class),
                                        @ColumnResult(name = "description", type = String.class),
                                        @ColumnResult(name = "rootPath", type = String.class),
                                        @ColumnResult(name = "status", type = String.class)
                                }
                        )
                )
                ,
                @SqlResultSetMapping(
                        name = "UserInfoMapping",
                        classes = @ConstructorResult(
                                targetClass = vn.vccorp.servicemonitoring.dto.ServiceDetailsDTO.UserInfo.class,
                                columns = {
                                        @ColumnResult(name = "name", type = String.class),
                                        @ColumnResult(name = "username", type = String.class),
                                        @ColumnResult(name = "email", type = String.class),
                                        @ColumnResult(name = "phone", type = String.class),
                                        @ColumnResult(name = "role", type = String.class)
                                }
                        )
                )
                ,
                @SqlResultSetMapping(
                        name = "SnapshotInfoMapping",
                        classes = @ConstructorResult(
                                targetClass = vn.vccorp.servicemonitoring.dto.ServiceDetailsDTO.SnapshotInfo.class,
                                columns = {
                                        @ColumnResult(name = "time", type = Date.class),
                                        @ColumnResult(name = "ramFree", type = Float.class),
                                        @ColumnResult(name = "ramUsed", type = Float.class),
                                        @ColumnResult(name = "cpuFree", type = Float.class),
                                        @ColumnResult(name = "cpuUsed", type = Float.class),
                                        @ColumnResult(name = "gpuFree", type = Float.class),
                                        @ColumnResult(name = "gpuUsed", type = Float.class),
                                        @ColumnResult(name = "diskFree", type = Float.class),
                                        @ColumnResult(name = "diskUsed", type = Float.class)
                                }
                        )
                )
                ,
                @SqlResultSetMapping(
                        name = "IssueInfoMapping",
                        classes = @ConstructorResult(
                                targetClass = vn.vccorp.servicemonitoring.dto.ServiceDetailsDTO.IssueInfo.class,
                                columns = {
                                        @ColumnResult(name = "detail", type = String.class),
                                        @ColumnResult(name = "issueType", type = String.class),
                                        @ColumnResult(name = "trackingTime", type = Date.class),
                                        @ColumnResult(name = "userAction", type = String.class),
                                        @ColumnResult(name = "name", type = String.class),
                                        @ColumnResult(name = "email", type = String.class)
                                }
                        )
                )})
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
