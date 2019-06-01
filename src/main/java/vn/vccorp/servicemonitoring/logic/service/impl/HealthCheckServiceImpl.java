/**
 * Created by: tuyennta
 * Created on: 30/05/2019 11:05
 */

package vn.vccorp.servicemonitoring.logic.service.impl;

import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import vn.vccorp.servicemonitoring.dto.ServiceErrorDTO;
import vn.vccorp.servicemonitoring.entity.IssueTracking;
import vn.vccorp.servicemonitoring.entity.Service;
import vn.vccorp.servicemonitoring.entity.Snapshot;
import vn.vccorp.servicemonitoring.entity.User;
import vn.vccorp.servicemonitoring.enumtype.IssueType;
import vn.vccorp.servicemonitoring.enumtype.Role;
import vn.vccorp.servicemonitoring.logic.repository.*;
import vn.vccorp.servicemonitoring.logic.service.EmailService;
import vn.vccorp.servicemonitoring.logic.service.HealthCheckService;
import vn.vccorp.servicemonitoring.message.Messages;
import vn.vccorp.servicemonitoring.utils.AppUtils;

import java.util.List;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
public class HealthCheckServiceImpl implements HealthCheckService {

    private static final Logger LOGGER = LoggerFactory.getLogger(HealthCheckServiceImpl.class);
    @Value("${ssh.port}")
    private String sshPort;
    @Value("${ssh.username}")
    private String sshUsername;
    @Autowired
    private ServiceRepository serviceRepository;
    @Autowired
    private SnapshotRepository snapshotRepository;
    @Autowired
    private Messages messages;
    @Autowired
    private EmailService emailService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private IssueTrackingRepository issueTrackingRepository;

    @Transactional
    @Override
    public void checkResources(Service service) {

        //get cpu,ram usage
        Snapshot snapshot = getCpuAndMemUsage(service.getPid(), service.getServer().getIp());
        snapshot.setService(service);

        //get disk usage
        snapshot.setDiskUsed(getDiskUsage(service.getDeployDir(), service.getLogDir(), service.getServer().getIp()));

        //save snapshot
        snapshotRepository.save(snapshot);

        //check if limit threshold is over
        checkLimitResource(service, snapshot);

    }

    /**
     * Check if service is consumed more resources than it's limit
     *
     * @param service  service to check
     * @param snapshot current snapshot of service to compare
     */
    private void checkLimitResource(Service service, Snapshot snapshot) {
        if (service.getRamLimit() != null && service.getRamLimit() > 0 && service.getRamLimit() <= snapshot.getRamUsed()) {
            String detailMessage = String.format("Your service has bean reached RAM limited. Current percent of RAM used: %s, limit: %s", snapshot.getRamUsed(), service.getRamLimit());
            addingIssueTrackingAndSendReport(service, detailMessage, IssueType.WARNING);
        }
        if (service.getCpuLimit() != null && service.getCpuLimit() > 0 && service.getCpuLimit() <= snapshot.getCpuUsed()) {
            String detailMessage = String.format("Your service has bean reached CPU limited. Current percent of CPU used: %s, limit: %s", snapshot.getCpuUsed(), service.getCpuLimit());
            addingIssueTrackingAndSendReport(service, detailMessage, IssueType.WARNING);
        }
        if (service.getDiskLimit() != null && service.getDiskLimit() > 0 && service.getDiskLimit() >= snapshot.getDiskUsed()) {
            String detailMessage = String.format("Your service has bean reached disk limited. Current percent of disk used: %s, limit: %s", snapshot.getDiskUsed(), service.getDiskLimit());
            addingIssueTrackingAndSendReport(service, detailMessage, IssueType.WARNING);
        }
    }

    /**
     * Adding a new issue_tracking to db and send warning report with specific detail message
     *  @param service       service that got warning
     * @param detailMessage detail message of warning
     * @param issueType
     */
    private void addingIssueTrackingAndSendReport(Service service, String detailMessage, IssueType issueType) {
        //create a new record for issue_tracking
        IssueTracking issueTracking = new IssueTracking();
        issueTracking.setIssueType(issueType);
        issueTracking.setDetail(detailMessage);
        issueTracking.setService(service);
        issueTracking.setTrackingTime(LocalDateTime.now().toDate());
        issueTrackingRepository.save(issueTracking);

        //build error object to send warning email
        ServiceErrorDTO errorDTO = ServiceErrorDTO.builder()
                .serviceName(service.getName())
                .deployedServer(service.getServer().getIp())
                .detail(detailMessage)
                .linkOnTool("will be updated")
                .problem(issueType.name())
                .status(service.getStatus().name())
                .build();

        //build recipients from owner, maintainers and admins
        List<String> recipients = service.getUserServices().parallelStream().map(u -> u.getUser().getEmail()).collect(Collectors.toList());
        recipients.addAll(userRepository.findAllByRole(Role.ADMIN).parallelStream().map(User::getEmail).collect(Collectors.toList()));
        recipients = recipients.parallelStream().distinct().collect(Collectors.toList());

        //send email
        emailService.sendServiceReachLimitWarning(errorDTO, recipients);
    }

    /**
     * Get percent disk usage of a service
     *
     * @param deployDir deploy directory of service
     * @param logDir    log directory of service
     * @param serverIp  server where service is running
     * @return percent of disk usage of service
     */
    private float getDiskUsage(String deployDir, String logDir, String serverIp) {
        int size = 0;
        //if deploy dir is not a parent of log dir than we must check both dir
        if (!deployDir.contains(logDir)) {
            size = AppUtils.getFolderSize(logDir, serverIp, sshPort, sshUsername);
        }
        size += AppUtils.getFolderSize(deployDir, serverIp, sshPort, sshUsername);

        int total = AppUtils.getDiskSize(deployDir, serverIp, sshPort, sshUsername);
        return (float) size / (float) total * 100;
    }

    /**
     * Get snapshot about percent cpu and memory usage of a service by it's pid
     *
     * @param pid      pid of a service
     * @param serverIp server where service is running on
     * @return snapshot instance
     */
    private Snapshot getCpuAndMemUsage(String pid, String serverIp) {
        Snapshot snapshot = new Snapshot();
        snapshot.setTime(LocalDateTime.now().toDate());
        String command = String.format("ssh -p %s %s@%s -t 'ps -p %s --no-headers -o %%cpu,%%mem'",
                sshPort, sshUsername, serverIp, pid);
        List<String> out = AppUtils.executeCommand(command);
        if (!out.isEmpty()) {
            String[] cpuAndMem = out.get(0).trim().split("\\s+");
            snapshot.setCpuUsed(Float.valueOf(cpuAndMem[0]));
            snapshot.setRamUsed(Float.valueOf(cpuAndMem[1]));
        }
        return snapshot;
    }

    public void healthCheck1(Service service) {
        if (!AppUtils.isProcessAlive(service.getServer().getIp(), service.getPid(), sshPort, sshUsername)) {
            String detailMessage = "Your service has died";
            addingIssueTrackingAndSendReport(service, detailMessage, IssueType.ERROR);
        }
    }
}
