/**
 * Created by: tuyennta
 * Created on: 10/05/2019 20:27
 */

package vn.vccorp.servicemonitoring.logic.repository;

import org.bouncycastle.asn1.DERTaggedObject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.vccorp.servicemonitoring.dto.ListDTO;
import vn.vccorp.servicemonitoring.entity.User;
import vn.vccorp.servicemonitoring.enumtype.Role;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer>, CustomUserdataRepository {
    Optional<User> findByUsernameOrEmailAndIsDeleted(String username, String email, boolean isDeleted);

    List<User> findAllByRoleAndIsDeleted(Role role, boolean isDeleted);
    List<User> findAllByRole(Role role);
    @Query(nativeQuery = true)
    List<ListDTO> getAllOwnerOrMaintainerDetail();
    Optional<User> findByIdAndIsDeleted(int id, boolean isDeleted);
}

