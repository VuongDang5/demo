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
import vn.vccorp.servicemonitoring.entity.Service;
import vn.vccorp.servicemonitoring.entity.Snapshot;
import vn.vccorp.servicemonitoring.logic.repository.ServiceRepository;
import vn.vccorp.servicemonitoring.logic.repository.SnapshotRepository;
import vn.vccorp.servicemonitoring.logic.service.HealthCheckService;
import vn.vccorp.servicemonitoring.message.Messages;
import vn.vccorp.servicemonitoring.utils.AppUtils;

import java.util.List;

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
}
