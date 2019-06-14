package vn.vccorp.servicemonitoring.dto.ServiceDetailsDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.vccorp.servicemonitoring.enumtype.Status;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ServerInfo {
    private String ip;
    private String name;
    private String description;
    private String rootPath;
    private String status;
}
