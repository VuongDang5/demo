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

import vn.vccorp.servicemonitoring.dto.*;
import vn.vccorp.servicemonitoring.dto.ServiceDetailsDTO.*;
import vn.vccorp.servicemonitoring.dto.ServiceDetailsDTO.ServerInfo;
import vn.vccorp.servicemonitoring.dto.ServiceDetailsDTO.ServiceInfo;
import vn.vccorp.servicemonitoring.entity.Service;
import vn.vccorp.servicemonitoring.logic.repository.ServiceRepositoryCustom;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.ArrayList;
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
    public ServiceDetailsDTO showService(int serviceId) {
        //Tim kiem va hien thi thong tin service dua tren serviceId
        Service service = entityManager.find(Service.class, serviceId);
        ServiceDetailsDTO finalResult = new ServiceDetailsDTO();
        String queryStr = "SELECT \n" +
                "    service.name,\n" +
                "    service.description,\n" +
                "    service.server_port AS serverPort,\n" +
                "    service.pid,\n" +
                "    service.deploy_dir AS deployDir,\n" +
                "    service.log_dir AS logDir,\n" +
                "    service.log_file AS logFile,\n" +
                "    service.language,\n" +
                "    service.deploy_command AS deployCommand,\n" +
                "    service.ram_limit AS ramLimit,\n" +
                "    service.cpu_limit AS cpuLimit,\n" +
                "    service.gpu_limit AS gpuLimit,\n" +
                "    service.disk_limit AS diskLimit,\n" +
                "    service.status,\n" +
                "    service.start_time AS startTime,\n" +
                "    service.last_check_time AS lastCheckTime,\n" +
                "    service.project,\n" +
                "    service.api_endpoint AS apiEndpoint,\n" +
                "    service.kong_mapping AS kongMapping,\n" +
                "    service.note\n" +
                "FROM\n" +
                "    service\n" +
                "WHERE\n" +
                "    service.id = ?1";
        Query q = entityManager.createNativeQuery(queryStr, "ServiceInfoMapping");
        q.setParameter(1, serviceId);
        ServiceInfo serviceInfo = (ServiceInfo) q.getResultList().get(0);
        if(serviceInfo!=null) {
            //Server info
            queryStr = "SELECT \n" +
                    "    server.ip,\n" +
                    "    server.name,\n" +
                    "    server.description,\n" +
                    "    server.root_path AS rootPath,\n" +
                    "    server.status\n" +
                    "FROM\n" +
                    "    server JOIN service ON server.id = service.server_id\n" +
                    "WHERE\n" +
                    "    service.id = ?1\n";
            q = entityManager.createNativeQuery(queryStr, "ServerInfoMapping");
            q.setParameter(1, serviceId);
            ServerInfo serverInfo = (ServerInfo) q.getResultList().get(0);
            //User info
            queryStr = "SELECT \n" +
                    "    user.name, user.username, user.email, user.phone, user_service.role\n" +
                    "FROM\n" +
                    "    user\n" +
                    "        JOIN\n" +
                    "    user_service ON user.id = user_service.user_id\n" +
                    "WHERE\n" +
                    "    user_service.service_id = ?1 AND (user_service.role = 'MAINTAINER' OR user_service.role = 'OWNER')";
            q = entityManager.createNativeQuery(queryStr, "UserInfoMapping");
            q.setParameter(1, serviceId);
            List<UserInfo> userInfo = q.getResultList();
            //Snapshot info
            queryStr = "SELECT \n" +
                    "    snapshot.time,\n" +
                    "    snapshot.ram_free AS ramFree,\n" +
                    "    snapshot.ram_used AS ramUsed,\n" +
                    "    snapshot.cpu_free AS cpuFree,\n" +
                    "    snapshot.cpu_used AS cpuUsed,\n" +
                    "    snapshot.gpu_free AS gpuFree,\n" +
                    "    snapshot.gpu_used AS gpuUsed,\n" +
                    "    snapshot.disk_free AS diskFree,\n" +
                    "    snapshot.disk_used AS diskUsed\n" +
                    "FROM\n" +
                    "    snapshot\n" +
                    "WHERE\n" +
                    "    snapshot.service_id = ?1";
            q = entityManager.createNativeQuery(queryStr, "SnapshotInfoMapping");
            q.setParameter(1, serviceId);
            List<SnapshotInfo> snapshotInfo = q.getResultList();
            //Issue info
            queryStr = "SELECT \n" +
                    "    issue_tracking.detail,\n" +
                    "    issue_tracking.issue_type AS issueType,\n" +
                    "    issue_tracking.tracking_time AS trackingTime,\n" +
                    "    issue_tracking.user_action AS userAction,\n" +
                    "    user.name,\n" +
                    "    user.email\n" +
                    "FROM issue_tracking LEFT JOIN user ON issue_tracking.user_id = user.id\n" +
                    "WHERE issue_tracking.service_id = ?1";
            q = entityManager.createNativeQuery(queryStr, "IssueInfoMapping");
            q.setParameter(1, serviceId);
            List<IssueInfo> issueInfo = q.getResultList();

            finalResult.setName(serviceInfo.getName());
            finalResult.setDescription(serviceInfo.getDescription());
            finalResult.setServerPort(serviceInfo.getServerPort());
            finalResult.setPid(serviceInfo.getPid());
            finalResult.setDeployDir(serviceInfo.getDeployDir());
            finalResult.setLogDir(serviceInfo.getLogDir());
            finalResult.setLogFile(serviceInfo.getLogFile());
            finalResult.setLanguage(serviceInfo.getLanguage());
            finalResult.setDeployCommand(serviceInfo.getDeployCommand());
            finalResult.setRamLimit(serviceInfo.getRamLimit());
            finalResult.setCpuLimit(serviceInfo.getCpuLimit());
            finalResult.setGpuLimit(serviceInfo.getGpuLimit());
            finalResult.setDiskLimit(serviceInfo.getDiskLimit());
            finalResult.setStatus(serviceInfo.getStatus());
            finalResult.setStartTime(serviceInfo.getStartTime());
            finalResult.setLastCheckTime(serviceInfo.getLastCheckTime());
            finalResult.setProject(serviceInfo.getProject());
            finalResult.setApiEndpoint(serviceInfo.getApiEndpoint());
            finalResult.setKongMapping(serviceInfo.getKongMapping());
            finalResult.setNote(serviceInfo.getNote());

            finalResult.setServerInfo(serverInfo);
            finalResult.setUserInfo(userInfo);
            finalResult.setSnapshotInfo(snapshotInfo);
            finalResult.setIssueInfo(issueInfo);
        }
        //Ket qua tra ve la Entity
        return finalResult;
    }
}