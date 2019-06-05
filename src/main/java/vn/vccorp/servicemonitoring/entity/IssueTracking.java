/**
 * Created by: tuyennta
 * Created on: 15/05/2019 11:21
 */

package vn.vccorp.servicemonitoring.entity;

import lombok.Getter;
import lombok.Setter;
import vn.vccorp.servicemonitoring.enumtype.IssueType;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Date;

@Getter
@Setter
@Entity
@Table
public class IssueTracking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "serviceId")
    private Service service;

    @ManyToOne
    @JoinColumn(name = "serverId")
    private Server server;

    private Date trackingTime;

    @Enumerated(EnumType.STRING)
    private IssueType issueType;

    @NotBlank
    @Size(max = 10000)
    private String detail;

    @Size(max = 1000)
    private String userAction;

    @ManyToOne
    @JoinColumn(name = "userId")
    private User user;
}
