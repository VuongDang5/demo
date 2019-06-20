/**
 * Created by: tuyennta
 * Created on: 22/05/2019 17:42
 */

package vn.vccorp.servicemonitoring.logic.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.vccorp.servicemonitoring.entity.UserService;
import vn.vccorp.servicemonitoring.enumtype.Role;

import java.util.List;

public interface UserServiceRepository extends JpaRepository<UserService, UserService.ServiceManagementKey> {
	UserService findByUserIdAndServiceId(int userId, int serviceId);
	
	List<UserService> findAllByRoleAndServiceId(Role owner, int serviceId);

}
