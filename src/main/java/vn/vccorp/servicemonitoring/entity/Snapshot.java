/**
 * Created by: tuyennta
 * Created on: 15/05/2019 10:17
 */

package vn.vccorp.servicemonitoring.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
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

    @NotNull
    private Date time;

    private Float ramFree;

    private Float ramUsed;

    private Float cpuFree;

    private Float cpuUsed;

    private Float gpuFree;

    private Float gpuUsed;

    private Float diskFree;

    private Float diskUsed;

    @ManyToOne
    @JoinColumn(name = "serverId")
    private Server server;

    @ManyToOne
    @JoinColumn(name = "serviceId")
    private Service service;
}
