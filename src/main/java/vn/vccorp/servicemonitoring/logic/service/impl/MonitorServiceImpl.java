/**
 * Created by: tuyennta
 * Created on: 20/05/2019 10:15
 */

package vn.vccorp.servicemonitoring.logic.service.impl;

import org.dozer.DozerBeanMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.vccorp.servicemonitoring.dto.LogServiceDTO;
import org.springframework.transaction.annotation.Transactional;
import vn.vccorp.servicemonitoring.dto.ServiceDTO;
import vn.vccorp.servicemonitoring.entity.Server;
import vn.vccorp.servicemonitoring.entity.UserService;
import vn.vccorp.servicemonitoring.enumtype.Role;
import vn.vccorp.servicemonitoring.exception.ApplicationException;
import vn.vccorp.servicemonitoring.logic.repository.ServerRepository;
import vn.vccorp.servicemonitoring.logic.repository.ServiceRepository;
import vn.vccorp.servicemonitoring.logic.repository.UserServiceRepository;
import vn.vccorp.servicemonitoring.logic.service.MonitorService;
import vn.vccorp.servicemonitoring.message.Messages;
import vn.vccorp.servicemonitoring.utils.AppUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MonitorServiceImpl implements MonitorService {

    @Value("${ssh.port}")
    private String sshPort;
    @Value("${ssh.username}")
    private String sshUsername;
    @Autowired
    private DozerBeanMapper dozerBeanMapper;
    @Autowired
    private ServiceRepository serviceRepository;
    @Autowired
    private UserServiceRepository userServiceRepository;
    @Autowired
    private Messages messages;
    @Autowired
    private ServerRepository serverRepository;

    @Transactional
    @Override
    public void registerService(ServiceDTO serviceDTO) {
        //get server info from serverId
        Server server = serverRepository.findById(serviceDTO.getServerId())
                .orElseThrow(() -> new ApplicationException(messages.get("service.server.not-available", new String[]{String.valueOf(serviceDTO.getServerId())})));

        //check if service with specify info is correct on the system
        if (!isProcessAlive(serviceDTO.getServerIp(), serviceDTO.getPid())) {
            throw new ApplicationException(messages.get("service.pid.not-available", new String[]{serviceDTO.getPid(), serviceDTO.getServerIp()}));
        }
        //check if log file is available
        File logFile = new File(serviceDTO.getLogDir() + serviceDTO.getLogFile());
        if (!isFileExist(serviceDTO.getServerIp(), logFile.getAbsolutePath())) {
            throw new ApplicationException(messages.get("service.log.not-available", new String[]{logFile.getAbsolutePath(), serviceDTO.getServerIp()}));
        }
        //check if deploy dir is available
        File deployDir = new File(serviceDTO.getDeployDir());
        if (!isFolderExist(serviceDTO.getServerIp(), deployDir.getAbsolutePath())) {
            throw new ApplicationException(messages.get("service.deploydir.not-available", new String[]{deployDir.getAbsolutePath(), serviceDTO.getServerIp()}));
        } else {
            //create a file on deploy dir to run service
            String deployCommand = "#!/bin/bash \n";
            deployCommand += "cd " + serviceDTO.getDeployDir() + " \n";
            deployCommand += serviceDTO.getDeployCommand() + "\n";
            deployCommand += "echo $!";
            File runFile = new File(getRunFileName(serviceDTO.getName()));
            try {
                runFile.createNewFile();
                BufferedWriter br = new BufferedWriter(new FileWriter(runFile));
                br.write(deployCommand);
                br.flush();
                br.close();
                runFile.setExecutable(true);
                AppUtils.putFile(serviceDTO.getServerIp(), sshUsername, sshPort, runFile.getAbsolutePath(), serviceDTO.getDeployDir());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        vn.vccorp.servicemonitoring.entity.Service service = dozerBeanMapper.map(serviceDTO, vn.vccorp.servicemonitoring.entity.Service.class);
        service.setStartTime(AppUtils.getStartedDateOfProcess(serviceDTO.getServerIp(), sshUsername, sshPort, service.getPid()));
        service.setServer(server);
        serviceRepository.save(service);

        //save UserService
        List<UserService> userServices = serviceDTO.getMaintainerIds()
                .parallelStream().map(id -> new UserService(id, service.getId(), Role.MAINTAINER)).collect(Collectors.toList());
        userServices.add(new UserService(serviceDTO.getOwnerId(), service.getId(), Role.OWNER));
        userServiceRepository.saveAll(userServices);
    }

    private String getRunFileName(String name) {
        return "run-" + name + ".sh";
    }

    @Override
    public void startService(int serviceId) {
        //check if service is existed in db
        vn.vccorp.servicemonitoring.entity.Service service = serviceRepository.findById(serviceId).orElseThrow(() -> new ApplicationException(messages.get("service.id.not-found")));

        //check if service is already run then we do nothing
        if (isServiceRunning(String.valueOf(serviceId), service.getPid())){
            return;
        }

        String startCommand = "ssh -p " + sshPort + " " + sshUsername + "@" + service.getServer().getIp()
                + " -t 'nohup sh " + service.getDeployDir() + getRunFileName(service.getName()) + " > " + service.getDeployDir() + "pid'";

        AppUtils.executeCommand(startCommand);

        String getPidCommand = "ssh -p " + sshPort + " " + sshUsername + "@" + service.getServer().getIp() + " -t 'cat " + service.getDeployDir() + "pid'";
        List<String> out = AppUtils.executeCommand(getPidCommand);
        if (out.isEmpty()) {
            throw new ApplicationException(messages.get("service.error.starting"));
        } else {
            service.setPid(out.get(0));
            serviceRepository.save(service);
        }
    }

    private boolean isServiceRunning(String serverId, String pid){
        String command = "ssh -p " + sshPort + " " + sshUsername + "@" + serverId + " -t 'ps -p " + pid + "'";
        List<String> out = AppUtils.executeCommand(command);
        return out.size() > 1 && out.get(1).split(" ")[0].equals(pid);
    }

    @Override
    public void stopService(int serviceId) {
        vn.vccorp.servicemonitoring.entity.Service service = serviceRepository.findById(serviceId).orElseThrow(() -> new ApplicationException(messages.get("service.id.not-found")));
        String command = "ssh -p " + sshPort + " " + sshUsername + "@" + service.getServer().getIp() + " -t 'kill -9 " + service.getPid() + "'; echo $?";
        List<String> out = AppUtils.executeCommand(command);
        if (out.isEmpty() || !out.get(0).equals("0")) {
            throw new ApplicationException(messages.get("service.error.stopping"));
        }
    }

    @Override
    public List<String> getLogService(LogServiceDTO logServiceDTO) {
        vn.vccorp.servicemonitoring.entity.Service service = serviceRepository.findById(logServiceDTO.getServiceId()).orElseThrow(() -> new ApplicationException(messages.get("service.id.not-found")));
        //check if log file is available
        File logRemoteFile = new File(service.getLogDir() + service.getLogFile());
        if (!isFileExist(service.getServer().getIp(), logRemoteFile.getAbsolutePath())) {
            throw new ApplicationException(messages.get("service.log.not-available"));
        }
        String command;
        if (logServiceDTO.getStart() == 0 && logServiceDTO.getEnd() == 0){
            command = "ssh -p " + sshPort + " " + sshUsername + "@" + service.getServer().getIp() + " -t 'tail -n 1000 " + logRemoteFile.getAbsolutePath() + "'";
        }
        else {
            command = "ssh -p " + sshPort + " " + sshUsername + "@" + service.getServer().getIp()
                    + " -t 'sed -n '" + logServiceDTO.getStart() + "," + logServiceDTO.getEnd() + "p'" + logRemoteFile.getAbsolutePath() + "'";
        }

        List<String> out = AppUtils.executeCommand(command);
        if (out.isEmpty() || !out.get(0).equals("0")) {
            throw new ApplicationException(messages.get("service.error.getLog"));
        }
        return out;
    }

    private boolean isFileExist(String serverIP, String filePath) {
        String command = "ssh -p " + sshPort + " " + sshUsername + "@" + serverIP + " -t 'test -f " + filePath + "'; echo $?";
        List<String> out = AppUtils.executeCommand(command);
        if (!out.isEmpty() && out.get(0).equals("0")) {
            return true;
        }
        return false;
    }

    private boolean isFolderExist(String serverIP, String filePath) {
        String command = "ssh -p " + sshPort + " " + sshUsername + "@" + serverIP + " -t 'test -d " + filePath + "'; echo $?";
        List<String> out = AppUtils.executeCommand(command);
        if (!out.isEmpty() && out.get(0).equals("0")) {
            return true;
        }
        return false;
    }

    private boolean isProcessAlive(String serverIP, String PID) {
        String command = "ssh -p " + sshPort + " " + sshUsername + "@" + serverIP + " -t 'ps -p " + PID + " > /dev/null'; echo $?";
        List<String> out = AppUtils.executeCommand(command);
        //if command execute success it will return 0
        if (!out.isEmpty() && out.get(0).equals("0")) {
            return true;
        }
        return false;
    }

    private boolean syncLogFromRemote(String serverIP, String remoteLog, String localLog, int limit) {
        String command = "ssh -p " + sshPort + " " + sshUsername + "@" + serverIP +  " -t 'tail -n " + limit + " " + remoteLog + " >> " + localLog + "'; echo $?";
        List<String> out = AppUtils.executeCommand(command);
        if (!out.isEmpty() && out.get(0).equals("0")) {
            return true;
        }
        return false;
    }
}
