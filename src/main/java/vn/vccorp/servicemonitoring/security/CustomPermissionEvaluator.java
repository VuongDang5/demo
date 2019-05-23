/**
 * Created by: tuyennta
 * Created on: 22/05/2019 09:17
 */

package vn.vccorp.servicemonitoring.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import vn.vccorp.servicemonitoring.entity.User;
import vn.vccorp.servicemonitoring.entity.UserService;
import vn.vccorp.servicemonitoring.enumtype.ApplicationError;
import vn.vccorp.servicemonitoring.enumtype.Role;
import vn.vccorp.servicemonitoring.exception.ApplicationException;
import vn.vccorp.servicemonitoring.logic.repository.UserRepository;
import vn.vccorp.servicemonitoring.message.Messages;
import vn.vccorp.servicemonitoring.utils.BeanUtils;

import java.util.List;
import java.util.stream.Collectors;

@Component("CustomPermissionEvaluator")
public class CustomPermissionEvaluator implements ICustomPermissionEvaluator {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private Messages messages;

    @Override
    public boolean forService(Role role, String serviceId) {

        int userId = BeanUtils.getAuthorizedUser().getId();

        User user = userRepository.findByIdAndIsDeleted(userId, false).orElseThrow(() -> new ApplicationException(ApplicationError.NOT_FOUND_OR_INVALID_ACCOUNT_ID));
        //if this user is an ADMIN then this user has all permission
        if (user.getRole().equals(Role.ADMIN)) {
            return true;
        }
        //otherwise, this user has to owned or maintained this service
        List<UserService> serviceManagements = user.getServices().parallelStream()
                .filter(s -> s.getService().getId().equals(serviceId) && s.getRole().equals(role)).collect(Collectors.toList());
        return !serviceManagements.isEmpty();
    }
}
