package vn.vccorp.servicemonitoring.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ServiceInfo {
    private String name;
    private String pid;
    private String role;
    private String description;
    private String status;
}
