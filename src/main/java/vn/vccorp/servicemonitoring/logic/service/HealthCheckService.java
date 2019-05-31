/**
 * Created by: tuyennta
 * Created on: 30/05/2019 11:03
 */

package vn.vccorp.servicemonitoring.logic.service;

import vn.vccorp.servicemonitoring.entity.Service;

public interface HealthCheckService {

    void checkResources(Service service);

    void healthCheck1(Service service);
}
