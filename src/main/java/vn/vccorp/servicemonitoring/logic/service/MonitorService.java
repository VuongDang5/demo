/**
 * Created by: tuyennta
 * Created on: 20/05/2019 10:14
 */

package vn.vccorp.servicemonitoring.logic.service;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;
import vn.vccorp.servicemonitoring.dto.LogServiceDTO;
import vn.vccorp.servicemonitoring.dto.ServiceDTO;
import vn.vccorp.servicemonitoring.entity.Service;
import vn.vccorp.servicemonitoring.entity.UserService;
import vn.vccorp.servicemonitoring.enumtype.Role;

import java.io.IOException;
import java.util.List;

public interface MonitorService {
    void registerService(ServiceDTO serviceDTO);

    void startService(int serviceId);

    void stopService(int serviceId);
    
    void deleteLog(int id);

    Page<Service> showAllService(int currentPage, int pageSize);

    Service showService(int serviceId);
    
    Page<UserService> showServiceOwners(int currentPage, int pageSize, Integer serviceId);
    
    void changeRoleServiceOwner(int userId,int serviceId, Role role);
    
    void addServiceOwner(int userId,int serviceId, Role role);

    List<String> getLogService(LogServiceDTO logServiceDTO);

    void deployService(ServiceDTO serviceDTO, Integer currentUserId,
                       MultipartFile jar,
                       MultipartFile originalJar,
                       MultipartFile dependencies,
                       MultipartFile modelFile,
                       MultipartFile sourceCode,
                       MultipartFile dockerFile) throws IOException;
}
