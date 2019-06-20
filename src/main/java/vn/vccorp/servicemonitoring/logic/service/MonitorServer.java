package vn.vccorp.servicemonitoring.logic.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.vccorp.servicemonitoring.dto.PortDTO;
import vn.vccorp.servicemonitoring.dto.ServerDTO;
import vn.vccorp.servicemonitoring.dto.ShowServerDTO;

import java.util.List;

public interface MonitorServer {
    void registerServer(ServerDTO serverDTO);

    Page<ShowServerDTO> getAllServer(Pageable page);
    String checkIfPortIsBeingUsed(PortDTO info);
    List<String> getAllPort(PortDTO info);
}