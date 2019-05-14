/**
 * Created by: tuyennta
 * Created on: 10/05/2019 20:27
 */

package vn.vccorp.servicemonitoring.logic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.vccorp.servicemonitoring.entity.User;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsernameOrEmail(String username, String email);
}
