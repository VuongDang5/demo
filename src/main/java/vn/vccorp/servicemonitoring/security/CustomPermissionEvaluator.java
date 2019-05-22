/**
 * Created by: tuyennta
 * Created on: 22/05/2019 09:17
 */

package vn.vccorp.servicemonitoring.security;

import org.springframework.stereotype.Component;

@Component("CustomPermissionEvaluator")
public class CustomPermissionEvaluator implements ICustomPermissionEvaluator {

    @Override
    public boolean forService(int userId, int serviceId) {
        System.out.println("user: " + userId + ", service: " + serviceId);
        return false;
    }
}
