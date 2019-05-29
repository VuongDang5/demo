/**
 * Created by: tuyennta
 * Created on: 22/05/2019 11:38
 */

package vn.vccorp.servicemonitoring.security;

import vn.vccorp.servicemonitoring.enumtype.Role;

import java.util.List;

public interface ICustomPermissionEvaluator {
    boolean forService(Role role, String serviceId);

    boolean forService(List<Role> roles, Integer serviceId);
}
