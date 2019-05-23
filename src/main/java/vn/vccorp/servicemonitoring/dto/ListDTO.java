package vn.vccorp.servicemonitoring.dto;

import lombok.*;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ListDTO {
        //User info
        private String name;
        private String username;
        private String email;
        private String phone;

        //Service info
        private String serviceName;
        private String pid;
        private String role;
        private String serviceDescription;
        private String serviceStatus;

        //Server info
        private String serverName;
        private String ip;
        private String groups;
        private String serverDescription;
        private String serverStatus;
}
