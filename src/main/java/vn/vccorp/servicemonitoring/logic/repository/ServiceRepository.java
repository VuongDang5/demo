/**
 * Created by: tuyennta
 * Created on: 20/05/2019 17:28
 */

package vn.vccorp.servicemonitoring.logic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import vn.vccorp.servicemonitoring.entity.Service;
import vn.vccorp.servicemonitoring.entity.User;


import java.awt.print.Pageable;
import java.util.List;

public interface ServiceRepository extends JpaRepository<Service, Integer> {

   @Query(value =
            "SELECT id, pid, api_endponit, description, service.name, project, kong_mapping, last_check_time, note, server_id, server_port, start_time, status,user_service.role, snapshot.cpu_used, snapshot.disk_used, snapshot.gpu_used, ram_used " +
            "FROM service, user_service, user, snapshot " +
            "WHERE service.service_id = user_service.id " +
            "WHERE service.last_check_time = snapshot.time " +
            "WHERE user_service.role = user.role ", nativeQuery = true)

    List<Service> findAll();
}
