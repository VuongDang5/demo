package vn.vccorp.servicemonitoring.logic.service.impl;

import freemarker.ext.beans.BeansWrapper;
import freemarker.template.Configuration;
import org.apache.commons.validator.routines.EmailValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import vn.vccorp.servicemonitoring.config.EmailConfig;
import vn.vccorp.servicemonitoring.config.GMailAuthenticator;
import vn.vccorp.servicemonitoring.dto.ServiceErrorDTO;
import vn.vccorp.servicemonitoring.dto.ServiceReportDTO;
import vn.vccorp.servicemonitoring.logic.service.EmailService;
import vn.vccorp.servicemonitoring.message.Messages;

import javax.mail.*;
import javax.mail.internet.*;
import java.io.File;
import java.util.*;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailServiceImpl.class);

    @Autowired
    private EmailConfig emailConfig;

    @Autowired
    private Configuration freemarkerConfiguration;
    @Autowired
    private Messages messages;

    @Override
    public String createBodyEmailFromTemplate(Map<String, Object> model, String emailTemplateName) {
        return geFreeMarkerTemplateContent(model, emailTemplateName);
    }

    @Override
    public void sendServiceErrorMessage(ServiceErrorDTO serviceErrorDTO, List<String> recipients) {
        Map<String, Object> model = new HashMap<>();
        model.put("service", serviceErrorDTO);
        model.put("statics", BeansWrapper.getDefaultInstance().getStaticModels());
        String body = createBodyEmailFromTemplate(model, "healthcheck-service-error-template.ftl");
        try {
            sendEmail(recipients, null, null,
                    messages.get("service.error.report-title", new String[]{serviceErrorDTO.getServiceName(), serviceErrorDTO.getDeployedServer()}),
                    body, null);
        } catch (Exception e) {
            LOGGER.error("Exception while sending warning message");
        }
    }
    
    @Override
    public void sendServiceReportMessage(List<ServiceReportDTO> serviceReportDTO, List<String> recipients) {
    	Map<String, Object> map = new HashMap<String, Object>(); 
    	map.put("serviceInfo", serviceReportDTO);
        String body = createBodyEmailFromTemplate(map, "frequently-report.ftl");
        try {
            sendEmail(recipients, null, null, messages.get("Frequently report for Admin"), body, null);
        } catch (Exception e) {
            LOGGER.error("Exception while sending warning message");
        }
    }

    @Override
    public void sendNotifyToNewUser(String email, String user, String password) {
        Map<String, Object> map = new HashMap<>();
        map.put("user", user);
        map.put("email", email);
        map.put("password", password);
        String body = createBodyEmailFromTemplate(map, "new-user-notify.ftl");
        try {
            sendEmail(Collections.singletonList(email), null, null, "Service Monitoring account info", body, null);
        } catch (Exception e) {
            LOGGER.error("Exception while sending warning message");
        }
    }

    @Override
    public void sendEmail(List<String> recipientList, List<String> ccList, List<String> bccList, String subject, String body, List<File> fileAttachments) throws Exception {
        if (CollectionUtils.isEmpty(recipientList) && CollectionUtils.isEmpty(ccList) && CollectionUtils.isEmpty(bccList)) {
            throw new Exception("Recipient is missing");
        }
        if (body == null) {
            throw new Exception("Body is missing");
        }
        if (subject == null) {
            throw new Exception("Subject is missing");
        }
        boolean authFlag = (!StringUtils.isEmpty(emailConfig.getAuthentication())) ? Boolean.valueOf(emailConfig.getAuthentication()) : false;
        Properties properties = createPropertiesMail(emailConfig.getHost(), emailConfig.getPort(), authFlag);
        Session session = Session.getInstance(properties, new GMailAuthenticator(emailConfig.getUser(), emailConfig.getPwd()));
        MimeMessage message = new MimeMessage(session);
        message.setSubject(subject);
        message.setFrom(new InternetAddress(emailConfig.getSender()));
        addRecipient(message, recipientList, ccList, bccList);
        addBodyAndAttachmentToMail(message, body, fileAttachments);
        send(message, session, authFlag);
        LOGGER.info("Sent message successfully....");
    }

    private void addRecipient(MimeMessage message, List<String> recipientList, List<String> ccList, List<String> bccList) throws Exception {
        if (recipientList != null) {
            for (String email : recipientList) {
                if (!StringUtils.isEmpty(email) && EmailValidator.getInstance().isValid(email)) {
                    message.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
                } else {
                    LOGGER.error("System does not recognize the email address '" + email + "'");
                }
            }
        }
        if (ccList != null) {
            for (String email : ccList) {
                if (!StringUtils.isEmpty(email) && EmailValidator.getInstance().isValid(email)) {
                    message.addRecipient(Message.RecipientType.CC, new InternetAddress(email));
                } else {
                    LOGGER.error("System does not recognize the email address '" + email + "'");
                }
            }
        }
        if (bccList != null) {
            for (String email : bccList) {
                if (!StringUtils.isEmpty(email) && EmailValidator.getInstance().isValid(email)) {
                    message.addRecipient(Message.RecipientType.BCC, new InternetAddress(email));
                } else {
                    LOGGER.error("System does not recognize the email address '" + email + "'");
                }
            }
        }
    }

    private void addBodyAndAttachmentToMail(MimeMessage message, String body, List<File> filePaths) throws Exception {
        Multipart msgMultipart = new MimeMultipart();
        BodyPart htmlMessagePart = new MimeBodyPart();
        htmlMessagePart.setContent(body, "text/html; charset=utf-8");
        msgMultipart.addBodyPart(htmlMessagePart);
        if (filePaths != null && filePaths.size() > 0) {
            for (File filePath : filePaths) {
                MimeBodyPart messageAttachment = new MimeBodyPart();
                messageAttachment.attachFile(filePath);
                messageAttachment.setFileName(MimeUtility.encodeText(filePath.getName(), "UTF-8", null));
                msgMultipart.addBodyPart(messageAttachment);
            }
        }
        message.setContent(msgMultipart);
    }

    private void send(Message message, Session session, boolean authFlag) throws Exception {
        if (authFlag) {
            LOGGER.info("sendEmail sending with authFlag");
            Transport transport = session.getTransport("smtp");
            transport.connect(emailConfig.getHost(), emailConfig.getUser(), emailConfig.getPwd());
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();
        } else {
            LOGGER.info("sendEmail sending without authFlag");
            Transport.send(message);
        }
    }

    private Properties createPropertiesMail(String host, String port, boolean authFlag) throws SecurityException {
        Properties properties = System.getProperties();
        properties.setProperty("mail.smtp.host", host);
        properties.setProperty("mail.smtp.port", (!StringUtils.isEmpty(port)) ? port : "25");
        if (authFlag) {
            properties.setProperty("mail.smtp.auth", "true");
        }
        properties.setProperty("mail.smtp.debug", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        return properties;
    }

    private String geFreeMarkerTemplateContent(Map<String, Object> model, String emailTemplateName) {
        StringBuffer content = new StringBuffer();
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            freemarkerConfiguration.setClassLoaderForTemplateLoading(classLoader, "/email-templates");
            content.append(FreeMarkerTemplateUtils.processTemplateIntoString(freemarkerConfiguration.getTemplate(emailTemplateName), model));
        } catch (Exception e) {
            LOGGER.error("Exception occurs while processing: {}", e);
        }
        return content.toString();
    }
}
