package vn.vccorp.servicemonitoring.logic.service.impl;

import org.apache.commons.collections.CollectionUtils;
import org.dozer.DozerBeanMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.vccorp.servicemonitoring.dto.ServerDTO;
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
import java.util.List;

@Service
public class MonitorServerImpl implements MonitorServer{
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
            throw new ApplicationException(messages.get("server.register.ssh.failed", new String[]{serverDTO.getIp()}));
        }

        //check sudo user on server
        String commandSudo = "ssh -p " + sshPort + " " + sshUsername + "@" + serverDTO.getIp() + " -t 'sudo -nv'; echo $?";
        List<String> outSudo = AppUtils.executeCommand(commandSudo);
        //if command execute success not return 0 error
        if (outSudo.isEmpty() || !(outSudo.get(0).equals("0"))) {
            throw new ApplicationException(messages.get("server.register.not.sudoUser", new String[]{serverDTO.getIp()}));
        }

        //save server active status
        Server server = dozerBeanMapper.map(serverDTO, Server.class);
        server.setStatus(Status.ACTIVE);
        serverRepository.save(server);

        //save UserService
        List<UserServer> userServer = new ArrayList<>();
        List<User> allUser = userRepository.findAll();
        for (User user : allUser) {
            String command = "ssh -p " + sshPort + " " + sshUsername + "@" + serverDTO.getIp() + " -t 'groups " + user.getUsername() + "'";
            List<String> outUser = AppUtils.executeCommand(command);
            if (outUser.isEmpty()) {
                continue;
            }
            String groups = outUser.get(0).split(user.getUsername()+" : ", 2)[1].replace(" ", ",");
            userServer.add(new UserServer(server.getId(), user.getUsername(), user.getId(), groups));
        }
        userServerRepository.saveAll(userServer);
    }
}


