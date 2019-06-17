package vn.vccorp.servicemonitoring.logic.service;

import vn.vccorp.servicemonitoring.dto.IssueTrackingDTO;
import vn.vccorp.servicemonitoring.dto.ServiceDTO;

import java.util.Date;
import java.util.List;

public interface ConfirmIssue {
    List<IssueTrackingDTO> getAllIssueError();

    List<ServiceDTO> getAllServiceError();

    void userConfirmIssue(int serviceId);

    void disableIssue(int serviceId, String date);
}
