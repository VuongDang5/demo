/**
 * Created by: tuyennta
 * Created on: 09/05/2019 16:05
 */

package vn.vccorp.servicemonitoring.entity;

import lombok.*;
import vn.vccorp.servicemonitoring.dto.ListDTO;
import vn.vccorp.servicemonitoring.dto.UserServiceDTO;
import vn.vccorp.servicemonitoring.enumtype.Role;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

@SqlResultSetMapping(
        name="userDetailsMapping",
        classes = {
                @ConstructorResult(
                        targetClass = UserServiceDTO.class,
                        columns = {
                                @ColumnResult(name = "name"),
                                @ColumnResult(name = "username"),
                                @ColumnResult(name = "email"),
                                @ColumnResult(name = "phone"),
                                @ColumnResult(name = "services"),
                                @ColumnResult(name = "servers")
                        }
                )
        }
)
/*@NamedNativeQuery(name = "User.getAllOwnerOrMaintainerDetail",
        query = "SELECT u.name, u.username, u.email, u.phone, u.id, " +
                "GROUP_CONCAT( " +
                "CONCAT_WS(',', IFNULL(service.name, 'null'), IFNULL(service.pid, 'null'), IFNULL(user_service.role, 'null'), IFNULL(service.description, 'null'), IFNULL(service.status, 'null')) " +
                "ORDER BY service.pid " +
                "SEPARATOR ';' " +
                ") AS serviceInfo, " +
                "GROUP_CONCAT( " +
                "CONCAT_WS(',', IFNULL(server.name, 'null'), IFNULL(server.ip, 'null'), IFNULL(user_server.groups, 'null'), IFNULL(server.description, 'null'), IFNULL(server.status, 'null')) " +
                "ORDER BY server.ip " +
                "SEPARATOR ';' " +
                ") AS serverInfo " +
                "FROM user u " +
                "JOIN user_service ON u.id = user_service.user_id " +
                "JOIN service ON service.id = user_service.service_id " +
                "JOIN user_server ON (u.id = user_server.user_id AND service.server_id = user_server.server_id) " +
                "JOIN server ON server.id = user_server.server_id " +
                "WHERE user_service.role = 'OWNER' " +
                "OR user_service.role = 'MAINTAINER' ",
resultSetMapping = "userDetailsMapping")*/
@Entity
@Table(
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {
                        "username"
                }),
                @UniqueConstraint(columnNames = {
                        "email"
                })
        },
        indexes = {
                @Index(columnList = "email"),
                @Index(columnList = "username"),
                @Index(columnList = "role")
        }
)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @NotBlank(message = "{user.email.not-empty}")
    @Size(max = 50, message = "{user.email.max}")
    private String email;

    @NotBlank(message = "{user.name.not-empty}")
    @Size(max = 50, message = "{user.name.max}")
    private String name;

    @Size(max = 10, message = "{user.username.max}")
    private String username;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Size(max = 61, message = "{user.password.max}")
    private String password;

    @Size(max = 15, message = "{user.phone.max}")
    @NotBlank(message = "{user.phone.not-empty}")
    private String phone;

    private boolean isDeleted;

    @OneToMany(mappedBy = "user")
    private List<UserServer> servers;

    @OneToMany(mappedBy = "user")
    private List<UserService> services;

    @OneToMany(mappedBy = "user")
    private List<IssueTracking> issues;
}
