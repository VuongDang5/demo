/**
 * Created by: tuyennta
 * Created on: 10/05/2019 22:04
 */

package vn.vccorp.servicemonitoring.logic.service;

import vn.vccorp.servicemonitoring.dto.AccountDTO;

public interface AccountService {
    void addAccount(AccountDTO accountDTO);

    void initRootAccount();
}
