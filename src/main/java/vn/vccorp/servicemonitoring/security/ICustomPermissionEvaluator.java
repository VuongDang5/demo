/**
 * Created by: tuyennta
 * Created on: 22/05/2019 11:38
 */

package vn.vccorp.servicemonitoring.security;

public interface ICustomPermissionEvaluator {
    boolean forService(int userId, int serviceId);
}
