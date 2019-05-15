/**
 * Created by: tuyennta
 * Created on: 10/05/2019 22:07
 */

package vn.vccorp.servicemonitoring.logic.service.impl;

import org.dozer.DozerBeanMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.vccorp.servicemonitoring.dto.UserDTO;
import vn.vccorp.servicemonitoring.entity.User;
import vn.vccorp.servicemonitoring.enumtype.ApplicationError;
import vn.vccorp.servicemonitoring.exception.ApplicationException;
import vn.vccorp.servicemonitoring.logic.repository.AccountRepository;
import vn.vccorp.servicemonitoring.logic.service.AccountService;
import vn.vccorp.servicemonitoring.security.RootUser;
import vn.vccorp.servicemonitoring.utils.BeanUtils;

@Service
public class AccountServiceImpl implements AccountService {

    @Autowired
    AccountRepository accountRepository;
    @Autowired
    DozerBeanMapper dozerBeanMapper;
    @Autowired
    PasswordEncoder passwordEncoder;

    @Override
    public void addAccount(UserDTO userDTO) {
        userDTO.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        accountRepository.save(dozerBeanMapper.map(userDTO, User.class));
    }

    @Override
    public void initRootAccount() {
        RootUser root = BeanUtils.getBean(RootUser.class);
        if (!accountRepository.findByUsernameOrEmail(root.getUsername(), root.getEmail()).isPresent()) {
            root.setPassword(passwordEncoder.encode(root.getPassword()));
            accountRepository.save(dozerBeanMapper.map(root, User.class));
        }
    }

    @Override
    public void updatePassword(int userId, String password) {
        User user = accountRepository.findById(userId).orElseThrow(() -> new ApplicationException(ApplicationError.NOT_FOUND_OR_INVALID_ACCOUNT_ID));
        user.setPassword(passwordEncoder.encode(password));
        accountRepository.save(user);
    }
}
