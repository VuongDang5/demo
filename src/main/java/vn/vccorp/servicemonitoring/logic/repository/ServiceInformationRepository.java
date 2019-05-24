package vn.vccorp.servicemonitoring.logic.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import vn.vccorp.servicemonitoring.entity.Service;

public interface ServiceInformationRepository extends JpaRepository<Service, Integer>{

    @Query(value =
            "SELECT s.id, s.pid, api_endpoint, s.description, s.name, project, kong_mapping, snapshot.time, note, s.server_id," +
                    " server_port, start_time, status, user_service.role," +
                    " snapshot.cpu_used, snapshot.disk_used, snapshot.gpu_used, snapshot.ram_used " +
            "FROM service as s " +
            "JOIN user_service ON s.id = user_service.service_id " +
            "JOIN user ON user_service.user_id = user.id " +
            "JOIN snapshot ON s.id = snapshot.service_id"
            , nativeQuery = true)

    Page<Service> showAllService(Pageable firstPageWithFourElements);
}
