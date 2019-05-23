/**
 * Created by: tuyennta
 * Created on: 10/05/2019 20:27
 */

package vn.vccorp.servicemonitoring.logic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.vccorp.servicemonitoring.dto.ListDTO;
import vn.vccorp.servicemonitoring.entity.User;
import vn.vccorp.servicemonitoring.enumtype.Role;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsernameOrEmailAndIsDeleted(String username, String email, boolean isDeleted);

    List<User> findAllByRoleAndIsDeleted(Role role, boolean isDeleted);
    @Query(value =
            "SELECT u.name, u.username, u.email, u.phone, " +
                    "service.name AS serviceName, service.pid, service.role, service.description AS serviceDescription, service.status AS serviceStatus, " +
                    "server.name AS serverName, server.ip, server.groups, server.description AS serverDescription, server.status AS serverStatus " +
                    "FROM user u " +
                    "JOIN user_service ON u.id = user_service.user_id " +
                    "JOIN service ON service.id = user_service.service_id " +
                    "JOIN user_server ON u.id = user_server.user_id " +
                    "JOIN server ON server.id = user_server.server_id " +
                    "WHERE user_service.role = 'OWNER' " +
                    "OR user_service.role = 'MAINTAINER' "
                    ,
            nativeQuery = true)
    List<ListDTO> findAllUser();
    List<User> findAllByRole(Role role);

    Optional<User> findByIdAndIsDeleted(int id, boolean isDeleted);
}
