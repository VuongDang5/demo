/**
 * Created by: tuyennta
 * Created on: 20/05/2019 10:14
 */

package vn.vccorp.servicemonitoring.logic.service;

import vn.vccorp.servicemonitoring.dto.ServiceDTO;

public interface MonitorService {
    void registerService(ServiceDTO serviceDTO);

    Object showAllService();

    Object showService(int id);
}
