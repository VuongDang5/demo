/**
 * Created by: tuyennta
 * Created on: 31/05/2019 12:00
 */

package vn.vccorp.servicemonitoring.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceErrorDTO {
    private String serviceName;
    private String deployedServer;
    private String status;
    private String problem;
    private String detail;
    private String linkOnTool;
}
