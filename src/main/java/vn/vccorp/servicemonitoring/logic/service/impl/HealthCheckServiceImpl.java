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
import vn.vccorp.servicemonitoring.entity.LogService;
import vn.vccorp.servicemonitoring.entity.Service;
import vn.vccorp.servicemonitoring.entity.Snapshot;
import vn.vccorp.servicemonitoring.entity.User;
import vn.vccorp.servicemonitoring.enumtype.IssueType;
import vn.vccorp.servicemonitoring.enumtype.Role;
import vn.vccorp.servicemonitoring.logic.repository.*;
import vn.vccorp.servicemonitoring.logic.service.EmailService;
import vn.vccorp.servicemonitoring.exception.ApplicationException;
import vn.vccorp.servicemonitoring.logic.repository.LogServiceRepository;
import vn.vccorp.servicemonitoring.logic.repository.ServiceRepository;
import vn.vccorp.servicemonitoring.logic.repository.SnapshotRepository;
import vn.vccorp.servicemonitoring.logic.service.HealthCheckService;
import vn.vccorp.servicemonitoring.message.Messages;
import vn.vccorp.servicemonitoring.utils.AppConstants;
import vn.vccorp.servicemonitoring.utils.AppUtils;

import java.io.File;
import java.io.IOException;
import java.net.Inet4Address;
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
    private ServiceRepository serviceRepository;
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
    private EmailService emailService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private IssueTrackingRepository issueTrackingRepository;

    @Transactional
    @Override
    public void checkLogService(Service service) {
        //check log services
        LogService logService = checkLog(service);
        //save log service
        logServiceRepository.save(logService);
    }

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

    private LogService checkLog(Service service) {
        File logFile = new File(service.getLogDir() + service.getLogFile());
        //Create new LogService if it = null
        LogService logService = logServiceRepository.findByServiceId(service.getId()).orElse(
                LogService.builder()
                        .serviceId(service.getId())
                        .errorCount(0)
                        .updatedDate(LocalDateTime.now())
                        .createdDate(LocalDateTime.now())
                        .lastLoggingDate(LocalDateTime.now())
                        .checkedLine(0)
                        .logFile(service.getLogFile())
                        .consecutiveErrCount(0)
                        .errorPerHourCount(0)
                        .hourlyCheck(LocalDateTime.now())
                        .logSize(0.0f)
                        .build()
        );

        //check if service run other service
        try {
            //check logfile is exist
            if (!AppUtils.isFileExist(service.getServer().getIp(), logFile.getAbsolutePath(), sshPort, sshUsername)) {
                throw new ApplicationException(messages.get("service.log.not-available"));
            }
            logService.setLogDir(service.getLogDir());

            if (!Inet4Address.getLocalHost().getHostAddress().equals(service.getServer().getIp())) {
                File logRemoteFile = new File(service.getLogDir() + service.getLogFile());
                File localLogDir = new File(folderLogName + File.separator + service.getName());
                //create new logfile on current server
                logFile = new File(localLogDir.getAbsolutePath() + File.separator + service.getLogFile());
                logService.setLogDir(localLogDir.getAbsolutePath());

                if (!logFile.exists()) {
                    //create a folder for saving local log which is mapped to remote log
                    if (!logFile.getParentFile().exists()) {
                        logFile.getParentFile().mkdirs();
                    }
                    if (!logFile.exists()) {
                        logFile.createNewFile();
                    }

                    //sync logs had been checked
                    if (!AppUtils.syncLogFromRemote(service.getServer().getIp(), logRemoteFile.getAbsolutePath(), logFile.getAbsolutePath(), (int) logService.getCheckedLine(), sshPort, sshUsername)) {
                        throw new ApplicationException(messages.get("service.log.sync.error"));
                    }

                    if (!logFile.exists()) {
                        throw new ApplicationException(messages.get("Log file not found: " + logFile.getAbsolutePath() + ". Service: " + service.getName()));
                    }
                }

                //sync log
                long newLine = AppUtils.getLastLine(service.getServer().getIp(), logRemoteFile.getAbsolutePath(), sshPort, sshUsername);
                if (newLine == -1) {
                    throw new ApplicationException(messages.get("service.check-log.error"));
                }
                long lastLine = Files.lines(logFile.toPath()).count();

                //log remote is deleted
                if (newLine < lastLine) {
                    //delete file and sync
                    org.apache.commons.io.FileUtils.write(logFile, "");
                    lastLine = 0;
                }

                //sync log
                int limit = Math.toIntExact(newLine - lastLine);
                if (!AppUtils.syncLogFromRemote(service.getServer().getIp(), logRemoteFile.getAbsolutePath(), logFile.getAbsolutePath(), limit, sshPort, sshUsername)) {
                    throw new ApplicationException(messages.get("service.log.sync.error"));
                }
            }
        }
        catch (IOException e) {
            throw new ApplicationException(messages.get("service.server.ip.not-available"));
        }

        //
        int errCount = 0;
        List<String> lassErr = new ArrayList<>();
        boolean isBeginErr = false;

        //check log file service
        try {
            long newLine = Files.lines(logFile.toPath()).count();
            long lastLine = logService.getCheckedLine();

            //Service don't have new log
            if (newLine == lastLine) {
                // send notify if service not write any thing after 24h
                if (logService.getLastLoggingDate().plusHours(lastUpdateLogMax).isBefore(LocalDateTime.now())) {
                    //
                    String detailMessage = String.format("Service dit not write any things since: %s, service: %s", logService.getLastLoggingDate(), service.getName());
                    addingIssueTrackingAndSendReport(service, detailMessage, IssueType.WARNING);
                }
                logService.setUpdatedDate(LocalDateTime.now());
                return logService;
            }
            //log file had deleted
            if (newLine < lastLine) {
                lastLine = 0;
            }

            int limit = Math.toIntExact(newLine - lastLine);
            //Get new logs and check error
            List<String> logs = fileTail(logFile.toPath(), limit);
            for (String line : logs) {
                if (line.matches(AppConstants.ERROR_REGEX)) {
                    lassErr.clear();
                    errCount++;
                    isBeginErr = true;
                    lassErr.add(line);
                } else {
                    if (line.matches(AppConstants.INFO_REGEX) || line.matches(AppConstants.DEBUG_REGEX) || line.matches(AppConstants.WARN_REGEX)) {
                        isBeginErr = false;
                    }
                    if (isBeginErr) {
                        lassErr.add(line);
                    }
                }
            }

            if (lassErr.isEmpty()) {
                logService.setConsecutiveErrCount(0);
            } else { //if error
                logService.setConsecutiveErrCount(logService.getConsecutiveErrCount() + 1);
                //
                String detailMessage = String.format("Service is running with %s errors, with error: %s",logService.getConsecutiveErrCount(), lassErr);
                addingIssueTrackingAndSendReport(service, detailMessage, IssueType.ERROR);
            }

            //save last check
            logService.setCheckedLine(newLine);
            logService.setErrorCount(logService.getErrorCount() + errCount);
            logService.setErrorMsg(String.join("\r\n", lassErr));
            logService.setUpdatedDate(LocalDateTime.now());
            logService.setLastLoggingDate(LocalDateTime.now());
            //save log service
            logServiceRepository.save(logService);
        }
        catch (IOException e){
            throw new ApplicationException(messages.get("service.check-log.error"));
        }

        return logService;
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

    public void healthCheck1(Service service) {
        if (!AppUtils.isProcessAlive(service.getServer().getIp(), service.getPid(), sshPort, sshUsername)) {
            String detailMessage = "Your service has died";
            addingIssueTrackingAndSendReport(service, detailMessage, IssueType.ERROR);
        }
    }
}
