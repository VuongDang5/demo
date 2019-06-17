package vn.vccorp.servicemonitoring.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PortDTO {
    String serverIP;
    String sshPort;
    String sshUsername;
    String port;
}
