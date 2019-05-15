/**
 * Created by: tuyennta
 * Created on: 15/05/2019 10:17
 */

package vn.vccorp.servicemonitoring.entity;

import io.swagger.models.auth.In;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(indexes = {
        @Index(columnList = "time")
})
public class Snapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank
    private Date time;

    @Size(max = 100)
    private String ramFree;

    @Size(max = 100)
    private String ramUsed;

    @Size(max = 100)
    private String cpuFree;

    @Size(max = 100)
    private String cpuUsed;

    @Size(max = 100)
    private String gpuFree;

    @Size(max = 100)
    private String gpuUsed;

    @Size(max = 100)
    private String diskFree;

    @Size(max = 100)
    private String diskUsed;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "serverId", insertable = false, updatable = false)
    private Server server;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "serviceId", insertable = false, updatable = false)
    private Service service;
}
