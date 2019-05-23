/**
 * Created by: tuyennta
 * Created on: 20/05/2019 10:15
 */

package vn.vccorp.servicemonitoring.logic.service.impl;

import org.dozer.DozerBeanMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import vn.vccorp.servicemonitoring.dto.ServiceDTO;
import vn.vccorp.servicemonitoring.exception.ApplicationException;
import vn.vccorp.servicemonitoring.logic.repository.ServiceRepository;
import vn.vccorp.servicemonitoring.logic.service.MonitorService;
import vn.vccorp.servicemonitoring.message.Messages;
import vn.vccorp.servicemonitoring.utils.AppUtils;

import java.io.File;
import java.util.List;
import java.util.Optional;

@Service
public class MonitorServiceImpl implements MonitorService {

    @Value("${ssh.port}")
    private String sshPort;
    @Autowired
    private DozerBeanMapper dozerBeanMapper;
    @Autowired
    private ServiceRepository serviceRepository;
    @Autowired
    private Messages messages;

    @Override
    public void registerService(ServiceDTO serviceDTO) {
        //check if service with specify info is correct on the system
        if (!isProcessAlive(serviceDTO.getServerId(), serviceDTO.getPID())) {
            throw new ApplicationException(messages.get("service.pid.not-available"));
        }
        //check if log file is available
        File logFile = new File(serviceDTO.getLogDir() + serviceDTO.getLogFile());
        if (!isFileExist(serviceDTO.getServerId(), logFile.getAbsolutePath())){
            throw new ApplicationException(messages.get("service.log.not-available"));
        }
        vn.vccorp.servicemonitoring.entity.Service service = dozerBeanMapper.map(serviceDTO, vn.vccorp.servicemonitoring.entity.Service.class);
        service.setStartTime(AppUtils.getStartedDateOfProcess(service.getServerId(), sshPort, service.getPID()));
        serviceRepository.save(service);
    }

    private boolean isFileExist(String serverIP, String filePath){
        String command = "ssh -p " + sshPort + " " + serverIP + " -t 'test -f " + filePath + "'; echo $?";
        List<String> out = AppUtils.executeCommand(command);
        if (!out.isEmpty() && out.get(0).equals("0")){
            return true;
        }
        return false;
    }

    private boolean isProcessAlive(String serverIP, String PID) {
        String command = "ssh -p " + sshPort + " " + serverIP + " -t 'ps -p " + PID + " > /dev/null'; echo $?";
        List<String> out = AppUtils.executeCommand(command);
        //if command execute success it will return 0
        if (!out.isEmpty() && out.get(0).equals("0")) {
            return true;
        }
        return false;
    }

    @Override
    public Page<vn.vccorp.servicemonitoring.entity.Service> showAllService(int pageId) {
        //Dung Pagination de liet ke danh sach tat ca service
        Pageable firstPageWithFourElements = PageRequest.of(pageId, 4);
        Page<vn.vccorp.servicemonitoring.entity.Service> service = serviceRepository.findAll(firstPageWithFourElements);
        return service;

    }

    @Override
    public vn.vccorp.servicemonitoring.entity.Service showService(int serviceId) {
        //Hien thi Detail cua service theo serviceId
        vn.vccorp.servicemonitoring.entity.Service service = serviceRepository.findById(serviceId);
        return service;
    }
}
