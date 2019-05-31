package vn.vccorp.servicemonitoring.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
public class ListDTO {
        //User info
        private String name;
        private String username;
        private String email;
        private String phone;

        private String serviceName;
        private String pid;
        private String role;
        private String serviceDescription;
        private String serviceStatus;

        private String serverName;
        private String ip;
        private String groups;
        private String serverDescription;
        private String serverStatus;
        public ListDTO(String name, String username, String email, String phone, String serviceName, String pid, String role, String serviceDescription, String serviceStatus, String serverName, String ip, String groups, String serverDescription, String serverStatus) {
                this.name = name;
                this.username = username;
                this.email = email;
                this.phone = phone;
                this.serviceName = serviceName;
                this.pid = pid;
                this.role = role;
                this.serviceDescription = serviceDescription;
                this.serviceStatus = serviceStatus;
                this.serverName = serverName;
                this.ip = ip;
                this.groups = groups;
                this.serverDescription = serverDescription;
                this.serverStatus = serverStatus;
        }
}
