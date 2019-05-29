/**
 * Created by: tuyennta
 * Created on: 20/05/2019 10:14
 */

package vn.vccorp.servicemonitoring.logic.service;

import org.springframework.web.multipart.MultipartFile;
import vn.vccorp.servicemonitoring.dto.ServiceDTO;

public interface MonitorService {
    void registerService(ServiceDTO serviceDTO);

    void startService(int serviceId);

    void stopService(int serviceId);

    void deployService(ServiceDTO serviceDTO, Integer currentUserId,
                       MultipartFile jar,
                       MultipartFile originalJar,
                       MultipartFile dependencies,
                       MultipartFile modelFile,
                       MultipartFile sourceCode,
                       MultipartFile dockerFile);

}
