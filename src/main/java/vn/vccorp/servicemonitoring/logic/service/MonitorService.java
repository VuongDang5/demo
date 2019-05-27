/**
 * Created by: tuyennta
 * Created on: 20/05/2019 10:14
 */

package vn.vccorp.servicemonitoring.logic.service;

import vn.vccorp.servicemonitoring.dto.LogServiceDTO;
import vn.vccorp.servicemonitoring.dto.ServiceDTO;

import java.util.List;

public interface MonitorService {
    void registerService(ServiceDTO serviceDTO);

    void startService(int serviceId);

    void stopService(int serviceId);

    List<String> getLogService(LogServiceDTO logServiceDTO);
}
