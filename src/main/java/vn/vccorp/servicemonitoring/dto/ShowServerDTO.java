package vn.vccorp.servicemonitoring.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShowServerDTO {
    private int STT;
    private String name;
    private String ip;
    private String description;
    private String ramFree, ramUsed;
    private String diskFree, diskUsed;
    private String cpuUsed, speed, core;
    private String user;
    private String status;
}