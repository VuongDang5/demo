/**
 * Created by: tuyennta
 * Created on: 30/05/2019 11:05
 */

package vn.vccorp.servicemonitoring.logic.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import vn.vccorp.servicemonitoring.dto.ServiceErrorDTO;
import vn.vccorp.servicemonitoring.entity.*;
import vn.vccorp.servicemonitoring.enumtype.IssueType;
import vn.vccorp.servicemonitoring.enumtype.Role;
import vn.vccorp.servicemonitoring.exception.ApplicationException;
import vn.vccorp.servicemonitoring.logic.repository.*;
import vn.vccorp.servicemonitoring.logic.service.EmailService;
import vn.vccorp.servicemonitoring.logic.service.HealthCheckService;
import vn.vccorp.servicemonitoring.message.Messages;
import vn.vccorp.servicemonitoring.utils.AppConstants;
import vn.vccorp.servicemonitoring.utils.AppUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@org.springframework.stereotype.Service
public class HealthCheckServiceImpl implements HealthCheckService {

    private static final Logger LOGGER = LoggerFactory.getLogger(HealthCheckServiceImpl.class);
    @Value("${ssh.port}")
    private String sshPort;
    @Value("${ssh.username}")
    private String sshUsername;
    @Autowired
    private SnapshotRepository snapshotRepository;
    @Autowired
    private Messages messages;
    @Autowired
    private LogServiceRepository logServiceRepository;
    @Value("${service.last-update-log.max}")
    private int lastUpdateLogMax;
    @Value("${service.log-folder.name}")
    private String folderLogName;
    @Autowired
    private EmailService emailService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private IssueTrackingRepository issueTrackingRepository;

    @Override
    public void checkResourcesUsage(Service service) {

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

    @Override
    public void checkLogService(Service service) {
        //Create new LogService if it = null
        LogService logService = logServiceRepository.findByServiceId(service.getId()).orElse(
                LogService.builder()
                        .serviceId(service.getId())
                        .checkedLine(-1)
                        .build()
        );
        String logFilePath = service.getLogDir() + service.getLogFile();
        long lastLineRemote = AppUtils.getLastLine(service.getServer().getIp(), logFilePath, sshPort, sshUsername);
        if (lastLineRemote == -1) {
            throw new ApplicationException(messages.get("service.check-log.error"));
        }

        //if this is the first time we check log, then we will only check for the last 10 lines
        //because we dont care what happen before the service is registered to the tool
        long limit = logService.getCheckedLine() == -1 ? 10 : lastLineRemote - logService.getCheckedLine();

        //get log
        List<String> logs = AppUtils.getLog(service.getServer().getIp(), service.getLogDir() + service.getLogFile(), limit, sshPort, sshUsername);

        List<String> lassErr = new ArrayList<>();
        List<String> lassWarn = new ArrayList<>();

        //check log file service
        String line;
        long problemAt = -1;
        for (int i = 0; i < logs.size(); i++) {
            line = logs.get(i);
            if (StringUtils.containsIgnoreCase(line, AppConstants.ERROR_REGEX)) {
                lassErr.add(line);
                problemAt = logService.getCheckedLine() + i;
            } else if (StringUtils.containsIgnoreCase(line, AppConstants.WARN_REGEX)) {
                lassWarn.add(line);
                problemAt = logService.getCheckedLine() + i;
            }
        }

        if (!lassErr.isEmpty()) {
            String detailMessage = String.format("Service is running with error: %s", lassErr);
            addingIssueTrackingAndSendReport(service, detailMessage, IssueType.ERROR, problemAt);
        }

        if (!lassWarn.isEmpty()) {
            String detailMessage = String.format("Service is running with warning: %s", lassWarn);
            addingIssueTrackingAndSendReport(service, detailMessage, IssueType.WARNING, problemAt);
        }

        //save last check
        logService.setCheckedLine(lastLineRemote);
        //save log service
        logServiceRepository.save(logService);
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
            addingIssueTrackingAndSendReport(service, detailMessage, IssueType.WARNING, null);
        }
        if (service.getCpuLimit() != null && service.getCpuLimit() > 0 && service.getCpuLimit() <= snapshot.getCpuUsed()) {
            String detailMessage = String.format("Your service has bean reached CPU limited. Current percent of CPU used: %s, limit: %s", snapshot.getCpuUsed(), service.getCpuLimit());
            addingIssueTrackingAndSendReport(service, detailMessage, IssueType.WARNING, null);
        }
        if (service.getDiskLimit() != null && service.getDiskLimit() > 0 && service.getDiskLimit() >= snapshot.getDiskUsed()) {
            String detailMessage = String.format("Your service has bean reached disk limited. Current percent of disk used: %s, limit: %s", snapshot.getDiskUsed(), service.getDiskLimit());
            addingIssueTrackingAndSendReport(service, detailMessage, IssueType.WARNING, null);
        }
    }

    /**
     * Adding a new issue_tracking to db and send warning report with specific detail message
     *  @param service       service that got warning
     * @param detailMessage detail message of warning
     * @param issueType type of issue
     * @param problemAt line number in log file where problem happen
     */
    private void addingIssueTrackingAndSendReport(Service service, String detailMessage, IssueType issueType, Long problemAt) {
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
                //TODO we will create a link to view log file from problemAt line
                .linkOnTool("will be updated")
                .problem(issueType.name())
                .status(service.getStatus().name())
                .build();

        //build recipients from owner, maintainers and admins
        List<String> recipients = userRepository.findAllByServiceId(service.getId());
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

    //read tail file
    private List<String> fileTail(final Path source, final int limit) throws IOException {
        final String[] data = new String[limit];
        AtomicInteger counter = new AtomicInteger(0);

        try (Stream<String> stream = Files.lines(source)) {
            stream.forEach(line -> data[counter.getAndIncrement() % limit] = line);
            return IntStream.range(counter.get() < limit ? 0 : counter.get() - limit, counter.get())
                    .mapToObj(index -> data[index % limit])
                    .collect(Collectors.toList());
        }
    }

    public void checkServiceStatus(Service service) {
        if (!AppUtils.isProcessAlive(service.getServer().getIp(), service.getPid(), sshPort, sshUsername)) {
            String detailMessage = "Your service has died";
            addingIssueTrackingAndSendReport(service, detailMessage, IssueType.ERROR, null);
        }
    }
}
