/**
 * Created by: tuyennta
 * Created on: 10/05/2019 22:04
 */

package vn.vccorp.servicemonitoring.logic.service;

import vn.vccorp.servicemonitoring.dto.UserDTO;
import vn.vccorp.servicemonitoring.enumtype.Role;

public interface UserService {
    void addAccount(UserDTO userDTO);

    void initRootAccount();

    void updatePassword(int userId, String password);

    void deleteAccount(int deleteUserId);

    void updateRole(int userId, String role);
}
