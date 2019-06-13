package vn.vccorp.servicemonitoring.logic.service;

import vn.vccorp.servicemonitoring.entity.Service;

public interface ConfirmIssue {
    void issueResolve();
    void userConfirmIssue();
    void disableIssue();
}
