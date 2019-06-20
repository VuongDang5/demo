/**
 * Created by: tuyennta
 * Created on: 14/05/2019 17:43
 */

package vn.vccorp.servicemonitoring.entity;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Getter
@Setter
@Entity
@Table
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserServer {

    @EmbeddedId
    private ServerManagementKey id;

    public UserServer(Integer serverId, String userName, Integer userId, String group){
        this.id = new ServerManagementKey();
        this.id.setUserId(userId);
        this.id.setServerId(serverId);
        this.username = userName;
        this.groups = group;
    }

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "serverId", insertable = false, updatable = false)
    private Server server;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "userId", insertable = false, updatable = false)
    private User user;

    @Size(max = 100)
    private String username;

    @Size(max = 1000)
    private String groups;

    @Getter
    @Setter
    @Embeddable
    public static class ServerManagementKey implements Serializable {
        private Integer userId;
        private Integer serverId;
    }
}