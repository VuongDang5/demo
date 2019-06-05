package vn.vccorp.servicemonitoring.logic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.vccorp.servicemonitoring.entity.UserServer;

public interface UserServerRepository extends JpaRepository<UserServer, UserServer.ServerManagementKey> {

}
