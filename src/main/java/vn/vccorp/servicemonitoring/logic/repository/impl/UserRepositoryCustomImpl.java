package vn.vccorp.servicemonitoring.logic.repository.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import vn.vccorp.servicemonitoring.dto.UserInfoDTO;
import vn.vccorp.servicemonitoring.dto.ServerInfo;
import vn.vccorp.servicemonitoring.dto.ServiceInfo;
import vn.vccorp.servicemonitoring.logic.repository.UserRepositoryCustom;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class UserRepositoryCustomImpl implements UserRepositoryCustom {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public PageImpl<UserInfoDTO> getAllUser(Pageable page) {
        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        class ReturnedQueryResult {
            String name;
            String username;
            String email;
            String phone;
            String services;
            String servers;
        }

        String queryStr = "SELECT \n" +
                "    u.name,\n" +
                "    u.username,\n" +
                "    u.email,\n" +
                "    u.phone,\n" +
                "    GROUP_CONCAT(DISTINCT CONCAT_WS(',,',\n" +
                "               IFNULL(service.name, 'NULL'),\n" +
                "               IFNULL(service.pid, 'NULL'),\n" +
                "               IFNULL(user_service.role, 'NULL'),\n" +
                "               IFNULL(service.description, 'NULL'),\n" +
                "               IFNULL(service.status, 'NULL'))\n" +
                "            ORDER BY service.pid\n" +
                "            SEPARATOR ';;') AS services,\n" +
                "    GROUP_CONCAT(DISTINCT CONCAT_WS(',,',\n" +
                "                IFNULL(server.name, 'NULL'),\n" +
                "                IFNULL(server.ip, 'NULL'),\n" +
                "                IFNULL(user_server.groups, 'NULL'),\n" +
                "                IFNULL(server.description, 'NULL'),\n" +
                "                IFNULL(server.status, 'NULL'))\n" +
                "        ORDER BY server.ip\n" +
                "        SEPARATOR ';;') AS servers\n" +
                "FROM\n" +
                "    user u\n" +
                "        LEFT JOIN\n" +
                "    user_service ON u.id = user_service.user_id\n" +
                "       AND (user_service.role = 'MAINTAINER'" +
                "       OR user_service.role = 'OWNER')" +
                "        LEFT JOIN\n" +
                "    service ON service.id = user_service.service_id\n" +
                "        JOIN\n" +
                "    user_server ON u.id = user_server.user_id\n" +
                "        JOIN\n" +
                "    server ON server.id = user_server.server_id\n" +
                "GROUP BY u.name , u.username , u.email , u.phone\n";

        Query query = entityManager.createNativeQuery(queryStr)
                //Sets the offset position in the result set to start pagination
                .setFirstResult(page.getPageSize() * (page.getPageNumber() - 1))
                //Sets the maximum number of entities that should be included in the page
                .setMaxResults(page.getPageSize());
        List<Object[]> resultList = query.getResultList();
        List<UserInfoDTO> finalResultList = new ArrayList<>();
        if (!resultList.isEmpty()) {
            List<ReturnedQueryResult> convertedResultList = new ArrayList<>(resultList.size());
            //Unwind the list returned from query, convert each obj into returnQueryResult and add them into a list
            for (Object[] row : resultList) {
                convertedResultList.add(
                        new
                                ReturnedQueryResult(
                                (String) row[0], // name
                                (String) row[1], // username
                                (String) row[2], // email
                                (String) row[3], // phone
                                (String) row[4], // services
                                (String) row[5]  // servers
                        )
                );
            }

            for (ReturnedQueryResult r : convertedResultList) {
                UserInfoDTO dto = new UserInfoDTO();

                //user info
                dto.setName(r.getName());
                dto.setUsername(r.getUsername());
                dto.setEmail(r.getEmail());
                dto.setPhone(r.getPhone());

                //user server info
                List<ServerInfo> serverInfoList = new ArrayList<>();
                if (!r.getServers().equals("NULL,,NULL,,NULL,,NULL,,NULL")) {
                    List<String> serverList = Arrays.asList(r.getServers().split(";;", -1));
                    for (String s : serverList) {
                        ServerInfo serverInfo = new ServerInfo();
                        List<String> info = Arrays.asList(s.split(",,", -1));
                        serverInfo.setName(getReplaceNullString(info, 0));
                        serverInfo.setIp(getReplaceNullString(info, 1));
                        serverInfo.setGroups(getReplaceNullString(info, 2));
                        serverInfo.setDescription(getReplaceNullString(info, 3));
                        serverInfo.setStatus(getReplaceNullString(info, 4));
                        serverInfoList.add(serverInfo);
                    }
                }

                //user service info
                List<ServiceInfo> serviceInfoList = new ArrayList<>();
                if (!r.getServices().equals("NULL,,NULL,,NULL,,NULL,,NULL")) {
                    List<String> serviceList = Arrays.asList(r.getServices().split(";;", -1));
                    for (String s : serviceList) {
                        List<String> info = Arrays.asList(s.split(",,", -1));
                        ServiceInfo serviceInfo = new ServiceInfo();
                        serviceInfo.setName(getReplaceNullString(info, 0));
                        serviceInfo.setPid(getReplaceNullString(info, 1));
                        serviceInfo.setRole(getReplaceNullString(info, 2));
                        serviceInfo.setDescription(getReplaceNullString(info, 3));
                        serviceInfo.setStatus(getReplaceNullString(info, 4));
                        serviceInfoList.add(serviceInfo);
                    }
                }

                dto.setServers(serverInfoList);
                dto.setServices(serviceInfoList);
                finalResultList.add(dto);
            }
        }
        return new PageImpl<>(finalResultList, page, page.getPageSize());
    }

    private String getReplaceNullString(List<String> list, int index) {
        String s = list.get(index);
        return s.equalsIgnoreCase("null")?null:s;
    }
}