/**
 * Created by: tuyennta
 * Created on: 20/05/2019 10:15
 */

package vn.vccorp.servicemonitoring.logic.service.impl;

import org.dozer.DozerBeanMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.vccorp.servicemonitoring.dto.ServiceDTO;
import vn.vccorp.servicemonitoring.entity.UserService;
import vn.vccorp.servicemonitoring.enumtype.ApplicationError;
import vn.vccorp.servicemonitoring.enumtype.Role;
import vn.vccorp.servicemonitoring.exception.ApplicationException;
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
    @Autowired
    private DozerBeanMapper dozerBeanMapper;
    @Autowired
    private ServiceRepository serviceRepository;
    @Autowired
    private UserServiceRepository userServiceRepository;
    @Autowired
    private Messages messages;

    @Override
    public void registerService(ServiceDTO serviceDTO) {
        //check if service with specify info is correct on the system
        if (!isProcessAlive(serviceDTO.getServerId(), serviceDTO.getPid())) {
            throw new ApplicationException(messages.get("service.pid.not-available"));
        }
        //check if log file is available
        File logFile = new File(serviceDTO.getLogDir() + serviceDTO.getLogFile());
        if (!isFileExist(serviceDTO.getServerId(), logFile.getAbsolutePath())) {
            throw new ApplicationException(messages.get("service.log.not-available"));
        }
        //check if deploy dir is available
        File deployDir = new File(serviceDTO.getDeployDir());
        if (!isFolderExist(serviceDTO.getServerId(), deployDir.getAbsolutePath())) {
            throw new ApplicationException(messages.get("service.deploydir.not-available"));
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
                AppUtils.putFile(serviceDTO.getServerId(), sshPort, runFile.getAbsolutePath(), serviceDTO.getDeployDir());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        vn.vccorp.servicemonitoring.entity.Service service = dozerBeanMapper.map(serviceDTO, vn.vccorp.servicemonitoring.entity.Service.class);
        service.setStartTime(AppUtils.getStartedDateOfProcess(service.getServer().getIp(), sshPort, service.getPID()));
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
        vn.vccorp.servicemonitoring.entity.Service service = serviceRepository.findById(serviceId).orElseThrow(() -> new ApplicationException(messages.get("service.id.not-found")));
        String startCommand = "ssh -p " + sshPort + " " + service.getServer().getIp()
                + " -t 'nohup sh " + service.getDeployDir() + getRunFileName(service.getName()) + "'";

        AppUtils.executeCommand(startCommand);

        String getPidCommand = "ssh -p " + sshPort + " " + service.getServer().getIp() + " -t 'cat " + service.getDeployDir() + "pid'";
        List<String> out = AppUtils.executeCommand(getPidCommand);
        if (out.isEmpty()) {
            throw new ApplicationException(messages.get("service.error.starting"));
        } else {
            service.setPID(out.get(0));
            serviceRepository.save(service);
        }
    }

    @Override
    public void stopService(int serviceId) {
        vn.vccorp.servicemonitoring.entity.Service service = serviceRepository.findById(serviceId).orElseThrow(() -> new ApplicationException(messages.get("service.id.not-found")));
        String command = "ssh -p " + sshPort + " " + service.getServer().getIp() + " -t 'kill -9 " + service.getPID() + "'; echo $?";
        List<String> out = AppUtils.executeCommand(command);
        if (out.isEmpty() || !out.get(0).equals("0")) {
            throw new ApplicationException(messages.get("service.error.stopping"));
        }
    }

    private boolean isFileExist(String serverIP, String filePath) {
        String command = "ssh -p " + sshPort + " " + serverIP + " -t 'test -f " + filePath + "'; echo $?";
        List<String> out = AppUtils.executeCommand(command);
        if (!out.isEmpty() && out.get(0).equals("0")) {
            return true;
        }
        return false;
    }

    private boolean isFolderExist(String serverIP, String filePath) {
        String command = "ssh -p " + sshPort + " " + serverIP + " -t 'test -d " + filePath + "'; echo $?";
        List<String> out = AppUtils.executeCommand(command);
        if (!out.isEmpty() && out.get(0).equals("0")) {
            return true;
        }
        return false;
    }

    private boolean isProcessAlive(String serverIP, String PID) {
        String command = "ssh -p " + sshPort + " " + serverIP + " -t 'ps -p " + PID + " > /dev/null'; echo $?";
        List<String> out = AppUtils.executeCommand(command);
        //if command execute success it will return 0
        if (!out.isEmpty() && out.get(0).equals("0")) {
            return true;
        }
        return false;
    }
    
    @Override
    public void deleteLog(int id) {
    	vn.vccorp.servicemonitoring.entity.Service service = serviceRepository.findById(id).orElseThrow(() -> new ApplicationException(messages.get("error.not.found.service")));
    	try {
    		String command = "ssh -p " + sshPort + " " + service.getServer().getIp() + " 'rm " + service.getLogDir() + service.getLogFile() + "; touch " + service.getLogDir() + service.getLogFile() + "'";
    		AppUtils.executeCommand(command);
    	} catch (Exception e) {
    		throw new ApplicationException(messages.get("error.delete.log.failed"));
    	}
        //if command execute success it will return
    }
    
}
