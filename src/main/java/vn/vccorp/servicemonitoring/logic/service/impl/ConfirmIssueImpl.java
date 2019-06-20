package vn.vccorp.servicemonitoring.logic.service.impl;

import org.dozer.DozerBeanMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import vn.vccorp.servicemonitoring.dto.IssueTrackingDTO;
import vn.vccorp.servicemonitoring.dto.ServiceDTO;
import vn.vccorp.servicemonitoring.dto.ServiceErrorDTO;
import vn.vccorp.servicemonitoring.entity.IssueTracking;
import vn.vccorp.servicemonitoring.entity.Service;
import vn.vccorp.servicemonitoring.entity.User;
import vn.vccorp.servicemonitoring.enumtype.IssueType;
import vn.vccorp.servicemonitoring.enumtype.Role;
import vn.vccorp.servicemonitoring.enumtype.Status;
import vn.vccorp.servicemonitoring.exception.ApplicationException;
import vn.vccorp.servicemonitoring.logic.repository.IssueTrackingRepository;
import vn.vccorp.servicemonitoring.logic.repository.ServiceRepository;
import vn.vccorp.servicemonitoring.logic.repository.UserRepository;
import vn.vccorp.servicemonitoring.logic.service.ConfirmIssue;
import vn.vccorp.servicemonitoring.logic.service.EmailService;
import vn.vccorp.servicemonitoring.logic.service.HealthCheckService;
import vn.vccorp.servicemonitoring.message.Messages;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@org.springframework.stereotype.Service
public class ConfirmIssueImpl implements ConfirmIssue {

    @Autowired
    private IssueTrackingRepository issueTrackingRepository;
    @Autowired
    private ServiceRepository serviceRepository;
    @Autowired
    DozerBeanMapper dozerBeanMapper;
    @Autowired
    private HealthCheckService healthCheckService;
    @Autowired
    private Messages messages;
    @Autowired
    private EmailService emailService;
    @Autowired
    private UserRepository userRepository;

    @Override
    public List<IssueTrackingDTO> getAllIssueError(){
        List<IssueTrackingDTO> listIssueError = new ArrayList<>();

        Page<Service> services;
        do {
            //get all services
            services = serviceRepository.findAll(PageRequest.of(0, 10));

            //loop through all services and check one by one
            for (Service service : services.getContent()) {
                //find last Issue in service
                Optional<IssueTracking> lastIssueRecovery = issueTrackingRepository.findTopByServiceAndIssueTypeOrderByTrackingTimeDesc(service, IssueType.RECOVERY);
                List<IssueTracking> IssuesService = new ArrayList<>();
                if (lastIssueRecovery.isPresent()) {
                    IssuesService = issueTrackingRepository.findAllByServiceAndTrackingTimeGreaterThanOrderByTrackingTimeDesc(service, lastIssueRecovery.get().getTrackingTime());
                } else {
                    IssuesService = issueTrackingRepository.findAllByServiceOrderByTrackingTimeDesc(service);
                }
                listIssueError.addAll(IssuesService.parallelStream()
                        .map(issue -> IssueTrackingDTO.builder()
                                .serviceId(issue.getService().getId())
                                .trackingTime(issue.getTrackingTime())
                                .issueType(issue.getIssueType())
                                .detail(issue.getDetail())
                                .build()).collect(Collectors.toList()));
            }
        } while (services.hasNext());

        return listIssueError;
    }

    @Override
    public List<ServiceDTO> getAllServiceError(){
        List<ServiceDTO> listServiceError = new ArrayList<>();
        for (Service service : serviceRepository.findAll()){
            if (service.getStatus() != Status.ACTIVE){
                listServiceError.add(dozerBeanMapper.map(service, ServiceDTO.class));
            }
        }
        return listServiceError;
    }

    @Override
    public void userConfirmIssue(int serviceId){
        //create a new record for issue_tracking
        String detailMessage = String.format("Service is Recovery");
        healthCheckService.addingIssueTrackingAndSendReport(serviceRepository.findById(serviceId).orElseThrow(() ->
                new ApplicationException(messages.get("service.id.not-found"))), detailMessage, IssueType.RECOVERY, null);
    }

    @Override
    public void disableIssue(int serviceId, Date date){
        Service service = serviceRepository.findById(serviceId).orElseThrow(() -> new ApplicationException(messages.get("service.id.not-found")));
        //find last issue
        Optional<IssueTracking> lastIssue = issueTrackingRepository.findTopByServiceOrderByTrackingTimeDesc(service);
        if (!lastIssue.isPresent()){
            return;
        }
        if (lastIssue.get().getIssueType() == IssueType.RECOVERY){
            return;
        }

        String issueDetail = lastIssue.get().getDetail();
        //if had disable service
        String[] disableDetail = issueDetail.split("::", 3);
        if (disableDetail[0].equals("Disable")){
            issueDetail = "Disable::" + date.toString() +"::" + disableDetail[2];
        }
        else{
            issueDetail = "Disable::" + date.toString() +"::\n" + issueDetail;
        }

        lastIssue.get().setDetail(issueDetail);
        issueTrackingRepository.save(lastIssue.get());

        //send mail disable
        ServiceErrorDTO errorDTO = ServiceErrorDTO.builder()
                .serviceName(service.getName())
                .deployedServer(service.getServer().getIp())
                .detail("Your service is disable to date: "+ date.toString())
                .linkOnTool("will be updated")
                .problem(lastIssue.get().getIssueType().name())
                .status(service.getStatus().name())
                .build();

        //build recipients from owner, maintainers and admins
        List<String> recipients = userRepository.findAllByServiceId(service.getId());
        recipients.addAll(userRepository.findAllByRole(Role.ADMIN).parallelStream().map(User::getEmail).collect(Collectors.toList()));
        recipients = recipients.parallelStream().distinct().collect(Collectors.toList());

        //send email
        emailService.sendServiceErrorMessage(errorDTO, recipients);
    }
}