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
            String owner;
            String apiEndpoint;
            String description;
            String project;
            String kongMapping;
            String note;
            String ip;
            String serverPort;
            Date startTime;
            String status;
            Double cpuUsed;
            Double diskUsed;
            Double gpuUsed;
            Double ramUsed;
        }

        //Query hien thi cac thong tin can thiet cua service
        String queryStr = "SELECT s.id, s.pid, s.name, " +
                "GROUP_CONCAT(DISTINCT CONCAT_WS(',', " +
                        "IFNULL(u.username, 'NULL')) " +
                //Khoang cach giua cac username
                        "SEPARATOR '; ' ) AS userInfo, " +
                "s.api_endpoint, s.description, s.project, s.kong_mapping, " +
                "s.note, sv.ip, s.server_port, s.start_time, s.status, " +
                "AVG(snapshot.cpu_used), AVG(snapshot.disk_used), AVG(snapshot.gpu_used), AVG(snapshot.ram_used) " +
                "FROM service s " +
                "JOIN user_service us ON s.id = us.service_id " +
                "JOIN user u ON us.user_id = u.id " +
                "JOIN snapshot ON s.id = snapshot.service_id " +
                "JOIN server sv ON s.server_id = sv.id " +
                "WHERE us.role = 'OWNER'" +
                "GROUP BY s.id ";

        Query query = entityManager.createNativeQuery(queryStr)
                //Sets the offset position in the result set to start pagination
                .setFirstResult(page.getPageSize() * (page.getPageNumber() - 1))
                //Sets the maximum number of entities that should be included in the page
               .setMaxResults(page.getPageSize());
        List<Object[]> resultList = query.getResultList();
        //Dung 1 List moi de key vao cho tung value
        List<ReturnedQueryResult> convertedResultList = new ArrayList<>(resultList.size());

        for(Object [] row: resultList) {
            convertedResultList.add(
                    new ReturnedQueryResult(
                            (Integer) row[0], //id
                            (String) row[1], //pid
                            (String) row[2], //name
                            (String) row[3], //owner
                            (String) row[4], //apiEndpoint
                            (String) row[5], // description
                            (String) row[6], //project
                            (String) row[7], //kongMapping
                            (String) row[8], //note
                            (String) row[9], //ip
                            (String) row[10], //serverPort
                            (Date) row[11], //startTime
                            (String) row[12], //status
                            (Double) row[13], //CPU
                            (Double) row[14], //Disk
                            (Double) row[15], //GPU
                            (Double) row[16] //Ram
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
            dto.setOwner(r.getOwner());
            dto.setApiEndpoint(r.getApiEndpoint());
            dto.setDescription(r.getDescription());
            dto.setProject(r.getProject());
            dto.setKongMapping(r.getKongMapping());
            dto.setNote(r.getNote());
            dto.setServerIp(r.getIp());
            dto.setServerPort(r.getServerPort());
            dto.setStartTime(r.getStartTime());
            dto.setStatus(r.getStatus());
            dto.setCpuUsed(r.getCpuUsed());
            dto.setDiskUsed(r.getDiskUsed());
            dto.setGpuUsed(r.getGpuUsed());
            dto.setRamUsed(r.getRamUsed());

            finalResultList.add(dto);
        }

    //Ket qua tra ve la PageImpl
        return new PageImpl<>(finalResultList, page, page.getPageSize());

    }

    @Override
    public Service showService(int serviceId) {
        //Tim kiem va hien thi thong tin service dua tren serviceId
        Service service = entityManager.find(Service.class, serviceId);
        //Ket qua tra ve la Entity
        return service;

    }

}