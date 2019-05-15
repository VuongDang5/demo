/**
 * Created by: tuyennta
 * Created on: 14/05/2019 16:58
 */

package vn.vccorp.servicemonitoring.entity;

import lombok.Getter;
import lombok.Setter;
import vn.vccorp.servicemonitoring.enumtype.Status;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

@Getter
@Setter
@Entity
@Table(indexes = {
        @Index(columnList = "ip"),
        @Index(columnList = "name")
})
public class Server {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank
    @Size(max = 15)
    private String ip;

    @NotBlank
    @Size(max = 50)
    private String name;

    @NotBlank
    @Size(max = 1000)
    private String description;

    @NotBlank
    @Size(max = 10)
    @Enumerated(EnumType.STRING)
    private Status status;

    @OneToMany(mappedBy = "server")
    private List<ServerManagement> managers;

    @OneToMany(mappedBy = "server")
    private List<Snapshot> snapshots;

    @OneToMany(mappedBy = "server")
    private List<IssueTracking> issues;
}
