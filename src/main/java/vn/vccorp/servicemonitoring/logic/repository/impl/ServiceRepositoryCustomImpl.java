/**
 * Created by: tuyennta
 * Created on: 24/05/2019 18:49
 */

package vn.vccorp.servicemonitoring.logic.repository.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import vn.vccorp.servicemonitoring.dto.*;
import vn.vccorp.servicemonitoring.dto.ServiceDetailsDTO.*;
import vn.vccorp.servicemonitoring.dto.ServiceDetailsDTO.ServerInfo;
import vn.vccorp.servicemonitoring.dto.ServiceDetailsDTO.ServiceInfo;
import vn.vccorp.servicemonitoring.entity.IssueTracking;
import vn.vccorp.servicemonitoring.entity.Service;
import vn.vccorp.servicemonitoring.entity.Snapshot;
import vn.vccorp.servicemonitoring.logic.repository.ServiceRepositoryCustom;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.lang.invoke.SerializedLambda;
import java.math.BigInteger;
import java.time.LocalDate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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
                "WHERE us.role = 'OWNER' " +
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
    public List<UserInfo> getAllUser(int serviceId) {
            String queryStr = "SELECT \n" +
                    "    user.name, user.username, user.email, user.phone, user_service.role\n" +
                    "FROM\n" +
                    "    user\n" +
                    "        JOIN\n" +
                    "    user_service ON user.id = user_service.user_id\n" +
                    "WHERE\n" +
                    "    user_service.service_id = ?1 AND (user_service.role = 'MAINTAINER' OR user_service.role = 'OWNER')";
            Query q = entityManager.createNativeQuery(queryStr, "UserInfoMapping");
            q.setParameter(1, serviceId);
            List<UserInfo> userInfo = q.getResultList();
            return userInfo;
    }

    @Override
    public List<UserInfo> getAllOwner(int serviceId) {
        return null;
    }

    @Override
    public List<UserInfo> getAllMaintainer(int serviceId) {
        return null;
    }

    @Override
    public ServiceInfo getServiceInfo(int serviceId) {
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
        return (ServiceInfo) q.getResultList().get(0);
    }

	@Override
	public List<ServiceReportDTO> reportService(LocalDate datePre) {
		
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
            BigInteger totalWarning;
            BigInteger totalError;
        }
		
		String queryStr = "SELECT \r\n" + 
				"    s.id,\r\n" + 
				"    s.pid,\r\n" + 
				"    s.name,\r\n" + 
				"    GROUP_CONCAT(DISTINCT CONCAT_WS(',', IFNULL(u.username, 'NULL'))\r\n" + 
				"        SEPARATOR '; ') AS userInfo,\r\n" + 
				"    s.api_endpoint,\r\n" + 
				"    s.description,\r\n" + 
				"    s.project,\r\n" + 
				"    s.kong_mapping,\r\n" + 
				"    s.note,\r\n" + 
				"    sv.ip,\r\n" + 
				"    s.server_port,\r\n" + 
				"    s.start_time,\r\n" + 
				"    s.status,\r\n" + 
				"    AVG(snapshot.cpu_used),\r\n" + 
				"    AVG(snapshot.disk_used),\r\n" + 
				"    AVG(snapshot.gpu_used),\r\n" + 
				"    AVG(snapshot.ram_used),\r\n" + 
				"    e.err,\r\n" + 
				"    w.war\r\n" + 
				"FROM\r\n" + 
				"    service s\r\n" + 
				"        JOIN\r\n" + 
				"    user_service us ON s.id = us.service_id\r\n" + 
				"        JOIN\r\n" + 
				"    user u ON us.user_id = u.id\r\n" + 
				"        JOIN\r\n" + 
				"    snapshot ON s.id = snapshot.service_id\r\n" + 
				"        JOIN\r\n" + 
				"    server sv ON s.server_id = sv.id\r\n" + 
				"        LEFT OUTER JOIN\r\n" + 
				"    (SELECT \r\n" + 
				"        service_id,\r\n" + 
				"            COUNT(CASE issue_type\r\n" + 
				"                WHEN 'ERROR' THEN 1\r\n" + 
				"                ELSE NULL\r\n" + 
				"            END) AS err\r\n" + 
				"    FROM\r\n" + 
				"        issue_tracking\r\n" + 
				"    GROUP BY service_id) AS e ON e.service_id = s.id\r\n" + 
				"        LEFT OUTER JOIN\r\n" + 
				"    (SELECT \r\n" + 
				"        service_id,\r\n" + 
				"            COUNT(CASE issue_type\r\n" + 
				"                WHEN 'WARNING' THEN 1\r\n" + 
				"                ELSE NULL\r\n" + 
				"            END) AS war\r\n" + 
				"    FROM\r\n" + 
				"        issue_tracking\r\n" + 
				"    GROUP BY service_id) AS w ON w.service_id = s.id\r\n" + 
				"WHERE\r\n" + 
				"    (us.role = 'OWNER')\r\n" + 
				"        AND (snapshot.time > '" + datePre + "')\r\n" + 
				"GROUP BY s.id";
		
		Query query = entityManager.createNativeQuery(queryStr);
				
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
                            (Double) row[16], //Ram
                            (BigInteger) row[17], //Waning
                            (BigInteger) row[18] //Error
                            )
            );
        }
        
        List<ServiceReportDTO> finalResultList = new ArrayList<>();
        for(ReturnedQueryResult r: convertedResultList) {
            ServiceReportDTO dto = new ServiceReportDTO();

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
            dto.setTotalError(r.getTotalWarning());
            dto.setTotalWarning(r.getTotalError());
            finalResultList.add(dto);
        }
       
		return finalResultList;
	}
	

    @Override
    public ServerInfo getServerInfo(int serviceId) {
        String queryStr = "SELECT \n" +
                    "    server.ip,\n" +
                    "    server.name,\n" +
                    "    server.description,\n" +
                    "    server.root_path AS rootPath,\n" +
                    "    server.status\n" +
                    "FROM\n" +
                    "    server JOIN service ON server.id = service.server_id\n" +
                    "WHERE\n" +
                    "    service.id = ?1\n";
            Query q = entityManager.createNativeQuery(queryStr, "ServerInfoMapping");
            q.setParameter(1, serviceId);
            ServerInfo serverInfo = (ServerInfo) q.getResultList().get(0);
            return serverInfo;
    }

    @Override
    public PageImpl<IssueInfo> getAllIssue(int serviceId, Pageable page) {
            String queryStr = "SELECT \n" +
                    "    issue_tracking.detail,\n" +
                    "    issue_tracking.issue_type AS issueType,\n" +
                    "    issue_tracking.tracking_time AS trackingTime,\n" +
                    "    issue_tracking.user_action AS userAction,\n" +
                    "    user.name,\n" +
                    "    user.email\n" +
                    "FROM issue_tracking LEFT JOIN user ON issue_tracking.user_id = user.id\n" +
                    "WHERE issue_tracking.service_id = ?1";
            Query q = entityManager.createNativeQuery(queryStr, "IssueInfoMapping")
                .setFirstResult(page.getPageSize() * (page.getPageNumber() - 1))
                .setMaxResults(page.getPageSize());
            q.setParameter(1, serviceId);
            List<IssueInfo> issueInfo = q.getResultList();
            return new PageImpl<>(issueInfo, page, page.getPageSize());
    }

    @Override
    public PageImpl<SnapshotInfo> getAllSnapshot(int serviceId, Pageable page) {
        String queryStr = "SELECT \n" +
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
        Query q = entityManager.createNativeQuery(queryStr, "SnapshotInfoMapping")
                .setFirstResult(page.getPageSize() * (page.getPageNumber() - 1))
                .setMaxResults(page.getPageSize());
        q.setParameter(1, serviceId);
        List<SnapshotInfo> snapshotInfo = q.getResultList();
        return new PageImpl<>(snapshotInfo, page, page.getPageSize());
    }

}