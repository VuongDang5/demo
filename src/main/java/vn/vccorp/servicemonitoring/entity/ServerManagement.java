/**
 * Created by: tuyennta
 * Created on: 14/05/2019 17:43
 */

package vn.vccorp.servicemonitoring.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Getter
@Setter
@Entity
@Table
public class ServerManagement {

    @EmbeddedId
    private ServerManagementKey id;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "serverId", insertable = false, updatable = false)
    private Server server;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "userId", insertable = false, updatable = false)
    private User user;

    @Size(max = 10)
    @NotBlank
    private String username;

    @Size(max = 1000)
    private String groups;

    @Embeddable
    public class ServerManagementKey implements Serializable {
        private Integer userId;
        private Integer serverId;
    }
}