package vn.vccorp.servicemonitoring.logic.repository.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import vn.vccorp.servicemonitoring.dto.ListDTO;
import vn.vccorp.servicemonitoring.dto.ServerInfo;
import vn.vccorp.servicemonitoring.dto.ServiceInfo;
import vn.vccorp.servicemonitoring.logic.repository.UserRepositoryCustom;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UserRepositoryCustomImpl implements UserRepositoryCustom {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public PageImpl<ListDTO> getAllUser(Pageable page) {

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        class returnedQueryResult {
            String name;
            String username;
            String email;
            String phone;
            String services;
            String servers;
        }

        String queryStr = "SELECT u.name, u.username, u.email, u.phone, u.id, " +
                "GROUP_CONCAT( " +
                "CONCAT_WS(',', IFNULL(service.name, 'null'), IFNULL(service.pid, 'null'), IFNULL(user_service.role, 'null'), IFNULL(service.description, 'null'), IFNULL(service.status, 'null')) " +
                "ORDER BY service.pid " +
                "SEPARATOR ';' " +
                ") AS serviceInfo, " +
                "GROUP_CONCAT(DISTINCT " +
                "CONCAT_WS(',', IFNULL(server.name, 'null'), IFNULL(server.ip, 'null'), IFNULL(user_server.groups, 'null'), IFNULL(server.description, 'null'), IFNULL(server.status, 'null')) " +
                "ORDER BY server.ip " +
                "SEPARATOR ';' " +
                ") AS serverInfo " +
                "FROM user u " +
                "JOIN user_service ON u.id = user_service.user_id " +
                "JOIN service ON service.id = user_service.service_id " +
                "JOIN user_server ON (u.id = user_server.user_id AND service.server_id = user_server.server_id) " +
                "JOIN server ON server.id = user_server.server_id " +
                "WHERE user_service.role = 'OWNER' " +
                "OR user_service.role = 'MAINTAINER' ";

        Query query = entityManager.createNativeQuery(queryStr)
                //Sets the offset position in the result set to start pagination
                .setFirstResult(page.getPageSize() * (page.getPageNumber() - 1))
                //Sets the maximum number of entities that should be included in the page
                .setMaxResults(page.getPageSize());
        List<Object[]> resultList = query.getResultList();
        List<returnedQueryResult> convertedResultList = new ArrayList<>(resultList.size());

        //Unwind the list returned from query, convert each obj into returnQueryResult and add them into a list
        for(Object [] row: resultList) {
            convertedResultList.add(
                    new
                    returnedQueryResult(
                            (String) row[0], // name
                            (String) row[1], // username
                            (String) row[2], // email
                            (String) row[3], // phone
                            (String) row[5], // services
                            (String) row[6]  // servers
                    )
            );
        }

        List<ListDTO> finalResultList = new ArrayList<>();
        for(returnedQueryResult r: convertedResultList) {
                ListDTO dto = new ListDTO();
                List<ServiceInfo> serviceInfoList = new ArrayList<>();
                List<ServerInfo> serverInfoList = new ArrayList<>();

                dto.setName(r.getName());
                dto.setUsername(r.getUsername());
                dto.setEmail(r.getEmail());
                dto.setPhone(r.getPhone());

                List<String> serviceList = Arrays.asList(r.getServices().split(";", -1));
                List<String> serverList = Arrays.asList(r.getServers().split(";", -1));

                for(String s: serviceList) {
                    ServiceInfo serviceInfo = new ServiceInfo();
                    List<String> info = Arrays.asList(s.split(",",-1));
                    serviceInfo.setName(info.get(0));
                    serviceInfo.setPid(info.get(1));
                    serviceInfo.setRole(info.get(2));
                    serviceInfo.setDescription(info.get(3));
                    serviceInfo.setStatus(info.get(4));
                    serviceInfoList.add(serviceInfo);
                }

                for(String s: serverList) {
                    ServerInfo serverInfo = new ServerInfo();
                    List<String> info = Arrays.asList(s.split(",", -1));
                    serverInfo.setName(info.get(0));
                    serverInfo.setIp(info.get(1));
                    serverInfo.setGroups(info.get(2));
                    serverInfo.setDescription(info.get(3));
                    serverInfo.setStatus(info.get(4));
                    serverInfoList.add(serverInfo);
                }

                dto.setServices(serviceInfoList);
                dto.setServers(serverInfoList);
                finalResultList.add(dto);
        }

        //Ket qua tra ve la PageImpl
        return new PageImpl<>(finalResultList, page, page.getPageSize());
    }
}
