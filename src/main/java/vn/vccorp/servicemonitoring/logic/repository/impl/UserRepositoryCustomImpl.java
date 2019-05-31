package vn.vccorp.servicemonitoring.logic.repository.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.collections.ArrayStack;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import vn.vccorp.servicemonitoring.dto.ListDTO;
import vn.vccorp.servicemonitoring.dto.ServerInfo;
import vn.vccorp.servicemonitoring.dto.ServiceInfo;
import vn.vccorp.servicemonitoring.dto.UserServiceDTO;
import vn.vccorp.servicemonitoring.entity.User;
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

        //Query hien thi cac thong tin can thiet cua service
        String queryStr = "SELECT u.name, u.username, u.email, u.phone, u.id, " +
                "GROUP_CONCAT( " +
                "CONCAT_WS(',', IFNULL(service.name, 'null'), IFNULL(service.pid, 'null'), IFNULL(user_service.role, 'null'), IFNULL(service.description, 'null'), IFNULL(service.status, 'null')) " +
                "ORDER BY service.pid " +
                "SEPARATOR ';' " +
                ") AS serviceInfo, " +
                "GROUP_CONCAT( " +
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

        Query query = entityManager.createNativeQuery(queryStr, "userDetailsMapping")
                //Sets the offset position in the result set to start pagination
                .setFirstResult(page.getPageSize() * (page.getPageNumber() - 1))
                //Sets the maximum number of entities that should be included in the page
                .setMaxResults(page.getPageSize());
        List<UserServiceDTO> resultList = query.getResultList();
        List<ListDTO> list = new ArrayList<>();
        /*for(UserServiceDTO r: resultList) {
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
                list.add(dto);
        }*/

        //Ket qua tra ve la PageImpl
        return new PageImpl<>(list, page, page.getPageSize());
    }
}
