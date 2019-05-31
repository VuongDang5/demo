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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import vn.vccorp.servicemonitoring.entity.LogService;
import vn.vccorp.servicemonitoring.entity.Service;
import vn.vccorp.servicemonitoring.entity.Snapshot;
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
    private int lastUpdateLogMax = 24;

    @Transactional
    @Override
    public void checkLogService() {
        Page<Service> services;
        do {
            //get all services
            services = serviceRepository.findAll(PageRequest.of(0, 10));

            //loop through all services and check one by one
            for (Service service : services.getContent()) {
                //check log all services
                LogService logService = checkLog(service);
                //save log service
                logServiceRepository.save(logService);
            }
        } while (services.hasNext());
    }

    @Transactional
    @Override
    public void checkResources() {
        Page<Service> services;
        do {
            //get all services
            services = serviceRepository.findAll(PageRequest.of(0, 10));

            //loop through all services and check one by one
            for (Service service : services.getContent()) {
                //get cpu,ram usage
                Snapshot snapshot = getCpuAndMemUsage(service.getPid(), service.getServer().getIp());
                snapshot.setService(service);

                //get disk usage
                snapshot.setDiskUsed(getDiskUsage(service.getDeployDir(), service.getLogDir(), service.getServer().getIp()));

                //save snapshot
                snapshotRepository.save(snapshot);

                //check if limit threshold is over
            }
        } while (services.hasNext());
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

    private LogService checkLog(vn.vccorp.servicemonitoring.entity.Service service)
    {
        //check if log remote file is available
        File logRemoteFile = new File(service.getLogDir() + service.getLogFile());
        if (!AppUtils.isFileExist(service.getServer().getIp(), logRemoteFile.getAbsolutePath(), sshPort, sshUsername)) {
            throw new ApplicationException(messages.get("service.log.not-available"));
        }
        File localLogDir = new File(service.getName());
        File logFile = new File(localLogDir.getAbsolutePath() + File.separator + service.getLogFile());

        //Create new LogService if it = null
        LogService logService = logServiceRepository.findByServiceId(service.getId()).orElse(
                LogService.builder()
                        .serviceId(service.getId())
                        .errorCount(0)
                        .updatedDate(LocalDateTime.now())
                        .createdDate(LocalDateTime.now())
                        .lastLoggingDate(LocalDateTime.now())
                        .checkedLine(0)
                        .logDir(localLogDir.getAbsolutePath())
                        .logFile(service.getLogFile())
                        .consecutiveErrCount(0)
                        .errorPerHourCount(0)
                        .hourlyCheck(LocalDateTime.now())
                        .logSize(0.0f)
                        .build()
        );
        logServiceRepository.save(logService);

        //
        int errCount = 0;
        List<String> lassErr = new ArrayList<>();
        boolean isBeginErr = false;

        String msg = "Service is still logging normally";
        if (!logFile.exists()) {
            try {
                //create a folder for saving local log which is mapped to remote log
                if (!logFile.getParentFile().exists()) {
                    logFile.getParentFile().mkdirs();
                }
                if (!logFile.exists()) {
                    logFile.createNewFile();
                }

                //sync log from that host to current host
                int newLine = (int)AppUtils.getLastLine(service.getServer().getIp(), logRemoteFile.getAbsolutePath(), sshPort, sshUsername);
                if (!AppUtils.syncLogFromRemote(service.getServer().getIp(), logRemoteFile.getAbsolutePath(), logFile.getAbsolutePath(), newLine, sshPort, sshUsername)) {
                    throw new ApplicationException(messages.get("service.log.sync.error"));
                }

                //save last check
                logService.setCheckedLine(newLine);
                logService.setUpdatedDate(LocalDateTime.now());
                logService.setLastLoggingDate(LocalDateTime.now());

                if (!logFile.exists()) {
                    msg = "Log file not found: " + logFile.getAbsolutePath() + ". Service: " + service.getName();
                    return logService;
                }
            }
            catch (IOException e){
                throw new ApplicationException(messages.get("service.log.not-available"));
            }
        }

        //
        try {
            long newLine = AppUtils.getLastLine(service.getServer().getIp(), logRemoteFile.getAbsolutePath(), sshPort, sshUsername);
            if (newLine == -1) {
                throw new ApplicationException(messages.get("service.check-log.error"));
            }
            long lastLine = Files.lines(logFile.toPath()).count();

            //Service don't have new log
            if (newLine == lastLine) {
                msg = "Service don't have new log: " + logService.getLastLoggingDate();
                // send notify if service not write any thing after 24h
                if (logService.getLastLoggingDate().plusHours(lastUpdateLogMax).isBefore(LocalDateTime.now())) {
                    sendNotify(logService, " is running without writing any log since: " + logService.getLastLoggingDate());
                }
                logService.setUpdatedDate(LocalDateTime.now());
                return logService;
            }
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
                //Create a issue tracking

                //send notify cuz service have error
                sendNotify(logService, " is running with " + logService.getConsecutiveErrCount() + " consecutive errors since last checks.");
            }

            //save last check
            logService.setCheckedLine(newLine);
            logService.setErrorCount(logService.getErrorCount() + errCount);
            logService.setErrorMsg(String.join("\r\n", lassErr));
            logService.setUpdatedDate(LocalDateTime.now());
            logService.setLastLoggingDate(LocalDateTime.now());
        }
        catch (IOException e){

        }

        return logService;
    }

    //Send notify
    private void sendNotify(LogService logService, String title){

    }

    //read tail file
    private List<String> fileTail(final Path source, final int limit) throws IOException {
        final String[] data = new String[limit];
        AtomicInteger counter = new AtomicInteger(0);

        try (Stream<String> stream = Files.lines(source)) {
            stream.forEach(line ->  data[counter.getAndIncrement() % limit] = line);
            return IntStream.range(counter.get() < limit ? 0 : counter.get() - limit, counter.get())
                    .mapToObj(index -> data[index % limit])
                    .collect(Collectors.toList());
        }
    }
}
