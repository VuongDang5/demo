/**
 * Created by: tuyennta
 * Created on: 15/05/2019 16:47
 */

package vn.vccorp.servicemonitoring.logic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.vccorp.servicemonitoring.entity.ServiceManagement;
import vn.vccorp.servicemonitoring.enumtype.Role;

import java.util.Optional;

public interface ServiceManagementRepository extends JpaRepository<ServiceManagement, Integer> {

    Optional<ServiceManagement> findByUserIdAndRole(int userId, Role role);
}
