/**
 * Created by: tuyennta
 * Created on: 10/05/2019 22:07
 */

package vn.vccorp.servicemonitoring.logic.service.impl;

import org.dozer.DozerBeanMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.vccorp.servicemonitoring.config.RootConfig;
import vn.vccorp.servicemonitoring.dto.ConfigurationDTO;
import vn.vccorp.servicemonitoring.dto.UserDTO;
import vn.vccorp.servicemonitoring.dto.UserInfoDTO;
import vn.vccorp.servicemonitoring.entity.Configuration;
import vn.vccorp.servicemonitoring.entity.Server;
import vn.vccorp.servicemonitoring.entity.User;
import vn.vccorp.servicemonitoring.entity.UserServer;
import vn.vccorp.servicemonitoring.enumtype.ApplicationError;
import vn.vccorp.servicemonitoring.enumtype.Role;
import vn.vccorp.servicemonitoring.exception.ApplicationException;
import vn.vccorp.servicemonitoring.logic.repository.*;
import vn.vccorp.servicemonitoring.logic.service.EmailService;
import vn.vccorp.servicemonitoring.logic.service.UserService;
import vn.vccorp.servicemonitoring.message.Messages;
import vn.vccorp.servicemonitoring.security.RootUser;
import vn.vccorp.servicemonitoring.utils.AppUtils;
import vn.vccorp.servicemonitoring.utils.BeanUtils;
import vn.vccorp.servicemonitoring.utils.CronExpression;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

    @Value("${ssh.port}")
    private String sshPort;
    @Value("${ssh.username}")
    private String sshUsername;
    @Value("${default.password}")
    private String defaultPassword;

    @Autowired
    UserRepository userRepository;
    @Autowired
    DozerBeanMapper dozerBeanMapper;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    Messages messages;
    @Autowired
    ServiceManagementRepository serviceManagementRepository;
    @Autowired
    ConfigurationRepository configurationRepository;
    @Autowired
    private ServerRepository serverRepository;
    @Autowired
    private UserServerRepository userServerRepository;
    @Autowired
    private EmailService emailService;

    @Override
    public void addAccount(UserDTO userDTO) {
        userDTO.setPassword(passwordEncoder.encode(defaultPassword));
        User user = dozerBeanMapper.map(userDTO, User.class);
        userRepository.save(user);

        //send email to new user
        emailService.sendNotifyToNewUser(user.getEmail(), user.getEmail(), defaultPassword);

        //save UserService
        List<UserServer> userServer = new ArrayList<>();
        List<Server> allServer = serverRepository.findAll();
        for (Server server : allServer) {
            String groups = AppUtils.getGroupUser(server.getIp(), user.getUsername(), sshPort, sshUsername);
            if (groups == null) {
                continue;
            }
            userServer.add(new UserServer(server.getId(), user.getUsername(), user.getId(), groups));
        }
        userServerRepository.saveAll(userServer);
    }

    @Override
    public void initRootAccount() {
        RootUser root = BeanUtils.getBean(RootUser.class);
        if (!userRepository.findByUsernameOrEmailAndIsDeleted(root.getUsername(), root.getEmail(), false).isPresent()) {
            root.setPassword(passwordEncoder.encode(root.getPassword()));
            userRepository.save(dozerBeanMapper.map(root, User.class));
        }
    }

    @Override
    public void initRootConfig() {
        RootConfig root = BeanUtils.getBean(RootConfig.class);
        if (!configurationRepository.findById(root.getId()).isPresent()) {
            configurationRepository.save(dozerBeanMapper.map(root, Configuration.class));
        }
    }


    @Override
    public void updatePassword(int userId, String password) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ApplicationException(ApplicationError.NOT_FOUND_OR_INVALID_ACCOUNT_ID));
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
    }

    @Override
    public void deleteAccount(int deleteUserId) {
        User user = userRepository.findById(deleteUserId).orElseThrow(() -> new ApplicationException(ApplicationError.NOT_FOUND_OR_INVALID_ACCOUNT_ID));
        //if delete an admin account
        if (user.getRole().equals(Role.ADMIN)) {
            //if there is only one admin user then we can not delete it
            if (userRepository.findAllByRoleAndIsDeleted(Role.ADMIN, false).size() == 1) {
                throw new ApplicationException(messages.get("error.user.delete.admin"));
            }
        }

        //if delete an account that own some userServices
        //we must alert user to transfer owner to others
        serviceManagementRepository.findByUserIdAndRole(deleteUserId, Role.OWNER).ifPresent(
                o -> {
                    throw new ApplicationException(messages.get("error.user.delete.owner"));
                }
        );

        user.setDeleted(true);
        userRepository.save(user);
    }

    @Override
    public void updateRole(int userId, Role role) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ApplicationException(ApplicationError.NOT_FOUND_OR_INVALID_ACCOUNT_ID));

        //at least have an admin
        List<User> userAdmin = userRepository.findAllByRole(Role.ADMIN);
        if (userAdmin.size() == 1 && userAdmin.get(0).getId() == userId && role == Role.USER) {
            throw new ApplicationException(messages.get("error.user.change.admin"));
        }

        user.setRole(role);
        userRepository.save(user);
    }

    public void updateConfig(ConfigurationDTO configurationDTO) {
        Configuration config = configurationRepository.getOne(1);
        if (configurationDTO.getCpuLimit() != null) {
            config.setCpuLimit(configurationDTO.getCpuLimit());
        }
        if (configurationDTO.getDiskLimit() != null) {
            config.setDiskLimit(configurationDTO.getDiskLimit());
        }
        if (configurationDTO.getGpuLimit() != null) {
            config.setGpuLimit(configurationDTO.getGpuLimit());
        }
        if (configurationDTO.getRamLimit() != null) {
            config.setRamLimit(configurationDTO.getRamLimit());
        }
        if (configurationDTO.getHealthCheckSchedule() != null) {
            config.setHealthCheckSchedule(configurationDTO.getHealthCheckSchedule());
        }
        if (configurationDTO.getReportSchedule() != null) {
            if (CronExpression.isValidExpression(configurationDTO.getReportSchedule())) {
                config.setReportSchedule(configurationDTO.getReportSchedule());
            } else {
                throw new ApplicationException(messages.get("error.cron.expression"));
            }
        }
        configurationRepository.save(config);
    }

    @Override
    public Page<UserInfoDTO> listAllUser(int currentPage, int pageSize) {

        Pageable pageNumber = PageRequest.of(currentPage, pageSize);
        return userRepository.getAllUser(pageNumber);
    }
}
