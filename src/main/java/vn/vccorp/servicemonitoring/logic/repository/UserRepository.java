/**
 * Created by: tuyennta
 * Created on: 10/05/2019 20:27
 */

package vn.vccorp.servicemonitoring.logic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.vccorp.servicemonitoring.entity.User;
import vn.vccorp.servicemonitoring.enumtype.Role;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsernameOrEmailAndIsDeleted(String username, String email, boolean isDeleted);

    List<User> findAllByRoleAndIsDeleted(Role role, boolean isDeleted);
}
