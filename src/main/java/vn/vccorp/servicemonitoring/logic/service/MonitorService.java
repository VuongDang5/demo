/**
 * Created by: tuyennta
 * Created on: 20/05/2019 10:14
 */

package vn.vccorp.servicemonitoring.logic.service;

import vn.vccorp.servicemonitoring.dto.LogServiceDTO;
import org.springframework.data.domain.Page;
import vn.vccorp.servicemonitoring.dto.ServiceDTO;
import vn.vccorp.servicemonitoring.entity.Service;

import java.util.List;

import java.util.List;

public interface MonitorService {
    void registerService(ServiceDTO serviceDTO);

	void startService(int serviceId);

    void stopService(int serviceId);

    Page<Service> showAllService(int currentPage, int pageSize);

    Service showService(int serviceId);
    List<String> getLogService(LogServiceDTO logServiceDTO);
}
