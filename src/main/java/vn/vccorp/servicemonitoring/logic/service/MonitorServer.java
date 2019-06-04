package vn.vccorp.servicemonitoring.logic.service;

import vn.vccorp.servicemonitoring.dto.ServerDTO;

public interface MonitorServer {
    void registerServer(ServerDTO serverDTO);
}