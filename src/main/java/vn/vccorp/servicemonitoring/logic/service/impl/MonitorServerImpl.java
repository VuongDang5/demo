package vn.vccorp.servicemonitoring.logic.service.impl;

import org.dozer.DozerBeanMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import vn.vccorp.servicemonitoring.dto.ServerDTO;
import vn.vccorp.servicemonitoring.dto.ShowServerDTO;
import vn.vccorp.servicemonitoring.entity.Server;
import vn.vccorp.servicemonitoring.entity.User;
import vn.vccorp.servicemonitoring.entity.UserServer;
import vn.vccorp.servicemonitoring.enumtype.Status;
import vn.vccorp.servicemonitoring.exception.ApplicationException;
import vn.vccorp.servicemonitoring.logic.repository.ServerRepository;
import vn.vccorp.servicemonitoring.logic.repository.UserRepository;
import vn.vccorp.servicemonitoring.logic.repository.UserServerRepository;
import vn.vccorp.servicemonitoring.logic.service.MonitorServer;
import vn.vccorp.servicemonitoring.message.Messages;
import vn.vccorp.servicemonitoring.utils.AppUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MonitorServerImpl implements MonitorServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(MonitorServerImpl.class);

    @Value("${ssh.port}")
    private String sshPort;
    @Value("${ssh.username}")
    private String sshUsername;
    @Autowired
    private Messages messages;
    @Autowired
    DozerBeanMapper dozerBeanMapper;
    @Autowired
    private ServerRepository serverRepository;
    @Autowired
    private UserServerRepository userServerRepository;
    @Autowired
    private UserRepository userRepository;

    @Override
    public void registerServer(ServerDTO serverDTO) {
        //check ssh server
        String commandSSH = "ssh -p " + sshPort + " " + sshUsername + "@" + serverDTO.getIp() + " -t 'exit'; echo $?";
        List<String> out = AppUtils.executeCommand(commandSSH);
        //if command execute success not return 0 error
        if (out.isEmpty() || !(out.get(0).equals("0"))) {
            throw new ApplicationException(messages.get("server.register.ssh-not-available", new String[]{serverDTO.getIp()}));
        }

        //check sudo user on server
        String commandSudo = "ssh -p " + sshPort + " " + sshUsername + "@" + serverDTO.getIp() + " -t 'sudo -nv'; echo $?";
        List<String> outSudo = AppUtils.executeCommand(commandSudo);
        //if command execute success not return 0 error
        if (outSudo.isEmpty() || !(outSudo.get(0).equals("0"))) {
            throw new ApplicationException(messages.get("server.register.not-sudo", new String[]{sshUsername, serverDTO.getIp()}));
        }

        //save server active status
        Server server = dozerBeanMapper.map(serverDTO, Server.class);
        server.setStatus(Status.ACTIVE);
        serverRepository.save(server);

        //save UserService
        List<UserServer> userServer = new ArrayList<>();
        List<User> allUser = userRepository.findAll();
        for (User user : allUser) {
            String groups = AppUtils.getGroupUser(server.getIp(), user.getUsername(), sshPort, sshUsername);
            if (groups == null) {
                continue;
            }
            userServer.add(new UserServer(server.getId(), user.getUsername(), user.getId(), groups));
        }
        userServerRepository.saveAll(userServer);
    }

    @Override
    public Page<ShowServerDTO> getAllServer(Pageable page) {
        List<ShowServerDTO> listInfoServer = new ArrayList<>();

        //Find all server
        List<Server> allServer = serverRepository.findAll();
        for (Server server : allServer) {
            //get task server
            Map<String, String> monitorServer = null;
            try {
                monitorServer = monitorServer(server, sshPort, sshUsername);
            } catch (InterruptedException e) {
                throw new ApplicationException(messages.get("server.get-all.error", new String[]{server.getIp()}));
            }

            //get user
            String userServer = "";
            for (UserServer user : userServerRepository.findAllByServerId(server.getId())) {
                if (userServer.equals(""))
                    userServer += user.getUsername();
                else
                    userServer += ",  " + user.getUsername();
            }

            listInfoServer.add(new ShowServerDTO().builder()
                    .STT(listInfoServer.size() + 1)
                    .name(server.getName())
                    .ip(server.getIp())
                    .description(server.getDescription())
                    .ramFree(monitorServer.get("ramFree"))
                    .ramUsed(monitorServer.get("ramUsed"))
                    .diskFree(monitorServer.get("diskFree"))
                    .diskUsed(monitorServer.get("diskUsed"))
                    .cpuUsed(monitorServer.get("cpuUsed"))
                    .speed(monitorServer.get("speed"))
                    .core(monitorServer.get("core"))
                    .user(userServer)
                    .status(server.getStatus().toString())
                    .build());
        }

        return new PageImpl<>(listInfoServer, page, page.getPageSize());
    }

    private Map<String, String> monitorServer(Server server, String sshPort, String sshUsername) throws InterruptedException {
        Map<String, String> monitorServer = new HashMap<String, String>();
        //get ram: men -h
        String commandRam = "ssh -p " + sshPort + " " + sshUsername + "@" + server.getIp() + " -t 'free -h | grep 'Mem:'' | awk '{print $3; print $7}'";
        List<String> outRam = AppUtils.executeCommand(commandRam);
        if (outRam.isEmpty()) {
            return null;
        }
        monitorServer.put("ramFree", outRam.get(0));
        monitorServer.put("ramUsed", outRam.get(1));

        //get disk: df -h
        String commandDisk = "ssh -p " + sshPort + " " + sshUsername + "@" + server.getIp() + " -t 'df -h' | awk '{print $3 \"-\" $4 \"-\" $6}'";
        List<String> outDisk = AppUtils.executeCommand(commandDisk);
        if (outDisk.isEmpty()) {
            return null;
        }
        for (String diskInfo : outDisk) {
            String[] disk = diskInfo.split("-", 3);
            //check correct path root
            if (!disk[2].equals(server.getRootPath())) {
                continue;
            }
            monitorServer.put("diskFree", disk[0]);
            monitorServer.put("diskUsed", disk[1]);
        }

        //get cpu; grep 'cpu' /proc/stat
        String commandCpu = "ssh -p " + sshPort + " " + sshUsername + "@" + server.getIp() + " -t 'cat <(grep 'cpu' /proc/stat) <(sleep 1 && grep 'cpu' /proc/stat)'";
        List<String> outCpu = AppUtils.executeCommand(commandCpu);
        if (outCpu.isEmpty()) {
            return null;
        }
        String[] prevCpu = outCpu.get(0).split(" ");
        String[] cpu = outCpu.get(5).split(" ");
        float prevTotal=0, total=0;
        for (int i = 2; i < cpu.length; i++){
            prevTotal += Float.valueOf(prevCpu[i]);
            total += Float.valueOf(cpu[i]);
        }
        float cpuPresent = (1-(Float.valueOf(cpu[5]) - Float.valueOf(prevCpu[5]))/(total - prevTotal))*100;
        monitorServer.put("cpuUsed", String.valueOf(cpuPresent)+"%");

        //get speed, core; /proc/cpuinfo | grep GHz
        String commandSpeed = "ssh -p " + sshPort + " " + sshUsername + "@" + server.getIp() + " -t 'cat /proc/cpuinfo | grep GHz'";
        List<String> outSpeed = AppUtils.executeCommand(commandSpeed);
        if (outSpeed.isEmpty()) {
            return null;
        }
        String[] speed = outSpeed.get(0).split(" ");
        monitorServer.put("speed", speed[speed.length-1]);
        monitorServer.put("core", String.valueOf(outSpeed.size()));

        return monitorServer;
    }
}



