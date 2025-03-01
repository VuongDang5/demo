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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import vn.vccorp.servicemonitoring.dto.LogServiceDTO;
import vn.vccorp.servicemonitoring.dto.ServiceDTO;
import vn.vccorp.servicemonitoring.dto.ServiceDetailsDTO.*;
import vn.vccorp.servicemonitoring.dto.ServiceInfoDTO;
import vn.vccorp.servicemonitoring.entity.Server;
import vn.vccorp.servicemonitoring.entity.User;
import vn.vccorp.servicemonitoring.entity.UserService;
import vn.vccorp.servicemonitoring.enumtype.ApplicationError;
import vn.vccorp.servicemonitoring.enumtype.IssueType;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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
    @Value("${path.upload}")
    private String uploadDir;
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
    @Autowired
    private ServiceRepository ServiceRepositoryCustom;

    @Transactional
    @Override
    public void registerService(ServiceDTO serviceDTO) {
        //get server info from serverId
        Server server = serverRepository.findById(serviceDTO.getServerId())
                .orElseThrow(() -> new ApplicationException(messages.get("service.server.not-available", new String[]{String.valueOf(serviceDTO.getServerId())})));

        //check if service with specified info is correct on the target server
        if (StringUtils.isEmpty(serviceDTO.getPid())) {
            if (StringUtils.isEmpty(serviceDTO.getServerPort())) {
                throw new ApplicationException(messages.get("service.info.missing"));
            } else {
                String pid = AppUtils.getPidFromPort(server.getIp(), serviceDTO.getServerPort(), sshPort, sshUsername);
                if (StringUtils.isEmpty(pid)) {
                    throw new ApplicationException(messages.get("service.pid.not-available", new String[]{serviceDTO.getServerPort(), server.getIp()}));
                } else {
                    serviceDTO.setPid(pid);
                }
            }
        } else {
            if (StringUtils.isEmpty(serviceDTO.getServerPort())) {
                String port = AppUtils.getPortFromPid(server.getIp(), serviceDTO.getPid(), sshPort, sshUsername);
                if (StringUtils.isEmpty(port)) {
                    throw new ApplicationException(messages.get("service.port.not-available", new String[]{serviceDTO.getPid(), server.getIp()}));
                }
                serviceDTO.setServerPort(port);
            } else {
                String pid = AppUtils.getPidFromPort(server.getIp(), serviceDTO.getServerPort(), sshPort, sshUsername);
                if (StringUtils.isEmpty(pid) || !pid.equals(serviceDTO.getPid())) {
                    throw new ApplicationException(messages.get("service.info.incorrect", new String[]{serviceDTO.getPid(), serviceDTO.getServerPort(), server.getIp()}));
                }
            }
        }

        //check if log file is available
        File logFile = new File(serviceDTO.getLogDir() + serviceDTO.getLogFile());
        if (!AppUtils.isFileExist(server.getIp(), logFile.getAbsolutePath(), sshPort, sshUsername)) {
            throw new ApplicationException(messages.get("service.log.not-available", new String[]{logFile.getAbsolutePath(), server.getIp()}));
        }
        //check if deploy dir is available
        createStartServiceScript(serviceDTO, server.getIp());

        vn.vccorp.servicemonitoring.entity.Service service = dozerBeanMapper.map(serviceDTO, vn.vccorp.servicemonitoring.entity.Service.class);
        service.setStartTime(AppUtils.getStartedDateOfProcess(server.getIp(), sshUsername, sshPort, service.getPid()));
        service.setServer(server);
        serviceRepository.save(service);

        //save UserService
        List<UserService> userServices = new ArrayList<>();
        userServices.add(new UserService(serviceDTO.getOwnerId(), service.getId(), Role.OWNER));
        if (!CollectionUtils.isEmpty(serviceDTO.getMaintainerIds())) {
            userServices.addAll(serviceDTO.getMaintainerIds().parallelStream()
                    .map(id -> new UserService(id, service.getId(), Role.MAINTAINER)).collect(Collectors.toList()));
        }
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

        //check if service pid is already run then we do nothing
        if (AppUtils.isProcessAlive(service.getServer().getIp(), service.getPid(), sshPort, sshUsername)) {
            return;
        } else {
            //check if service is still running on it's port then update pid
            String newPid = AppUtils.getPidFromPort(service.getServer().getIp(), service.getServerPort(), sshPort, sshUsername);
            if (!org.apache.commons.lang3.StringUtils.isEmpty(newPid)) { //if we can not get pid from port, then service is died
                service.setPid(newPid);
                serviceRepository.save(service);
                return;
            }
        }

        String startCommand = "ssh -p " + sshPort + " " + sshUsername + "@" + service.getServer().getIp()
                + " -t 'nohup sh " + service.getDeployDir() + getRunFileName(service.getName()) + " &'";

        AppUtils.executeCommand(startCommand);

        String getPidCommand = "ssh -p " + sshPort + " " + sshUsername + "@" + service.getServer().getIp() + " -t 'cat " + service.getDeployDir() + "pid'";

        List<String> out = AppUtils.executeCommand(getPidCommand);
        if (out.isEmpty()) {
            throw new ApplicationException(messages.get("service.error.starting"));
        } else {
            service.setPid(out.get(0));
//            String port = AppUtils.getPortFromPid(service.getServer().getIp(), service.getPid(), sshPort, sshUsername);
//            if (StringUtils.isEmpty(port)) {
//                LOGGER.error(messages.get("service.port.not-available", new String[]{service.getPid(), service.getServer().getIp()}));
//            } else {
//                service.setServerPort(port);
//            }
            serviceRepository.save(service);
        }
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
    public void deployService(ServiceDTO serviceDTO, Integer currentUserId, MultipartFile jar, MultipartFile originalJar, MultipartFile dependencies, MultipartFile modelFile, MultipartFile sourceCode, MultipartFile dockerFile) throws IOException {
        //get server info from serverId
        Server server = serverRepository.findById(serviceDTO.getServerId())
                .orElseThrow(() -> new ApplicationException(messages.get("service.server.not-available", new String[]{String.valueOf(serviceDTO.getServerId())})));

        //if re-deploy old service
        if (serviceDTO.getId() != null) {
            vn.vccorp.servicemonitoring.entity.Service oldService = serviceRepository.findById(serviceDTO.getId()).orElseThrow(() -> new ApplicationException(messages.get("service.id.not-found")));

            //check permission of current user on the service
            if (!BeanUtils.getBean(CustomPermissionEvaluator.class).forServices(Arrays.asList(Role.ADMIN, Role.OWNER, Role.MAINTAINER), serviceDTO.getId())) {
                throw new ApplicationException(messages.get("service.user.not-authorize"));
            }

            //stop old service
            stopService(oldService.getId());
        }

        //create deploy dir
        AppUtils.mkdir(serviceDTO.getDeployDir(), server.getIp(), sshPort, sshUsername);

        //create log dir
        AppUtils.mkdir(serviceDTO.getLogDir(), server.getIp(), sshPort, sshUsername);

        //process upload deploy file
        //all upload files will be place in deploy directory of service on remote server where the service will be deployed
        processUploadFile(server.getIp(), serviceDTO.getDeployDir(), jar, false, true);
        processUploadFile(server.getIp(), serviceDTO.getDeployDir(), originalJar, false, true);
        processUploadFile(server.getIp(), serviceDTO.getDeployDir(), dependencies, true, false);
        processUploadFile(server.getIp(), serviceDTO.getDeployDir(), modelFile, true, true);
        //TODO check deploy file with docker later
        processUploadFile(server.getIp(), serviceDTO.getDeployDir(), dockerFile, false, true);

        //process register service
        createStartServiceScript(serviceDTO, server.getIp());

        //save service info
        vn.vccorp.servicemonitoring.entity.Service service = dozerBeanMapper.map(serviceDTO, vn.vccorp.servicemonitoring.entity.Service.class);
        service.setStartTime(LocalDateTime.now().toDate());
        service.setServer(server);
        serviceRepository.save(service);

        //save UserService
        List<UserService> userServices = new ArrayList<>();
        userServices.add(new UserService(serviceDTO.getOwnerId(), service.getId(), Role.OWNER));
        if (!CollectionUtils.isEmpty(serviceDTO.getMaintainerIds())) {
            userServices = serviceDTO.getMaintainerIds()
                    .parallelStream().map(id -> new UserService(id, service.getId(), Role.MAINTAINER)).collect(Collectors.toList());
        }
        userServiceRepository.saveAll(userServices);

        //start service
        startService(service.getId());
    }

    /**
     * Create an executable file to run service
     *
     * @param serviceDTO service info
     * @param serverIp   server where to place execute script
     */
    private void createStartServiceScript(ServiceDTO serviceDTO, String serverIp) {
        File deployDir = new File(serviceDTO.getDeployDir());
        if (!AppUtils.isFolderExist(serverIp, deployDir.getAbsolutePath(), sshPort, sshUsername)) {
            throw new ApplicationException(messages.get("service.deploydir.not-available", new String[]{deployDir.getAbsolutePath(), serverIp}));
        } else {
            //create a file on deploy dir to run service
            String deployCommand = "#!/bin/bash \n";
            deployCommand += "cd " + serviceDTO.getDeployDir() + " \n";
            deployCommand += serviceDTO.getDeployCommand() + "\n";
            deployCommand += "sleep 0.1 \n";
            deployCommand += "echo $! > pid";
            File upload = new File(uploadDir);
            if (!upload.exists()) {
                upload.mkdirs();
            }
            File runFile = new File(uploadDir + getRunFileName(serviceDTO.getName()));
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
    private void processUploadFile(String serverIp, String destinationFolder, MultipartFile file, boolean willExtract, boolean willExecute) throws IOException {
        if (file != null) {
            File upload = new File(uploadDir);
            if (!upload.exists()) {
                upload.mkdirs();
            }
            Path path = Paths.get(uploadDir + file.getOriginalFilename());
            Files.write(path, file.getBytes());
            //upload file to target server
            AppUtils.putFile(serverIp, sshUsername, sshPort, path.toString(), destinationFolder);
            //delete local file to save storage
            try {
                Files.delete(path);
            } catch (IOException e) {
                LOGGER.error("Exception while deleting uploaded file on local", e);
            }
            //if we need to extract this file
            if (willExtract) {
                extract(destinationFolder + file.getOriginalFilename(), serverIp);
            }
            //if this file is an executable file
            if (willExecute) {
                AppUtils.chmod(destinationFolder + file.getOriginalFilename(), serverIp, 777, sshPort, sshUsername);
            }
        }
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

    @Override
    public List<String> getLogService(LogServiceDTO logServiceDTO) {
        vn.vccorp.servicemonitoring.entity.Service service = serviceRepository.findById(logServiceDTO.getServiceId()).orElseThrow(() -> new ApplicationException(messages.get("service.id.not-found")));
        //check if log file is available
        File logRemoteFile = new File(service.getLogDir() + service.getLogFile());
        if (!AppUtils.isFileExist(service.getServer().getIp(), logRemoteFile.getAbsolutePath(), sshPort, sshUsername)) {
            throw new ApplicationException(messages.get("service.log.not-available"));
        }
        String command;
        if (logServiceDTO.getEnd() == -1) {
            int limit = 1000;
            if (logServiceDTO.getStart() != 0) {
                limit = (int) AppUtils.getLastLine(service.getServer().getIp(), logRemoteFile.getAbsolutePath(), sshPort, sshUsername) - logServiceDTO.getStart();
                if (limit == -1) {
                    throw new ApplicationException(messages.get("service.log.error"));
                }
            }
            command = "ssh -p " + sshPort + " " + sshUsername + "@" + service.getServer().getIp() + " -t 'sudo tail -n " + limit + " " + logRemoteFile.getAbsolutePath() + "'";
        } else {
            command = "ssh -p " + sshPort + " " + sshUsername + "@" + service.getServer().getIp()
                    + " -t 'sudo sed -n '" + logServiceDTO.getStart() + "," + logServiceDTO.getEnd() + "p' " + logRemoteFile.getAbsolutePath() + "'";
        }

        return AppUtils.executeCommand(command);
    }

    @Override
    public void deleteLog(int id) {
        vn.vccorp.servicemonitoring.entity.Service service = serviceRepository.findById(id).orElseThrow(() -> new ApplicationException(messages.get("error.not.found.service")));
        String command = "ssh -p " + sshPort + " " + sshUsername + "@" + service.getServer().getIp() + " 'rm " + service.getLogDir() + service.getLogFile() + "; touch " + service.getLogDir() + service.getLogFile() + "'";
        AppUtils.executeCommand(command);
    }

    @Override
    public Page<ServiceInfoDTO> showAllService(int currentPage, int pageSize) {
        //dung pageable de them currentPage va Pagesize vao Page
        Pageable pageNumber = PageRequest.of(currentPage, pageSize);
        Page<ServiceInfoDTO> results = ServiceRepositoryCustom.showAllService(pageNumber);
        //kieu tra ve Pagination
        return results;
    }

    @Override
    public ServiceDetailsDTO showService(int serviceId) {
        ServiceDetailsDTO s = new ServiceDetailsDTO();
        ServiceInfo serviceInfo = ServiceRepositoryCustom.getServiceInfo(serviceId);
        if(serviceInfo!=null) {
            ServerInfo serverInfo = ServiceRepositoryCustom.getServerInfo(serviceId);
            List<UserInfo> userInfo = ServiceRepositoryCustom.getAllUser(serviceId);

            Page<SnapshotInfo> snapshotInfoPage = ServiceRepositoryCustom.getAllSnapshot(serviceId, new Pageable() {
                @Override
                public int getPageNumber() {
                    return 0;
                }

                @Override
                public int getPageSize() {
                    return 10;
                }

                @Override
                public long getOffset() {
                    return 0;
                }

                @Override
                public Sort getSort() {
                    return null;
                }

                @Override
                public Pageable next() {
                    return null;
                }

                @Override
                public Pageable previousOrFirst() {
                    return null;
                }

                @Override
                public Pageable first() {
                    return null;
                }

                @Override
                public boolean hasPrevious() {
                    return false;
                }
            });
            List<SnapshotInfo> snapshotInfo = snapshotInfoPage.getContent();

            Page<IssueInfo> issueInfoPage = ServiceRepositoryCustom.getAllIssue(serviceId, new Pageable() {
                @Override
                public int getPageNumber() {
                    return 0;
                }

                @Override
                public int getPageSize() {
                    return 10;
                }

                @Override
                public long getOffset() {
                    return 0;
                }

                @Override
                public Sort getSort() {
                    return null;
                }

                @Override
                public Pageable next() {
                    return null;
                }

                @Override
                public Pageable previousOrFirst() {
                    return null;
                }

                @Override
                public Pageable first() {
                    return null;
                }

                @Override
                public boolean hasPrevious() {
                    return false;
                }
            });
            List<IssueInfo> issueInfo = issueInfoPage.getContent();
            s.setName(serviceInfo.getName());
            s.setDescription(serviceInfo.getDescription());
            s.setServerPort(serviceInfo.getServerPort());
            s.setPid(serviceInfo.getPid());
            s.setDeployDir(serviceInfo.getDeployDir());
            s.setLogDir(serviceInfo.getLogDir());
            s.setLogFile(serviceInfo.getLogFile());
            s.setLanguage(serviceInfo.getLanguage());
            s.setDeployCommand(serviceInfo.getDeployCommand());
            s.setRamLimit(serviceInfo.getRamLimit());
            s.setCpuLimit(serviceInfo.getCpuLimit());
            s.setGpuLimit(serviceInfo.getGpuLimit());
            s.setDiskLimit(serviceInfo.getDiskLimit());
            s.setStatus(serviceInfo.getStatus());
            s.setStartTime(serviceInfo.getStartTime());
            s.setLastCheckTime(serviceInfo.getLastCheckTime());
            s.setProject(serviceInfo.getProject());
            s.setApiEndpoint(serviceInfo.getApiEndpoint());
            s.setKongMapping(serviceInfo.getKongMapping());
            s.setNote(serviceInfo.getNote());

            s.setServerInfo(serverInfo);
            s.setUserInfo(userInfo);
            s.setSnapshotInfo(snapshotInfo);
            s.setIssueInfo(issueInfo);
        }
        return s;
    }
    
    @Override
    public void addServiceOwner(int userId, int serviceId, Role role) {
        UserService userService = userServiceRepository.findByUserIdAndServiceId(userId, serviceId);
        if (userService != null) {
            // update role
            List<UserService> user = userServiceRepository.findAllByRoleAndServiceId(Role.OWNER, serviceId);
            //Check Unique Owner
            if (user.size() == 1 && user.get(0).getUser().getId() == userId && role == Role.MAINTAINER) {
                throw new ApplicationException(messages.get("error.cannot.change.owner"));
            } else {
                userService.setRole(role);
                userServiceRepository.save(userService);
            }
        } else {
            // add role
            userService = new UserService(userId, serviceId, role);
            userServiceRepository.save(userService);
        }
    }

    @Override
    public void editService(int serviceId, ServiceDTO serviceDTO) {
        vn.vccorp.servicemonitoring.entity.Service service = serviceRepository.findById(serviceId).orElseThrow(() -> new ApplicationException(messages.get("error.not.found.service")));
        if (serviceDTO.getName() != null) {
            service.setName(serviceDTO.getName());
        }
        if (serviceDTO.getDescription() != null) {
            service.setDescription(serviceDTO.getDescription());
        }
        if (serviceDTO.getRamLimit() != null) {
            service.setRamLimit(serviceDTO.getRamLimit());
        }
        if (serviceDTO.getCpuLimit() != null) {
            service.setCpuLimit(serviceDTO.getCpuLimit());
        }
        if (serviceDTO.getGpuLimit() != null) {
            service.setGpuLimit(serviceDTO.getGpuLimit());
        }
        if (serviceDTO.getDiskLimit() != null) {
            service.setDiskLimit(serviceDTO.getDiskLimit());
        }
        serviceRepository.save(service);
    }

}
