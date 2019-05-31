/**
 * Created by: tuyennta
 * Created on: 10/05/2019 22:04
 */

package vn.vccorp.servicemonitoring.logic.service;

import vn.vccorp.servicemonitoring.dto.ListDTO;
import vn.vccorp.servicemonitoring.dto.UserDTO;
import vn.vccorp.servicemonitoring.entity.User;
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
    
List<ListDTO> listAllUser();
}
