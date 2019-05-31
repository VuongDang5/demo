package vn.vccorp.servicemonitoring.logic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import vn.vccorp.servicemonitoring.entity.LogService;

import java.util.Optional;

public interface LogServiceRepository extends ServiceRepositoryCustom, JpaRepository<LogService, Integer> {
    @Query(value = "select * from LogService where serviceId = ?1", nativeQuery = true)
    Optional<LogService> findByServiceId(int serviceId);

}
