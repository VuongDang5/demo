/**
 * Created by: tuyennta
 * Created on: 22/05/2019 17:42
 */

package vn.vccorp.servicemonitoring.logic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.vccorp.servicemonitoring.entity.UserService;

import java.util.List;

public interface UserServiceRepository extends JpaRepository<UserService, UserService.ServiceManagementKey> {

}
