/**
 * Created by: tuyennta
 * Created on: 24/05/2019 18:49
 */

package vn.vccorp.servicemonitoring.logic.repository.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import vn.vccorp.servicemonitoring.dto.*;
import vn.vccorp.servicemonitoring.entity.Service;
import vn.vccorp.servicemonitoring.entity.UserService;
import vn.vccorp.servicemonitoring.enumtype.Role;
import vn.vccorp.servicemonitoring.enumtype.Status;
import vn.vccorp.servicemonitoring.logic.repository.ServiceRepositoryCustom;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.lang.invoke.SerializedLambda;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class ServiceRepositoryCustomImpl implements ServiceRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public PageImpl<ServiceInfoDTO> showAllService(Pageable page) {

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        class ReturnedQueryResult {
            Integer id;
            String pid;
            String name;
            String apiEndpoint;
            String description;
            String project;
            String kongMapping;
            String note;
            String ip;
            String serverPort;
            Date startTime;
            Status status;
            String nameUser;
            String email;
            Role role;
            Date time;
            Float cpuUsed;
            Float diskUsed;
            Float gpuUsed;
            Float ramUsed;
        }

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
        List<Object[]> resultList = query.getResultList();

        //Dung 1 List moi de key vao cho tung value
        List<ReturnedQueryResult> convertedResultList = new ArrayList<>(resultList.size());
        for(Object [] row: resultList) {
            convertedResultList.add(
                    new
                            ReturnedQueryResult(
                            (Integer) row[0],
                            (String) row[1],
                            (String) row[2],
                            (String) row[3],
                            (String) row[4],
                            (String) row[5],
                            (String) row[6],
                            (String) row[7],
                            (String) row[8],
                            (String) row[9],
                            (Date) row[10],
                            (Status) row[11],
                            (String) row[12],
                            (String) row[13],
                            (Role) row[14],
                            (Date) row[15],
                            (Float) row[16],
                            (Float) row[17],
                            (Float) row[18],
                            (Float) row[19]
                            )
            );
        }

        //Set Key cho tung gia tri
        List<ServiceInfoDTO> finalResultList = new ArrayList<>();
        for(ReturnedQueryResult r: convertedResultList) {
            ServiceInfoDTO dto = new ServiceInfoDTO();
            dto.setId(r.getId());
            dto.setPid(r.getPid());
            dto.setServiceName(r.getName());
            dto.setApiEndpoint(r.getApiEndpoint());
            dto.setDescription(r.getDescription());
            dto.setProject(r.getProject());
            dto.setKongMapping(r.getKongMapping());
            dto.setNote(r.getNote());
            dto.setServerIp(r.getIp());
            dto.setServerPort(r.getServerPort());
            dto.setStartTime(r.getStartTime());
            dto.setStatus(r.getStatus());
            dto.setUserName(r.getNameUser());
            dto.setEmail(r.getEmail());
            dto.setRole(r.getRole());
            dto.setTime(r.getTime());
            dto.setCpuUsed(r.getCpuUsed());
            dto.setDiskUsed(r.getDiskUsed());
            dto.setGpuUsed(r.getGpuUsed());
            dto.setRamUsed(r.getRamUsed());
            finalResultList.add(dto);
        }

        return new PageImpl<>(finalResultList, page, page.getPageSize());


        //Ket qua tra ve la PageImpl
        //return new //PageImpl<>(resultList, page, page.getPageSize());
    }


    @Override
    public Service showService(int serviceId) {
        //Tim kiem va hien thi thong tin service dua tren serviceId
        Service service = entityManager.find(Service.class, serviceId);
        //Ket qua tra ve la Entity
        return service;
    }

}