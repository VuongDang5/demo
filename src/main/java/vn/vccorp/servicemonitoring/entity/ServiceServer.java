/**
 * Created by: tuyennta
 * Created on: 15/05/2019 09:55
 */

package vn.vccorp.servicemonitoring.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.vccorp.servicemonitoring.enumtype.Role;

import javax.persistence.*;
import java.io.Serializable;

@Getter
@Setter
@Entity
@Table
@NoArgsConstructor
@AllArgsConstructor
public class ServiceServer {

    @EmbeddedId
    private ServiceServerKey id;

    public ServiceServer(int serverId, int serviceId){
        id = new ServiceServerKey();
        id.setServerId(serverId);
        id.setServiceId(serviceId);
    }

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "serverId", insertable = false, updatable = false)
    private Server server;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "serviceId", insertable = false, updatable = false)
    private Service service;

    @Getter
    @Setter
    @Embeddable
    public static class ServiceServerKey implements Serializable {
        private Integer serviceId;
        private Integer serverId;
    }
}
