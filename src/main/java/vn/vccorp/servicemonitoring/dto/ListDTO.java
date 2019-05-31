package vn.vccorp.servicemonitoring.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor

public class ListDTO {
        //User info
        private String name;
        private String username;
        private String email;
        private String phone;

        private List<ServiceInfo> services;
        private List<ServerInfo> servers;

        public ListDTO(String name, String username, String email, String phone, List<ServiceInfo> services, List<ServerInfo> servers) {
                this.name = name;
                this.username = username;
                this.email = email;
                this.phone = phone;
                this.services = services;
                this.servers = servers;
        }
}
