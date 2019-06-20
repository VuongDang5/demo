package vn.vccorp.servicemonitoring.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ServerInfo {
    private String name;
    private String ip;
    private String groups;
    private String description;
    private String status;
}
