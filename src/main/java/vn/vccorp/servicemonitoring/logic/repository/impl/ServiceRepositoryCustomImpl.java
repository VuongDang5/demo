/**
 * Created by: tuyennta
 * Created on: 24/05/2019 18:49
 */

package vn.vccorp.servicemonitoring.logic.repository.impl;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import vn.vccorp.servicemonitoring.entity.Service;
import vn.vccorp.servicemonitoring.entity.UserService;
import vn.vccorp.servicemonitoring.logic.repository.ServiceRepositoryCustom;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.lang.invoke.SerializedLambda;
import java.util.List;

public class ServiceRepositoryCustomImpl implements ServiceRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public PageImpl<Service> showAllService(Pageable page) {
        //Query hien thi cac thong tin can thiet cua service
        String queryStr = "SELECT s.id, s.pid, s.name, s.apiEndpoint, s.description, s.project, s.kongMapping, " +
                "s.note, sv.ip, s.serverPort, s.startTime, s.status, " +
                "u.name, u.email, " +
                "us.role, " +
                "sn.time, sn.cpuUsed, sn.diskUsed, sn.gpuUsed, sn.ramUsed " +
                "FROM Service s " +
                //Ghep cac table lai voi nhau. Neu thanh phan trong table co dau gach duoi thi thay bang dau cham
                "JOIN UserService us ON s.id = us.id.serviceId " +
                "JOIN User u ON us.id.userId = u.id " +
                "JOIN Snapshot sn ON s.id = sn.service.id " +
                "JOIN Server sv ON s.server.id = sv.id ";

        Query query = entityManager.createQuery(queryStr)
                //Sets the offset position in the result set to start pagination
                .setFirstResult(page.getPageSize() * (page.getPageNumber() - 1))
                //Sets the maximum number of entities that should be included in the page
                .setMaxResults(page.getPageSize());
        List<Service> resultList = query.getResultList();
        //Ket qua tra ve la PageImpl
        return new PageImpl<>(resultList, page, page.getPageSize());
    }

    @Override
    public Service showService(int serviceId) {
        //Tim kiem va hien thi thong tin service dua tren serviceId
        Service service = entityManager.find(Service.class, serviceId);
        //Ket qua tra ve la Entity
        return service;
    }
    
    @Override
    public PageImpl<UserService> showServiceOwners(Pageable page, Integer serviceId) {
        //Query hien thi cac thong tin can thiet cua service
        String queryStr = "SELECT u.email, u.username, u.name, u.phone, us.role " +
                "FROM User u " +
                //Ghep cac table lai voi nhau. Neu thanh phan trong table co dau gach duoi thi thay bang dau cham
                "JOIN UserService us ON u.id = us.user.id " +
                "WHERE us.service.id = " + serviceId.toString();
        Query query = entityManager.createQuery(queryStr)
                //Sets the offset position in the result set to start pagination
                .setFirstResult(page.getPageSize() * (page.getPageNumber() - 1))
                //Sets the maximum number of entities that should be included in the page
                .setMaxResults(page.getPageSize());
        List<UserService> resultList = query.getResultList();
        //Ket qua tra ve la PageImpl
        return new PageImpl<>(resultList, page, page.getPageSize());
    }

}