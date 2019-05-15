/**
 * Created by: tuyennta
 * Created on: 10/05/2019 22:04
 */

package vn.vccorp.servicemonitoring.logic.service;

import vn.vccorp.servicemonitoring.dto.UserDTO;

public interface AccountService {
    void addAccount(UserDTO userDTO);

    void initRootAccount();

    void updatePassword(int userId, String password);
}
