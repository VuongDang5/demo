/**
 * Created by: tuyennta
 * Created on: 20/05/2019 10:15
 */

package vn.vccorp.servicemonitoring.logic.service.impl;

import org.dozer.DozerBeanMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.vccorp.servicemonitoring.dto.ServiceDTO;
import vn.vccorp.servicemonitoring.exception.ApplicationException;
import vn.vccorp.servicemonitoring.logic.repository.ServiceRepository;
import vn.vccorp.servicemonitoring.logic.service.MonitorService;
import vn.vccorp.servicemonitoring.message.Messages;
import vn.vccorp.servicemonitoring.utils.AppUtils;

import java.util.List;

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
        //check if service is running on an PID or not: ssh -p 2395 10.5.36.95 -t 'ps -p 16611'
        if (isProcessAlive(serviceDTO.getServerId(), serviceDTO.getPID())){
            vn.vccorp.servicemonitoring.entity.Service service = dozerBeanMapper.map(serviceDTO, vn.vccorp.servicemonitoring.entity.Service.class);
            service.setStartTime(AppUtils.getStartedDateOfProcess(service.getServerId(), sshPort, service.getPID()));
            serviceRepository.save(service);
        } else {
            throw new ApplicationException(messages.get("service.pid.not-available"));
        }
    }

    private boolean isProcessAlive(String serverIP, String PID){
        String command = "ssh -p " + sshPort + " " + serverIP + " -t 'ps -p " + PID + " > /dev/null'; echo $?";
        List<String> out = AppUtils.executeCommand(command);
        //if command execute success it will return 0
        if (!out.isEmpty() && out.get(0).equals("0")){
            return true;
        }
        return false;
    }
}
