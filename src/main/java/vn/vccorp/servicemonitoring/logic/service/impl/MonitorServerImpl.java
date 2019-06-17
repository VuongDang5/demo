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
import vn.vccorp.servicemonitoring.dto.PortDTO;
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

import javax.sound.sampled.Port;
import java.math.RoundingMode;
import java.text.DecimalFormat;
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

        //check disk root n server
            String commandoDisk = "ssh -p " + sshPort + " " + sshUsername + "@" + serverDTO.getIp() + " -t '[ -d "+ serverDTO.getRootPath() +" ] && echo '0' || echo '1''";
        List<String> outDisk = AppUtils.executeCommand(commandoDisk);
        //if command execute success not return 0 error
        if (outDisk.isEmpty() || !(outDisk.get(0).equals("0"))) {
            throw new ApplicationException(messages.get("server.register.path-root", new String[]{serverDTO.getRootPath(), serverDTO.getIp()}));
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
            String userServer = userServerRepository.findNameByServerId(server.getId());

            listInfoServer.add(new ShowServerDTO().builder()
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

    @Override
    public String checkIfPortIsBeingUsed(PortDTO info) {
        boolean result = AppUtils.isPortUsed(info.getPort(),info.getServerIP(),info.getSshPort(),info.getSshUsername());
        if(result) {
            return "Port " + info.getPort() + " is being used";
        }
        else {
            return "Port " + info.getPort() + " is not being used";
        }
    }

    @Override
    public List<String> getAllPort(PortDTO info) {
        return AppUtils.getAllListeningPort(info.getServerIP(), info.getSshPort(), info.getSshUsername());
    }

    private Map<String, String> monitorServer(Server server, String sshPort, String sshUsername) throws InterruptedException {
        Map<String, String> monitorServer = new HashMap<String, String>();
        //get ram: men -h
        String commandRam = "ssh -p " + sshPort + " " + sshUsername + "@" + server.getIp() + " -t 'free -h | grep 'Mem:'' | awk '{print $3; print $7}'";
        List<String> outRam = AppUtils.executeCommand(commandRam);
        if (outRam.isEmpty()) {
            throw new ApplicationException(messages.get("server.get-ram.error", new String[]{server.getIp()}));
        }
        monitorServer.put("ramUsed", outRam.get(0));
        monitorServer.put("ramFree", outRam.get(1));

        //get disk: df -h
        String commandDisk = "ssh -p " + sshPort + " " + sshUsername + "@" + server.getIp() + " -t 'df -hT "+ server.getRootPath() +"' | awk '{if ($1 == \"df:\") print \"-1\"; print $4; print $5}'";
        List<String> outDisk = AppUtils.executeCommand(commandDisk);
        //if not found path error return -1
        if (outDisk.get(0).equals("-1")) {
            throw new ApplicationException(messages.get("server.get-disk.error", new String[]{server.getIp(), server.getRootPath()}));
        }
        monitorServer.put("diskUsed", outDisk.get(2));
        monitorServer.put("diskFree", outDisk.get(3));

        //get cpu; grep 'cpu' /proc/stat
        String commandCpu = "ssh -p " + sshPort + " " + sshUsername + "@" + server.getIp() + " -t 'cat <(grep 'cpu' /proc/stat) <(sleep 1 && grep 'cpu' /proc/stat)'";
        List<String> outCpu = AppUtils.executeCommand(commandCpu);
        if (outCpu.isEmpty()) {
            throw new ApplicationException(messages.get("server.get-cpu-present.error", new String[]{server.getIp()}));
        }
        String[] prevCpu = outCpu.get(0).split(" ");
        String[] cpu = outCpu.get(outCpu.size()/2).split(" ");
        float prevTotal=0, total=0;
        for (int i = 2; i < cpu.length; i++){
            prevTotal += Float.valueOf(prevCpu[i]);
            total += Float.valueOf(cpu[i]);
        }
        float cpuPresent = (1-(Float.valueOf(cpu[5]) - Float.valueOf(prevCpu[5]))/(total - prevTotal))*100;
        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.CEILING);
        monitorServer.put("cpuUsed", String.valueOf(df.format(cpuPresent))+"%");

        //get speed, core; /proc/cpuinfo | grep GHz
        String commandSpeed = "ssh -p " + sshPort + " " + sshUsername + "@" + server.getIp() + " -t 'cat /proc/cpuinfo | grep GHz'";
        List<String> outSpeed = AppUtils.executeCommand(commandSpeed);
        if (outSpeed.isEmpty()) {
            throw new ApplicationException(messages.get("server.get-speed.error", new String[]{server.getIp()}));
        }
        String[] speed = outSpeed.get(0).split(" ");
        monitorServer.put("speed", speed[speed.length-1]);
        monitorServer.put("core", String.valueOf(outSpeed.size()));

        return monitorServer;
    }
}