package vn.vccorp.servicemonitoring.dto;

import lombok.*;
import vn.vccorp.servicemonitoring.enumtype.Status;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ServiceInfoDTO {

    private Integer id;

    private String name;

    private String email;

    private String role;

    private String description;

    private String pid;

    private String serverIp;

    private String serverPort;

    private String project;

    private String apiEndpoint;

    private String kongMapping;

    private Date startTime;

    private Date lastCheckTime;

    private Status status;

    private String note;
}
