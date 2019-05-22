/**
 * Created by: tuyennta
 * Created on: 22/05/2019 09:17
 */

package vn.vccorp.servicemonitoring.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import vn.vccorp.servicemonitoring.entity.ServiceManagement;
import vn.vccorp.servicemonitoring.entity.User;
import vn.vccorp.servicemonitoring.enumtype.ApplicationError;
import vn.vccorp.servicemonitoring.exception.ApplicationException;
import vn.vccorp.servicemonitoring.logic.repository.UserRepository;
import vn.vccorp.servicemonitoring.message.Messages;

import java.util.List;
import java.util.stream.Collectors;

@Component("CustomPermissionEvaluator")
public class CustomPermissionEvaluator implements ICustomPermissionEvaluator {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private Messages messages;

    @Override
    public boolean forService(int userId, int serviceId) {
        User user = userRepository.findByIdAndIsDeleted(userId, false).orElseThrow(() -> new ApplicationException(ApplicationError.NOT_FOUND_OR_INVALID_ACCOUNT_ID));
        List<ServiceManagement> serviceManagements = user.getServices().parallelStream().filter(s -> s.getService().getId().equals(serviceId)).collect(Collectors.toList());
        return !serviceManagements.isEmpty();
    }
}
