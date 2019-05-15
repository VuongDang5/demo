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

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "serviceId", insertable = false, updatable = false)
    private Service service;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "serverId", insertable = false, updatable = false)
    private Server server;

    private Date trackingTime;

    @Enumerated(EnumType.STRING)
    @Size(max = 10)
    private IssueType issueType;

    @NotBlank
    @Size(max = 1000)
    private String detail;

    @Size(max = 1000)
    private String userAction;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "userId", insertable = false, updatable = false)
    private User user;
}
