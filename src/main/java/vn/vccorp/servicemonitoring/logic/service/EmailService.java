package vn.vccorp.servicemonitoring.logic.service;

import java.io.File;
import java.util.List;
import java.util.Map;

public interface EmailService {

    void sendEmail(List<String> recipientList, List<String> ccList, List<String> bccList, String subject, String body, List<File> fileAttachments) throws Exception;

    String createBodyEmailFromTemplate(Map<String, Object> model, String emailTemplateName);
}
