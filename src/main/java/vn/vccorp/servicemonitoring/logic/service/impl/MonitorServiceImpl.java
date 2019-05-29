/**
 * Created by: tuyennta
 * Created on: 20/05/2019 10:15
 */

package vn.vccorp.servicemonitoring.logic.service.impl;

import org.dozer.DozerBeanMapper;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
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
import vn.vccorp.servicemonitoring.security.CustomPermissionEvaluator;
import vn.vccorp.servicemonitoring.utils.AppUtils;
import vn.vccorp.servicemonitoring.utils.BeanUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Inet4Address;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MonitorServiceImpl implements MonitorService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MonitorServiceImpl.class);

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

        //check if service with specified info is correct on the system
        if (!isProcessAlive(serviceDTO.getServerIp(), serviceDTO.getPid())) {
            throw new ApplicationException(messages.get("service.pid.not-available", new String[]{serviceDTO.getPid(), serviceDTO.getServerIp()}));
        }
        //check if log file is available
        File logFile = new File(serviceDTO.getLogDir() + serviceDTO.getLogFile());
        if (!isFileExist(serviceDTO.getServerIp(), logFile.getAbsolutePath())) {
            throw new ApplicationException(messages.get("service.log.not-available", new String[]{logFile.getAbsolutePath(), serviceDTO.getServerIp()}));
        }
        //check if deploy dir is available
        createDeployFile(serviceDTO, server.getIp());

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

    /**
     * Concat name to a prefix and postfix to get a run file
     *
     * @param name file name
     * @return final file name
     */
    private String getRunFileName(String name) {
        return "run-" + name + ".sh";
    }

    @Override
    public void startService(int serviceId) {
        //check if service is existed in db
        vn.vccorp.servicemonitoring.entity.Service service = serviceRepository.findById(serviceId).orElseThrow(() -> new ApplicationException(messages.get("service.id.not-found")));

        //check if service is already run then we do nothing
        if (isServiceRunning(String.valueOf(serviceId), service.getPid())) {
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

    private boolean isServiceRunning(String serverId, String pid) {
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

    @Transactional
    @Override
    public void deployService(ServiceDTO serviceDTO, Integer currentUserId, MultipartFile jar, MultipartFile originalJar, MultipartFile dependencies, MultipartFile modelFile, MultipartFile sourceCode, MultipartFile dockerFile) {
        //get server info from serverId
        Server server = serverRepository.findById(serviceDTO.getServerId())
                .orElseThrow(() -> new ApplicationException(messages.get("service.server.not-available", new String[]{String.valueOf(serviceDTO.getServerId())})));

        //if re-deploy old service
        if (serviceDTO.getId() != null) {
            vn.vccorp.servicemonitoring.entity.Service oldService = serviceRepository.findById(serviceDTO.getId()).orElseThrow(() -> new ApplicationException(messages.get("service.id.not-found")));

            //check permission of current user on the service
            if (!BeanUtils.getBean(CustomPermissionEvaluator.class).forService(Arrays.asList(Role.ADMIN, Role.OWNER, Role.MAINTAINER), serviceDTO.getId())) {
                throw new ApplicationException(messages.get("service.user.not-authorize"));
            }

            //stop old service
            stopService(serviceDTO.getId());
        }

        //create deploy dir
        mkdir(serviceDTO.getDeployDir(), server.getIp());

        //create log dir
        mkdir(serviceDTO.getLogDir(), server.getIp());

        //process upload deploy file
        //all upload files will be place in deploy directory of service on remote server where the service will be deployed
        processUploadFile(server.getIp(), serviceDTO.getDeployDir(), jar, false, true);
        processUploadFile(server.getIp(), serviceDTO.getDeployDir(), originalJar, false, true);
        processUploadFile(server.getIp(), serviceDTO.getDeployDir(), dependencies, true, false);
        processUploadFile(server.getIp(), serviceDTO.getDeployDir(), modelFile, true, true);
        //TODO check deploy file with docker later
        processUploadFile(server.getIp(), serviceDTO.getDeployDir(), dockerFile, false, true);

        //process register service
        createDeployFile(serviceDTO, server.getIp());

        //save service info
        vn.vccorp.servicemonitoring.entity.Service service = dozerBeanMapper.map(serviceDTO, vn.vccorp.servicemonitoring.entity.Service.class);
        service.setStartTime(LocalDateTime.now().toDate());
        service.setServer(server);
        serviceRepository.save(service);

        //save UserService
        List<UserService> userServices = serviceDTO.getMaintainerIds()
                .parallelStream().map(id -> new UserService(id, service.getId(), Role.MAINTAINER)).collect(Collectors.toList());
        userServices.add(new UserService(serviceDTO.getOwnerId(), service.getId(), Role.OWNER));
        userServiceRepository.saveAll(userServices);

        //start service
        startService(service.getId());
    }

    /**
     * Create an executable file to run service
     *
     * @param serviceDTO service info
     * @param serverIp
     */
    private void createDeployFile(ServiceDTO serviceDTO, String serverIp) {
        File deployDir = new File(serviceDTO.getDeployDir());
        if (!isFolderExist(serverIp, deployDir.getAbsolutePath())) {
            throw new ApplicationException(messages.get("service.deploydir.not-available", new String[]{deployDir.getAbsolutePath(), serverIp}));
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
                AppUtils.putFile(serverIp, sshUsername, sshPort, runFile.getAbsolutePath(), serviceDTO.getDeployDir());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * Processing an upload file to a destination remote server
     *
     * @param serverIp          server where to upload file
     * @param destinationFolder folder on remote server where to store file
     * @param file              upload file
     * @param willExtract       if upload file is a compressed file and need to be extracted then set this parameter to true otherwise false
     * @param willExecute       if upload file is an executable file then set this parameter to true otherwise false
     */
    private void processUploadFile(String serverIp, String destinationFolder, MultipartFile file, boolean willExtract, boolean willExecute) {
        if (file != null) {
            Path path = Paths.get(destinationFolder + file.getOriginalFilename());
            try {
                Files.write(path, file.getBytes());
                //if we deploy service on a difference server then we must upload file to that server
                if (!Inet4Address.getLocalHost().getHostAddress().equals(serverIp)) {
                    AppUtils.putFile(serverIp, sshUsername, sshPort, path.toString(), destinationFolder);
                    //delete local file to save storage
                    Files.delete(path);
                }
                //if we need to extract this file
                if (willExtract) {
                    extract(path.toString(), serverIp);
                }
                //if this file is an executable file
                if (willExecute) {
                    chmod(path.toString(), serverIp, 777);
                }
            } catch (IOException e) {
                LOGGER.error("Exception while putting file to target server", e);
            }
        }
    }

    /**
     * Create directory on target server
     *
     * @param dir      absolute path to directory need to create
     * @param serverIp server where to create directory
     */
    private void mkdir(String dir, String serverIp) {
        String commandPrefix = "ssh -p " + sshPort + " " + sshUsername + "@" + serverIp + " -t '";
        String mkdirCmd = "sudo mkdir " + " " + dir + " -p'; echo $?";
        AppUtils.executeCommand(commandPrefix + mkdirCmd);
        if (dir.lastIndexOf("/") == dir.length() - 1) {
            dir = dir.substring(0, dir.length() - 1);
        }
        dir = dir.substring(0, dir.lastIndexOf("/"));
        String chownCmd = "sudo chown " + sshUsername + ":" + sshUsername + " -R " + dir + "'; echo $?";
        AppUtils.executeCommand(commandPrefix + chownCmd);
    }

    /**
     * Change mode of a file or directory to specified mod on a remote serverIp
     *
     * @param file     file or directory to change
     * @param serverIp server where the file is located
     * @param mod      mode to change
     */
    private void chmod(String file, String serverIp, int mod) {
        String command = "ssh -p " + sshPort + " " + sshUsername + "@" + serverIp + " -t '" +
                "sudo chmod " + mod + " " + file + " -r'; echo $?";
        AppUtils.executeCommand(command);
    }

    /**
     * Extract a compressed file on a remote server
     * Note: compressed file must be in format *.tar.gz
     *
     * @param file     compressed file need to extract
     * @param serverIp server where file is located
     */
    private void extract(String file, String serverIp) {
        if (!file.endsWith(".tar.gz")) {
            throw new ApplicationException(messages.get("service.upload.invalid-file-compress"));
        }
        String command = "ssh -p " + sshPort + " " + sshUsername + "@" + serverIp + " -t '" +
                "tar -xzvf " + file + "'; echo $?";
        List<String> out = AppUtils.executeCommand(command);
        if (!out.isEmpty() && out.get(0).equals("0")) {
            LOGGER.info("File upload and extract success");
        } else {
            LOGGER.info("File upload and extract fail");
        }
    }

    /**
     * Check if a file is exist on remote server or not
     *
     * @param serverIP server to check
     * @param filePath file to check
     * @return true if file is existed otherwise false
     */
    private boolean isFileExist(String serverIP, String filePath) {
        String command = "ssh -p " + sshPort + " " + sshUsername + "@" + serverIP + " -t 'test -f " + filePath + "'; echo $?";
        List<String> out = AppUtils.executeCommand(command);
        if (!out.isEmpty() && out.get(0).equals("0")) {
            return true;
        }
        return false;
    }

    /**
     * Check if a folder is existed on a remote server
     *
     * @param serverIP server to check
     * @param filePath folder to check
     * @return true if folder existed otherwise false
     */
    private boolean isFolderExist(String serverIP, String filePath) {
        String command = "ssh -p " + sshPort + " " + sshUsername + "@" + serverIP + " -t 'test -d " + filePath + "'; echo $?";
        List<String> out = AppUtils.executeCommand(command);
        if (!out.isEmpty() && out.get(0).equals("0")) {
            return true;
        }
        return false;
    }

    /**
     * Check if a process is alive on a remote server using pid of process
     *
     * @param serverIP server to check
     * @param PID      process id to check
     * @return true if process is alive otherwise false
     */
    private boolean isProcessAlive(String serverIP, String PID) {
        String command = "ssh -p " + sshPort + " " + sshUsername + "@" + serverIP + " -t 'ps -p " + PID + " > /dev/null'; echo $?";
        List<String> out = AppUtils.executeCommand(command);
        //if command execute success it will return 0
        if (!out.isEmpty() && out.get(0).equals("0")) {
            return true;
        }
        return false;
    }
}
