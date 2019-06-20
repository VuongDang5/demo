package vn.vccorp.servicemonitoring.dto;

import lombok.*;
import vn.vccorp.servicemonitoring.enumtype.Role;
import vn.vccorp.servicemonitoring.enumtype.Status;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ServiceReportDTO {

    private Integer id;

    private String serviceName;

    private String description;

    private String pid;

    private String serverIp;

    private String serverPort;

    private String project;

    private String apiEndpoint;

    private String kongMapping;

    private Date startTime;

    private String status;

    private String note;

    private Double ramUsed;

    private Double cpuUsed;

    private Double gpuUsed;

    private Double diskUsed;

    private String owner;
    
    private BigInteger totalWarning;
    
    private BigInteger totalError;

}
