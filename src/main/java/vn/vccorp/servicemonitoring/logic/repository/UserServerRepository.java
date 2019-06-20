package vn.vccorp.servicemonitoring.logic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import vn.vccorp.servicemonitoring.entity.UserServer;

import java.util.List;

public interface UserServerRepository extends JpaRepository<UserServer, UserServer.ServerManagementKey> {
    @Query(value = "select group_concat(username) from user_server where user_server.server_id = ?1", nativeQuery = true)
    String findNameByServerId(int serverId);
}
