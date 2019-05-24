/**
 * Created by: tuyennta
 * Created on: 24/05/2019 18:49
 */

package vn.vccorp.servicemonitoring.logic.repository.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.vccorp.servicemonitoring.dto.ServiceInfoDTO;
import vn.vccorp.servicemonitoring.entity.Service;
import vn.vccorp.servicemonitoring.logic.repository.ServiceRepositoryCustom;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

public class ServiceRepositoryCustomImpl implements ServiceRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<ServiceInfoDTO> showAllService(Pageable firstPageWithFourElements) {
        String queryStr = "SELECT s.id, s.PID, s.api_endpoint, s.description, s.name, s.project, s.kong_mapping, " +
                "s.note, s.server_id," +
                " s.server_port, s.start_time, s.status, " +
                "us.role," +
                "sn.time, sn.cpu_used, sn.disk_used, sn.gpu_used, sn.ram_used " +
                "FROM Service s " +
                "JOIN UserService us ON s.id = us.service_id " +
                "JOIN User u ON us.user_id = u.id " +
                "JOIN Snapshot sn ON s.id = sn.service_id";
        Query query = entityManager.createQuery(queryStr);
        List<Service> resultList = query.getResultList();

        return null;
    }
}
