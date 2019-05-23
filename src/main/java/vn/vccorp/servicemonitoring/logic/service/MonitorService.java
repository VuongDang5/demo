/**
 * Created by: tuyennta
 * Created on: 20/05/2019 10:14
 */

package vn.vccorp.servicemonitoring.logic.service;

import org.springframework.data.domain.Page;
import vn.vccorp.servicemonitoring.dto.ServiceDTO;
import vn.vccorp.servicemonitoring.entity.Service;

import java.util.List;

public interface MonitorService {
    void registerService(ServiceDTO serviceDTO);

    Page<Service> showAllService(int pageId);

    Service showService(int serviceId);
}
