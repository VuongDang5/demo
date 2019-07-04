package vn.vccorp.servicemonitoring.logic.service;

import vn.vccorp.servicemonitoring.dto.ServiceErrorDTO;
import vn.vccorp.servicemonitoring.dto.ServiceReportDTO;

import java.io.File;
import java.util.List;
import java.util.Map;

public interface EmailService {

    void sendEmail(List<String> recipientList, List<String> ccList, List<String> bccList, String subject, String body, List<File> fileAttachments) throws Exception;

    String createBodyEmailFromTemplate(Map<String, Object> model, String emailTemplateName);

    void sendServiceErrorMessage(ServiceErrorDTO serviceErrorDTO, List<String> recipients);

	void sendServiceReportMessage(List<ServiceReportDTO> serviceInfoDTO, List<String> recipients);

	void sendNotifyToNewUser(String email, String user, String password);
}
