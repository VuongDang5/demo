package vn.vccorp.servicemonitoring.dto.ServiceDetailsDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ServiceInfo    {
    private String name;
    private String description;
    private String serverPort;
    private String pid;
    private String deployDir;
    private String logDir;
    private String logFile;
    private String language;
    private String deployCommand;
    private Float ramLimit;
    private Float cpuLimit;
    private Float gpuLimit;
    private Float diskLimit;
    private String status;
    private Date startTime;
    private Date lastCheckTime;
    private String project;
    private String apiEndpoint;
    private String kongMapping;
    private String note;
}
