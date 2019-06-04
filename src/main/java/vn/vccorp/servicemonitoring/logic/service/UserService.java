/**
 * Created by: tuyennta
 * Created on: 10/05/2019 22:04
 */

package vn.vccorp.servicemonitoring.logic.service;

import org.springframework.data.domain.Page;
import vn.vccorp.servicemonitoring.dto.UserInfoDTO;
import vn.vccorp.servicemonitoring.dto.UserDTO;
import vn.vccorp.servicemonitoring.dto.ConfigurationDTO;
import vn.vccorp.servicemonitoring.enumtype.Role;

public interface UserService {
    void addAccount(UserDTO userDTO);

    void initRootAccount();
    
    void initRootConfig();

    void updatePassword(int userId, String password);

    void deleteAccount(int deleteUserId);

    void updateRole(int userId, Role role);
    
    void updateConfig(ConfigurationDTO configurationDTO);
    
    Page<UserInfoDTO> listAllUser(int currentPage, int pageSize);
}
