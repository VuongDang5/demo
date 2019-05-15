/**
 * Created by: tuyennta
 * Created on: 15/05/2019 09:55
 */

package vn.vccorp.servicemonitoring.entity;

import lombok.Getter;
import lombok.Setter;
import vn.vccorp.servicemonitoring.enumtype.Role;

import javax.persistence.*;
import java.io.Serializable;

@Getter
@Setter
@Entity
@Table
public class ServiceManagement {

    @EmbeddedId
    private ServiceManagementKey id;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "userId", insertable = false, updatable = false)
    private User user;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "serviceId", insertable = false, updatable = false)
    private Service service;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Getter
    @Setter
    @Embeddable
    public class ServiceManagementKey implements Serializable {
        private Integer serviceId;
        private Integer userId;
    }
}
