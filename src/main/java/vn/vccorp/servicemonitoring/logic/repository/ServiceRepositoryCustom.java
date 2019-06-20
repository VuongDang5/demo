/**
 * Created by: tuyennta
 * Created on: 24/05/2019 18:48
 */

package vn.vccorp.servicemonitoring.logic.repository;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import vn.vccorp.servicemonitoring.dto.ServiceDetailsDTO.*;
import vn.vccorp.servicemonitoring.dto.ServiceInfoDTO;

import vn.vccorp.servicemonitoring.dto.ServiceReportDTO;

import vn.vccorp.servicemonitoring.entity.IssueTracking;
import vn.vccorp.servicemonitoring.entity.Snapshot;


import java.time.LocalDate;
import java.util.List;

public interface ServiceRepositoryCustom {
    PageImpl<ServiceInfoDTO> showAllService(Pageable firstPageWithFourElements);

    List<UserInfo> getAllUser(int serviceId);
    List<UserInfo> getAllOwner(int serviceId);
    List<UserInfo> getAllMaintainer(int serviceId);


	List<ServiceReportDTO> reportService(LocalDate datePre);


    ServiceInfo getServiceInfo(int serviceId);
    ServerInfo getServerInfo(int serviceId);
    PageImpl<IssueInfo> getAllIssue(int serviceId, Pageable page);
    PageImpl<SnapshotInfo> getAllSnapshot(int serviceId, Pageable page);

}
