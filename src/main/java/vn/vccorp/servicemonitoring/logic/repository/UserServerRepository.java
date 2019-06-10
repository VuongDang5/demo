package vn.vccorp.servicemonitoring.logic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.vccorp.servicemonitoring.entity.UserServer;

import java.util.List;

public interface UserServerRepository extends JpaRepository<UserServer, UserServer.ServerManagementKey> {
    List<UserServer> findAllByServerId(int serverId);
}
