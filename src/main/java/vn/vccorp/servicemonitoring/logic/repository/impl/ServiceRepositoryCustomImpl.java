/**
 * Created by: tuyennta
 * Created on: 24/05/2019 18:49
 */

package vn.vccorp.servicemonitoring.logic.repository.impl;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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
    public PageImpl<Service> showAllService(Pageable page) {
        String queryStr = "SELECT s.id, s.pid, s.apiEndpoint, s.description, s.name, s.project, s.kongMapping, " +
                "s.note, s.server.id, s.serverPort, s.startTime, s.status, " +
                "us.role," +
                "sn.time, sn.cpuUsed, sn.diskUsed, sn.gpuUsed, sn.ramUsed " +
                "FROM Service s " +
                "JOIN UserService us ON s.id = us.id.serviceId " +
                "JOIN User u ON us.id.userId = u.id " +
                "JOIN Snapshot sn ON s.id = sn.service.id " +
                "ORDER BY s.id ASC";
        Query query = entityManager.createQuery(queryStr)
                .setFirstResult(page.getPageSize() * (page.getPageNumber() - 1))
                .setMaxResults(page.getPageSize());
        List<Service> resultList = query.getResultList();

        return new PageImpl<>(resultList, page, page.getPageSize());
    }
}
